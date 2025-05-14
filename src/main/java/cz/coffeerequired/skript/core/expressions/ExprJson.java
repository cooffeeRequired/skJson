package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.PathParser;
import cz.coffeerequired.support.SkriptUtils;
import lombok.Getter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

@Name("Json")
@Description("")
@Examples("")
@SuppressWarnings("unchecked")
public class ExprJson<T> extends SimpleExpression<Object> {
    Expression<JsonElement> jsonExpression;
    Expression<String> pathExpression;
    @Getter
    private JsonType jsonType;

    public enum JsonState {
        VALUE, INDEX, INDICES, KEY, ENTRIES
    }

    public enum JsonType {
        OBJECT, ARRAY
    }

    @Getter
    private JsonState state;


    @Override
    public T @Nullable [] get(Event event) {
        return get(event, this.jsonExpression.getSingle(event), this.state);
    }

    @SuppressWarnings("unused")
    public boolean checkType(Object json, JsonType type) {
       return checkType(json, type,null);
    }

    public boolean checkType(Object json, JsonType type, String message) {
        Class<? extends JsonElement> toClass = type.equals(JsonType.OBJECT) ? JsonObject.class : JsonArray.class;

        if (toClass != json.getClass()) {
            SkJson.severe(getParser().getNode(), message != null ? message : "");
            return false;
        }
        return true;
    }


    @SuppressWarnings("unchecked")
    public T[] get(Event event, JsonElement json, JsonState state) {
        if (json == null) {
            SkJson.severe(getParser().getNode(), "");
            return null;
        }

        var accessor = new JsonAccessor(json);

        ArrayList<Map.Entry<String, PathParser.Type>> tokens = pathExpression == null ?
                new ArrayList<>() :
                PathParser.tokenize(pathExpression.getSingle(event), Api.Records.PROJECT_DELIM);


        switch (state) {
            case VALUE -> {
                if (tokens.isEmpty()) {
                    return (T[]) JsonAccessorUtils.getAsParsedArray(json);
                } else {
                    var found = accessor.searcher.keyOrIndex(tokens);
                    return (T[]) JsonAccessorUtils.getAsParsedArray(found);
                }
            }
            case KEY -> {
                if (tokens.isEmpty()) {
                    if (checkType(json, JsonType.OBJECT, "You can loops keys only in json object but given %s".formatted(json.getClass().getSimpleName().toLowerCase()))) {
                        return (T[]) ((JsonObject) json).keySet().toArray(String[]::new);
                    }
                } else {
                    var found = accessor.searcher.keyOrIndex(tokens);
                    assert found != null;
                    if (checkType(found, JsonType.OBJECT, "You can loops keys only in json object but given %s".formatted(found.getClass().getSimpleName().toLowerCase()))) {
                        return (T[]) ((JsonObject) found).keySet().toArray(String[]::new);
                    }
                }
            }
            case ENTRIES -> {
                if (tokens.isEmpty()) {
                    if (checkType(json, JsonType.OBJECT, "You can loops entries only in json object but given %s".formatted(json.getClass().getSimpleName().toLowerCase()))) {
                        return (T[]) ((JsonObject) json).entrySet().toArray(Map.Entry[]::new);
                    }
                } else {
                    var found = accessor.searcher.keyOrIndex(tokens);
                    assert found != null;
                    if (checkType(found, JsonType.OBJECT, "You can loops entries only in json object but given json %s".formatted(found.getClass().getSimpleName().toLowerCase()))) {
                        return (T[]) ((JsonObject) found).entrySet().toArray(Map.Entry[]::new);
                    }
                }
            }
            case INDEX, INDICES -> {
                if (tokens.isEmpty()) {
                    if (checkType(json, JsonType.ARRAY, "You can loops indices only in json array but given %s".formatted(json.getClass().getSimpleName().toLowerCase()))) {
                        return (T[]) JsonAccessorUtils.getArrayIndices(((JsonArray) json)).toArray();
                    }
                } else {
                    var found = accessor.searcher.keyOrIndex(tokens);
                    assert found != null;
                    if (checkType(found, JsonType.ARRAY, "You can loops indices only in json array but given %s".formatted(found.getClass().getSimpleName().toLowerCase()))) {
                        return (T[]) JsonAccessorUtils.getArrayIndices(((JsonArray) found)).toArray();
                    }
                }
            }
        }

        return (T[]) SkriptUtils.emptyArray(Object.class);

    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        var state_ = state.toString().toLowerCase();
        var jsonType_ = jsonType.toString().toLowerCase();
        var jE =  jsonExpression.toString(event, debug);
        var pE = pathExpression == null ? "" : "at path " + pathExpression.toString(event, debug);

        return "loop %s of json %s %s %s".formatted(state_, jsonType_, jE, pE);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        jsonType = parseResult.hasTag("object") ? JsonType.OBJECT : parseResult.hasTag("array") ?  JsonType.ARRAY : null;
        state = parseResult.hasTag("values")
                ? JsonState.VALUE : parseResult.hasTag("keys")
                ? JsonState.KEY : parseResult.hasTag("entries")
                ? JsonState.ENTRIES : parseResult.hasTag("indexes")
                ? JsonState.INDEX : parseResult.hasTag("indices")
                ? JsonState.INDICES : null;
        jsonExpression = LiteralUtils.defendExpression(expressions[0]);
        pathExpression = LiteralUtils.defendExpression(expressions[1]);
        return LiteralUtils.canInitSafely(jsonExpression);
    }
}
