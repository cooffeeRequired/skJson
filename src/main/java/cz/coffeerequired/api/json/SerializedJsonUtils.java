package cz.coffeerequired.api.json;

import com.google.gson.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

@SuppressWarnings("ALL")
public abstract class SerializedJsonUtils {
    public static boolean isJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isQuoted(JsonElement json) {
        boolean isQuoted = json.toString().startsWith("\"") && json.toString().endsWith("\"");
        if (json.isJsonNull()) return false;
        if (json.isJsonPrimitive()) return isQuoted;
        else return false;
    }

    public static boolean isNull(JsonElement json) {
        if (json == null) return false;
        return json.isJsonNull();
    }

    public static boolean isExpression(JsonElement json) {
        if (json.isJsonObject()) {
           return !json.getAsJsonObject().entrySet().isEmpty();
        }
        return false;
    }

    public static <E> Deque<E> listToDeque(ArrayList<E> list) {
        return new ArrayDeque<>(list);
    }

    public static Number isNumeric(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static JsonElement handle(JsonElement json, Object key) throws SerializedJsonException, NumberFormatException {
        if (json instanceof JsonObject object) {
            return object.get(key.toString());
        } else if (json instanceof JsonArray element) {
            return element.get(Integer.parseInt(key.toString()));
        } else {
            throw new SerializedJsonException("Json is not object or array");
        }
    }

    public static <T> JsonElement lazyObjectConverter(T object) {
        JsonParser parser = new JsonParser();
        if(object == null) return null;
        if (object instanceof JsonElement element) return element;
        else if (object instanceof String string) return parser.parse(string);
        else if (object instanceof Number number) return parser.parse(number.toString());
        else if (object instanceof Boolean bool) return parser.parse(bool.toString());
        else if (object instanceof Character character) return parser.parse(character.toString());
        else if (object instanceof JsonObject jsonObject) return jsonObject;
        else if (object instanceof JsonArray jsonArray) return jsonArray;
        else if (object instanceof JsonNull jsonNull) return jsonNull;
        else if (object instanceof JsonPrimitive jsonPrimitive) return jsonPrimitive;
        else return null;
    }


    public static <T> T lazyJsonConverter(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        if (json.isJsonArray() || json.isJsonObject()) return (T) json;
        else if (json.isJsonPrimitive()) return (T) GsonParser.getGson().fromJson(json, Object.class);
        else return null;
    }

    public static boolean isJavaType(Object object) {
        Class<?> c = object.getClass();
        return (c.isAssignableFrom(String.class) ||
                c.isAssignableFrom(Number.class) ||
                c.isAssignableFrom(Boolean.class) ||
                object instanceof Number ||
                c.isAssignableFrom(Integer.class) ||
                c.isAssignableFrom(Long.class));
    }

}
