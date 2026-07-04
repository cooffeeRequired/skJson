package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Json values")
@Description({
        "Reads or changes a single value or a list of values at a JSON path.",
        "Use `value at path … in …` / `values at path … in …` for the preferred syntax.",
        "Legacy aliases `json value`, `json values`, `path … of …` and `{json}'s value at path …` are also supported.",
        "Without a path, `values …` returns all top-level values and can be used as a loop source."
})
@Since({"4.1 - API UPDATE", "5.0"})
@Examples("""
            set {_json} to parse "{""test"": [true, false, {""A"": [1, 2, 3]}]}" as json

            send value at path "test.2" in {_json}
            send values at path "test" in {_json}
            loop values at path "test" in {_json}:
                send "%loop-value%"
        """)
public class ExprJsonValues extends SimpleExpression<Object> {

    private Type type;
    private Expression<JsonElement> jsonVariable;
    private Expression<String> pathVariable;
    @Override
    protected @Nullable Object[] get(Event event) {
        try {
            boolean isPathEmpty = pathVariable == null;
            JsonElement tempJson = jsonVariable.getSingle(event);

            if (tempJson == null || tempJson instanceof JsonNull) return new Object[0];

            var stringifyPath = !isPathEmpty ? pathVariable.getSingle(event) : null;
            var tokens = PathParser.tokenize(stringifyPath, Api.Records.PROJECT_DELIM);

            if (type.equals(Type.MULTIPLES)) {
                if (!isPathEmpty) {
                    Object resolved = JsonAccessorUtils.resolveParsed(tempJson, tokens);
                    if (resolved == null) return new Object[0];
                    return JsonAccessorUtils.getAsParsedArray(resolved);
                } else {
                    return JsonAccessorUtils.getAsParsedArray(tempJson);
                }
            } else if (type.equals(Type.SINGLE)) {
                if (!isPathEmpty) {
                    Object resolved = JsonAccessorUtils.resolveParsed(tempJson, tokens);
                    if (resolved instanceof JsonArray)
                        SkJson.warning("You didn't want to use \"value\" instead of \"values\", the plural expression will ensure that you get the array");
                    if (resolved == null) return new Object[0];
                    return new Object[]{resolved};
                } else {
                    SkJson.warning("You cannot use \"value\" in root without specified path");
                    return new Object[0];
                }
            }
        } catch (Exception e) {
            SkJson.exception(e, "get");
        }
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return type == Type.SINGLE;
    }

    @Override
    public Class<Object> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return this.type.equals(Type.SINGLE) ? "json value" : "json values" + " of " + jsonVariable.toString(event, b);
    }


    private static boolean isMultiplesPattern(int matchedPattern, SkriptParser.ParseResult parseResult) {
        if (parseResult.hasTag("values")) {
            return true;
        }
        return switch (matchedPattern) {
            case 0, 1, 3, 4, 6, 7 -> true;
            default -> false;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        type = isMultiplesPattern(i, parseResult) ? Type.MULTIPLES : Type.SINGLE;
        pathVariable = null;
        jsonVariable = null;
        for (Expression<?> expr : expressions) {
            if (expr == null) {
                continue;
            }
            Class<?> returnType = expr.getReturnType();
            if (returnType != null && JsonElement.class.isAssignableFrom(returnType)) {
                jsonVariable = defendExpression(expr);
            } else if (returnType != null && String.class.isAssignableFrom(returnType)) {
                pathVariable = (Expression<String>) expr;
            }
        }

        return canInitSafely(jsonVariable);
    }

    @Override
    public boolean isLoopOf(String input) {
        return input.equals("skjson-custom-loop");
    }

    @Override
    public @Nullable Iterator<?> iterator(Event event) {
        if (type != Type.MULTIPLES) {
            return null;
        }
        JsonElement root = jsonVariable.getSingle(event);
        if (root == null || root.isJsonNull()) {
            return null;
        }
        if (pathVariable != null) {
            String path = pathVariable.getSingle(event);
            if (path != null && !path.isEmpty()) {
                root = JsonAccessorUtils.resolve(root, PathParser.tokenize(path, Api.Records.PROJECT_DELIM));
                if (root == null) {
                    return null;
                }
            }
        }
        return entryIterator(root);
    }

    private Iterator<HashMap<String, Object>> entryIterator(JsonElement it) {
        final List<String> objectKeys;
        if (it instanceof JsonObject object) {
            objectKeys = List.copyOf(object.keySet());
        } else {
            objectKeys = List.of();
        }

        return new Iterator<>() {
            int idx = 0;

            @Override
            public boolean hasNext() {
                if (it instanceof JsonArray array) {
                    return idx < array.size();
                }
                if (it instanceof JsonObject) {
                    return idx < objectKeys.size();
                }
                return false;
            }

            @Override
            public HashMap<String, Object> next() {
                HashMap<String, Object> itMap = new HashMap<>();
                if (it instanceof JsonArray array) {
                    itMap.put(String.valueOf(idx), Parser.fromJson(array.get(idx)));
                    idx++;
                    return itMap;
                }
                if (it instanceof JsonObject object) {
                    String declaredKey = objectKeys.get(idx);
                    itMap.put(declaredKey, Parser.fromJson(object.get(declaredKey)));
                    idx++;
                    return itMap;
                }
                throw new IllegalStateException("Cannot iterate json element: " + it);
            }
        };
    }

    enum Type {SINGLE, MULTIPLES}

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET, REMOVE_ALL -> CollectionUtils.array(Object[].class);
            case DELETE -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
        if (pathVariable == null) {
            SkJson.severe(getParser().getNode(), "Cannot change root json without a path");
            return;
        }
        var tokens = PathParser.tokenize(pathVariable.getSingle(event), Api.Records.PROJECT_DELIM);

        SkJson.debug("&cTrying change -> &e%s", mode);

        SkJson.debug("tokens %s", tokens);

        var json = jsonVariable.getSingle(event);
        if (json == null) {
            SkJson.severe(getParser().getNode(), "Trying to change JSON %s what is null", jsonVariable.toString(event, false));
            return;
        }
        var serializedJson = new JsonAccessor(json);

        delta = delta == null ? new Object[0] : delta;

        SkJson.debug("type %s", type);

        if (type.equals(Type.SINGLE) && delta.length > 1) {
            SkJson.severe(getParser().getNode(), "You are using 'value' instead of 'values', Do you want to use 'values' instead?");
            return;
        }

        switch (mode) {
            case SET -> {
                if (delta.length > 1) {
                    JsonArray array = new JsonArray();

                    for (Object o : delta) {
                        JsonElement parsed = Parser.toJson(o);
                        array.add(parsed);
                    }

                    serializedJson.changer.value(tokens, array);
                } else {
                    for (Object o : delta) {
                        JsonElement parsed = Parser.toJson(o);
                        serializedJson.changer.value(tokens, parsed);
                    }
                }
                break;
            }
            case DELETE -> {
                serializedJson.remover.byKey(tokens);
                break;
            }
            case REMOVE_ALL -> {
                for (var o : delta) {
                    serializedJson.remover.allByValue(tokens, o);
                }
                break;
            }
            default -> {
                SkJson.severe(getParser().getNode(), "Unknown change mode: %s", mode);
            }
        }
    }
}
