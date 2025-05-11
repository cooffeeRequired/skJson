package cz.coffeerequired.api.json;

import ch.njol.skript.lang.parser.ParserInstance;
import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.skript.core.support.JsonSupportElements.SearchType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class JsonAccessorUtils {

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

    public static List<Integer> getArrayIndices(JsonArray array) {
       return IntStream.range(0, array.size())
                .boxed()
                .toList();
    }

    public static JsonElement handle(JsonElement json, Map.Entry<String, PathParser.Type> key_, boolean inSetMode) {
        try {
            if (json == null || json.isJsonNull()) {
                SkJson.severe(ParserInstance.get().getNode(), "Cannot handle a null JSON element");
                return json;
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
                                yield json;
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
                            default -> {
                                SkJson.severe(ParserInstance.get().getNode(), "Unknown type: &e'%s'&4 for array", key_.getValue());
                                yield json;
                            }
                        };
                    } else {
                        return array.get(index);
                    }
                } else {
                    SkJson.severe(ParserInstance.get().getNode(), "Json is not an object or array: %s", json);
                    return json;
                }
            }
        } catch (Exception e) {
           SkJson.exception(e, "Error handling JSON element: %s", json);
        }

        return json;
    }


    public static Object[] getAsParsedArray(Object input) {
        if (!(input instanceof JsonElement current)) return new Object[]{input};
        ArrayList<Object> results = new ArrayList<>();
        if (current.isJsonPrimitive() || current.isJsonNull()) return results.toArray();
        if (current instanceof JsonArray array) {
            for (JsonElement element : array) {
                if (element != null) {
                    results.add(Parser.fromJson(element));
                }
            }
        } else if (current instanceof JsonObject object) {
            for (String key : object.keySet()) {
                JsonElement element = object.get(key);
                if (element != null) {
                    results.add(Parser.fromJson(element));
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
                        ? Parser.fromJson(object.entrySet().iterator().next().getValue())
                        : object.keySet().iterator().next();
            }
        }
        return json;
    }

    public static Object getLast(JsonElement json, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (!array.isEmpty()) {
                return type.equals(SearchType.VALUE) ? Parser.fromJson(array.get(array.size() - 1)) : array.size() - 1;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (!object.isEmpty()) {
                return type.equals(SearchType.VALUE)
                        ? Parser.fromJson((JsonElement) (object.entrySet().toArray(Map.Entry[]::new)[object.keySet().size() - 1]).getValue())
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
                        ? Parser.fromJson(array.get(index))
                        : index;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.size() > index) {
                return type.equals(SearchType.VALUE)
                        ? Parser.fromJson((JsonElement) object.entrySet().toArray()[index])
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
                return type.equals(SearchType.VALUE) ? Parser.fromJson(array.get(index)) : index;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (!object.isEmpty()) {
                int index = random.nextInt(object.size());
                if (type.equals(SearchType.VALUE)) {
                    return Parser.fromJson((JsonElement) object.entrySet().toArray(Map.Entry[]::new)[index].getValue());
                } else {
                    return object.keySet().toArray(String[]::new)[index];
                }
            }
        }
        return json;
    }
}
