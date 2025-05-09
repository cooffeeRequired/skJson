package cz.coffeerequired.api.json;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;

import ch.njol.skript.lang.parser.ParserInstance;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.skript.core.support.JsonSupportElements.SearchType;

import java.util.*;

import static cz.coffeerequired.api.Api.Records.PROJECT_DEBUG;

public abstract class SerializedJsonUtils {

    private static final Gson StrictnessGson = new GsonBuilder().setStrictness(Strictness.LEGACY_STRICT).create();

    public static boolean isNull(JsonElement json) {
        if (json == null) return false;
        return json.isJsonNull();
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

    public static JsonElement handle(JsonElement json, Map.Entry<String, SkriptJsonInputParser.Type> key_, boolean inSetMode) throws SerializedJsonException {
        try {
            if (json == null || json.isJsonNull()) {
                throw new SerializedJsonException("Cannot handle a null JSON element");
            }

            String key = key_.getKey();

            if (!inSetMode) {
                if (json instanceof JsonObject object) {
                    if (!object.has(key)) {
                        SkJson.warning("Json object does not contain key: %s", key);
                        return object;
                    }
                    return object.get(key);
                } else if (json instanceof JsonArray array) {
                    int index = Integer.parseInt(key);
                    if (index < 0 || index >= array.size()) {
                        SkJson.warning("Index out of bounds: %s", index);
                        return array;
                    }
                }
            } else {
                if (json instanceof JsonObject object) {
                    if (! object.has(key)) {
                        return switch (key_.getValue()) {
                            case List, Index, ListInit -> {
                                var new_ = new JsonArray();
                                object.add(key, new_);
                                yield new_;
                            }
                            case Object, Key -> {
                                var new_ = new JsonObject();
                                object.add(key, new_);
                                yield new_;
                            }
                            default -> {
                                SkJson.severe(ParserInstance.get().getNode(), "Unknown type: &e'%s'&4 for object", key_.getValue());
                                yield null;
                            }
                        };
                    } else {
                        return object.get(key);
                    }
                } else if (json instanceof JsonArray array) {
                    int index = Integer.parseInt(key);
                    if (array.isEmpty() || index >= array.size()) {
                        return switch (key_.getValue()) {
                            case List, Index -> {
                                var new_ = new JsonArray();
                                array.add(new_);
                                yield new_;
                            }
                            case Object -> { // add ListObject
                                var new_ = new JsonObject();
                                array.add(new_);
                                yield new_;
                            }
                            default -> throw new SerializedJsonException("Unknown type: %s for array".formatted(key_.getValue()));
                        };
                    } else {
                        return array.get(index);
                    }
                } else {
                    throw new SerializedJsonException("Json is not an object or array: %s".formatted(json));
                }
            }
        } catch (Exception e) {
           SkJson.exception(e, "Error handling JSON element: %s", json);
        }

        return json;
    }


    public static <T> JsonElement lazyObjectConverter(T object) {
        try {
            if (object == null) return null;
            Class<?> clazz = object.getClass();
            if (clazz.equals(String.class)) {
                try {
                    return JsonParser.parseString(object.toString());
                } catch (Exception e) {
                    return StrictnessGson.toJsonTree(object);
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


    @SuppressWarnings("unchecked")
    public static <T> T lazyJsonConverter(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        if (json.isJsonArray() || json.isJsonObject()) return (T) json;
        else if (json.isJsonPrimitive()) return (T) GsonParser.getGson().fromJson(json, Object.class);
        else return null;
    }

    public static Object[] getAsParsedArray(Object input) {
        if (!(input instanceof JsonElement current)) return new Object[]{input};
        ArrayList<Object> results = new ArrayList<>();
        if (current.isJsonPrimitive() || current.isJsonNull()) return results.toArray();
        if (current instanceof JsonArray array) {
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
            } else if (o instanceof JsonElement) {
                return true;
            } else {
                return true;
            }
        } catch (Exception e) {
            if (PROJECT_DEBUG) {
                SkJson.exception(e, "&cisValidJson, wont parse that %s", o);
            }
            return false;
        }
    }

    public static Object getFirst(JsonElement json, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (!array.isEmpty()) {
                return type.equals(SearchType.VALUE) ? array.get(0) : 0;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (!object.isEmpty()) {
                return type.equals(SearchType.VALUE)
                        ? GsonParser.fromJson(object.entrySet().iterator().next().getValue())
                        : object.keySet().iterator().next();
            }
        }
        return json;
    }

    public static Object getLast(JsonElement json, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (!array.isEmpty()) {
                return type.equals(SearchType.VALUE) ? GsonParser.fromJson(array.get(array.size() - 1)) : array.size() - 1;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (!object.isEmpty()) {
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
            if (!array.isEmpty()) {
                int index = random.nextInt(array.size());
                return type.equals(SearchType.VALUE) ? GsonParser.fromJson(array.get(index)) : index;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (!object.isEmpty()) {
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
