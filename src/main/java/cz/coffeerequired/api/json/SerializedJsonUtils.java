package cz.coffeerequired.api.json;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.skript.json.SupportSkriptJson;

import java.util.*;

import static cz.coffeerequired.api.Api.Records.PROJECT_DEBUG;
import static cz.coffeerequired.skript.json.SupportSkriptJson.JsonSupportElement.SearchType;

@SuppressWarnings("ALL")
public abstract class SerializedJsonUtils {
    public static boolean isJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception exception) {
            SkJson.logger().exception("isJson, wont parse that " + json.toString(), exception);
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

    public static Number isNumeric(Object obj) {
        try {
            return Double.parseDouble(obj.toString());
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
            throw new SerializedJsonException("Json is not object or array: " + json.toString());
        }
    }

    public static <T> JsonElement lazyObjectConverter(T object) {
        try {
            if (object == null) return null;
            Class<?> clazz = object.getClass();
            if (clazz.equals(String.class)) {
                try {
                    return JsonParser.parseString(object.toString());
                } catch (Exception e) {
                    var gson = new GsonBuilder().setLenient().create();
                    return gson.toJsonTree(object);
                }
            }
            if (clazz.equals(Integer.class) || clazz.equals(LazilyParsedNumber.class)) {
                if (clazz.equals(LazilyParsedNumber.class)) {
                    return new JsonPrimitive(((LazilyParsedNumber) object).intValue());
                } else {
                    return new JsonPrimitive((Integer) object);
                }
            }
            if (clazz.equals(Boolean.class))
                return new JsonPrimitive((Boolean) object);
            if (clazz.equals(Double.class) || clazz.equals(Float.class))
                return new JsonPrimitive(((Number) object).doubleValue());
            if (clazz.equals(Long.class))
                return new JsonPrimitive((Long) object);
            if (clazz.equals(Byte.class))
                return new JsonPrimitive((Byte) object);
            if (clazz.equals(Short.class))
                return new JsonPrimitive((Short) object);
            if (clazz.equals(Character.class))
                return new JsonPrimitive((Character) object);
            if (object instanceof JsonElement)
                return (JsonElement) object;
            return null;
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }


    public static <T> T lazyJsonConverter(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        if (json.isJsonArray() || json.isJsonObject()) return (T) json;
        else if (json.isJsonPrimitive()) return (T) GsonParser.getGson().fromJson(json, Object.class);
        else return null;
    }

    public static Object[] getAsParsedArray(Object input) {
        if (!(input instanceof JsonElement)) return new Object[]{input};
        JsonElement current = (JsonElement) input;
        ArrayList<Object> results = new ArrayList<>();
        if (current == null || current.isJsonPrimitive() || current.isJsonNull()) return results.toArray();
        if(current instanceof JsonArray array) {
            for (JsonElement element : array) {
                if (element != null) {
                    results.add(GsonParser.fromJson(element));
                }
            }
        } else if (current instanceof JsonObject object) {
            for (String key : object.keySet()) {
                JsonElement element = object.get(key);
                if (element != null) {
                    results.add(GsonParser.fromJson(element));
                }
            }
        }

        return results.toArray();
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

    public static boolean isValidJson(Object o) {
        try {
            if (o instanceof String str) {
                JsonParser.parseString(str);
                return true;
            }
            else if (o instanceof JsonElement element) {
                return true;
            } else {
                GsonParser.toJson(o);
                return true;
            }
        } catch (Exception e) {
            if (PROJECT_DEBUG) {
                SkJson.logger().exception("isValidJson, wont parse that " + o.toString(), e);
            }
            return false;
        }
    }

    public static Object getFirst(JsonElement json, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.size() > 0) {
                return type.equals(SearchType.VALUE) ? array.get(0) : 0;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.size() > 0) {
                return type.equals(SearchType.VALUE)
                        ? GsonParser.fromJson((JsonElement) object.entrySet().iterator().next().getValue())
                        : object.keySet().iterator().next();
            }
        }
        return json;
    }

    public static Object getLast(JsonElement json, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.size() > 0) {
                return type.equals(SearchType.VALUE) ? GsonParser.fromJson(array.get(array.size() - 1)) : array.size() - 1;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.size() > 0) {
                return type.equals(SearchType.VALUE)
                        ? GsonParser.fromJson((JsonElement) (object.entrySet().toArray(Map.Entry[]::new)[object.keySet().size() - 1]).getValue())
                        : object.keySet().toArray(String[]::new)[object.keySet().size() - 1];
            }
        }
        return json;
    }

    public static Object get(JsonElement json, int index, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.size() > index) {
                return type.equals(SearchType.VALUE)
                    ? GsonParser.fromJson(array.get(index))
                    : index;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.size() > index) {
                return type.equals(SearchType.VALUE)
                    ? GsonParser.fromJson((JsonElement) object.entrySet().toArray()[index])
                    : object.keySet().toArray()[index];
            }
        }
        return json;
    }

    public static Object getRandom(JsonElement json, SearchType type) {
        Random random = new Random();

        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.size() > 0) {
                int index = random.nextInt(array.size());
                return type.equals(SearchType.VALUE) ? GsonParser.fromJson(array.get(index)) : index;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.size() > 0) {
                int index = random.nextInt(object.size());
                if (type.equals(SearchType.VALUE)) {
                    return GsonParser.fromJson((JsonElement) object.entrySet().toArray(Map.Entry[]::new)[index].getValue());
                } else {
                    return object.keySet().toArray(String[]::new)[index];
                }
            }
        }
        return json;
    }
}
