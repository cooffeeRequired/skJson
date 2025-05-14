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
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Json values")
@Description({
        "Returns the value of the json at the given path. If the path is not provided, it will return all values from the json.",
        "If the path is a number, it will return the value at that index in the array.",
        "If the path is a string, it will return the value at that key in the object.",
        "If the path is empty, it will return all values from the json."
})
@Since({"4.1 - API UPDATE", "5.0"})
@Examples("""
            set {_json} to json from "{test: [true, false, {A: [1,2,3]}]}"
        
            send value "test.2" of {_json}
            send values "test" of {_json}
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

            JsonAccessor serializedJson = new JsonAccessor(tempJson);

            var stringifyPath = !isPathEmpty ? pathVariable.getSingle(event) : null;
            var tokens = PathParser.tokenize(stringifyPath, Api.Records.PROJECT_DELIM);

            if (type.equals(Type.MULTIPLES)) {
                if (!isPathEmpty) {
                    Object searcherResult = serializedJson.searcher.keyOrIndex(tokens);
                    if (searcherResult == null) return new Object[0];
                    return JsonAccessorUtils.getAsParsedArray(searcherResult);
                } else {
                    return  JsonAccessorUtils.getAsParsedArray(serializedJson.getJson());
                }
            } else if (type.equals(Type.SINGLE)) {
                if (!isPathEmpty) {
                    Object searcherResult = serializedJson.searcher.keyOrIndex(tokens);
                    if (searcherResult instanceof JsonArray)
                        SkJson.warning("You didn't want to use \"value\" instead of \"values\", the plural expression will ensure that you get the array");
                    if (searcherResult == null) return new Object[0];
                    return new Object[]{searcherResult};
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


    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        type = i == 0 ? Type.SINGLE : Type.MULTIPLES;
        jsonVariable = defendExpression(expressions[1]);
        pathVariable = (Expression<String>) expressions[0];
        return canInitSafely(jsonVariable);
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
        var tokens = PathParser.tokenize(pathVariable.getSingle(event), Api.Records.PROJECT_DELIM);

        var json = jsonVariable.getSingle(event);
        if (json == null) {
            SkJson.severe(getParser().getNode(), "Trying to change JSON %s what is null", jsonVariable.toString(event, false));
            return;
        }
        var serializedJson = new JsonAccessor(json);

        delta = delta == null ? new Object[0] : delta;

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
            }
            case DELETE -> serializedJson.remover.byKey(tokens);
            case REMOVE_ALL -> {
                for (var o : delta) {
                    serializedJson.remover.allByValue(tokens, o);
                }
            }
            default -> SkJson.severe(getParser().getNode(), "Unknown change mode: %s", mode);
        }
    }
}
