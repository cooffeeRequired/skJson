package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.api.json.SerializedJsonUtils;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Json values")
@Description("You can get values/s from the given Json")
@Since({"4.1 - API UPDATE", "5.0"})
@Examples("""
            set {_json} to json from "{test: [true, false, {A: [1,2,3]}]}"
        
            send value "test::2" of {_json}
            send values "test" of {_json}
        """)
public class ExprJsonValues extends SimpleExpression<Object> {

    private Type type;
    private Expression<JsonElement> jsonVariable;
    private Expression<String> pathVariable;
    public boolean relevantToLoop = false;

    @Override
    protected @Nullable Object[] get(Event event) {
        try {
            boolean isPathEmpty = pathVariable == null;
            JsonElement tempJson = jsonVariable.getSingle(event);

            if (tempJson == null || tempJson instanceof JsonNull) return new Object[0];

            SerializedJson serializedJson = new SerializedJson(tempJson);

            var stringifyPath = !isPathEmpty ? pathVariable.getSingle(event) : null;
            var tokens = SkriptJsonInputParser.tokenize(stringifyPath, Api.Records.PROJECT_DELIM);

            if (type.equals(Type.MULTIPLES)) {
                if (!isPathEmpty) {
                    Object searcherResult = serializedJson.searcher.keyOrIndex(tokens);
                    if (searcherResult == null) return new Object[0];
                    return relevantToLoop ? new Object[]{searcherResult} : SerializedJsonUtils.getAsParsedArray(searcherResult);
                } else {
                    return relevantToLoop ? new JsonElement[]{serializedJson.getJson()} : SerializedJsonUtils.getAsParsedArray(serializedJson.getJson());
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

    @Override
    public boolean isLoopOf(String input) {
        relevantToLoop = input.equals("skjson-custom-loop");
        return input.equals("skjson-custom-loop");
    }

    @Override
    public @Nullable Iterator<?> iterator(Event event) {
        Object o;
        JsonElement json = null;

        Iterator<?> superIterator = super.iterator(event);
        if (superIterator == null) return null;

        while (superIterator.hasNext()) {
            o = superIterator.next();
            if (!(o instanceof JsonElement)) return null;
            else json = (JsonElement) o;
        }

        JsonElement it = json;
        return new Iterator<>() {
            int idx = 0;

            @Override
            public boolean hasNext() {
                if (it instanceof JsonArray array) {
                    return idx < array.size();
                } else if (it instanceof JsonObject object) {
                    return idx < object.entrySet().size();
                }
                return false;
            }

            @Override
            public Object next() {
                HashMap<String, Object> itMap = new HashMap<>();
                if (it instanceof JsonArray array) {
                    itMap.put(String.valueOf(idx), SerializedJsonUtils.lazyJsonConverter(array.get(idx)));
                    idx++;
                    return itMap;
                } else if (it instanceof JsonObject object) {
                    var keys = object.keySet().stream().toList();
                    String declaredKey = keys.get(idx);
                    itMap.put(declaredKey, SerializedJsonUtils.lazyJsonConverter(object.get(declaredKey)));
                    idx++;
                    return itMap;
                }
                return null;
            }
        };
    }

    enum Type {SINGLE, MULTIPLES}
}
