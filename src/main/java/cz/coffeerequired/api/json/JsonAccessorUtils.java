package cz.coffeerequired.api.json;

import ch.njol.skript.lang.parser.ParserInstance;
import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.skript.core.support.JsonSupportElements.SearchType;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public abstract class JsonAccessorUtils {

    public static boolean isNull(JsonElement json) {
        if (json == null) return false;
        return json.isJsonNull();
    }

    public static <E> Deque<E> listToDeque(ArrayList<E> list) {
        return new ArrayDeque<>(list);
    }

    public static @Nullable Integer parseArrayIndex(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        int len = key.length();
        for (int i = 0; i < len; i++) {
            char c = key.charAt(i);
            if (c < '0' || c > '9') {
                return null;
            }
        }
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Number isNumeric(Object obj) {
        if (obj instanceof Number number) {
            return number;
        }
        if (obj instanceof String str) {
            Integer index = parseArrayIndex(str);
            if (index != null) {
                return index;
            }
        }
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

    public static @Nullable JsonElement navigate(JsonElement json, Map.Entry<String, PathParser.Type> key_) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        String key = key_.getKey();
        if (json instanceof JsonObject object) {
            if (!object.has(key)) {
                return null;
            }
            return object.get(key);
        }
        if (json instanceof JsonArray array) {
            Integer index = parseArrayIndex(key);
            if (index == null) {
                Number numeric = isNumeric(key);
                if (numeric == null) {
                    return null;
                }
                index = numeric.intValue();
            }
            if (index < 0 || index >= array.size()) {
                return null;
            }
            return array.get(index);
        }
        return null;
    }

    public static boolean pathExists(JsonElement json, java.util.List<Map.Entry<String, PathParser.Type>> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return json != null && !json.isJsonNull();
        }
        JsonElement current = json;
        for (var token : tokens) {
            current = navigate(current, token);
            if (current == null) {
                return false;
            }
        }
        return true;
    }

    public static @Nullable JsonElement resolve(JsonElement root, List<Map.Entry<String, PathParser.Type>> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return root;
        }
        JsonElement current = root;
        for (var token : tokens) {
            current = navigate(current, token);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public static @Nullable Object resolveParsed(JsonElement root, List<Map.Entry<String, PathParser.Type>> tokens) {
        JsonElement element = resolve(root, tokens);
        return element == null ? null : Parser.fromJson(element);
    }

    public static JsonElement handle(JsonElement json, Map.Entry<String, PathParser.Type> key_, boolean inSetMode) {
        try {
            if (json == null || json.isJsonNull()) {
                SkJson.severe(ParserInstance.get().getNode(), "Cannot handle a null JSON element");
                return json;
            }

            if (!inSetMode) {
                JsonElement next = navigate(json, key_);
                if (next == null) {
                    SkJson.warning("Json path segment not found: %s", key_.getKey());
                    return json;
                }
                return next;
            }

            String key = key_.getKey();

            {
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
        if (current.isJsonPrimitive() || current.isJsonNull()) {
            return new Object[0];
        }
        if (current instanceof JsonArray array) {
            Object[] results = new Object[array.size()];
            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                results[i] = element != null ? Parser.fromJson(element) : null;
            }
            return results;
        }
        if (current instanceof JsonObject object) {
            Object[] results = new Object[object.size()];
            int i = 0;
            for (String key : object.keySet()) {
                JsonElement element = object.get(key);
                results[i++] = element != null ? Parser.fromJson(element) : null;
            }
            return results;
        }
        return new Object[0];
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
                if (type.equals(SearchType.VALUE)) {
                    Map.Entry<String, JsonElement> last = null;
                    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                        last = entry;
                    }
                    return last == null ? json : Parser.fromJson(last.getValue());
                }
                String lastKey = null;
                for (String objectKey : object.keySet()) {
                    lastKey = objectKey;
                }
                return lastKey == null ? json : lastKey;
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
                if (type.equals(SearchType.VALUE)) {
                    int i = 0;
                    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                        if (i++ == index) {
                            return Parser.fromJson(entry.getValue());
                        }
                    }
                } else {
                    int i = 0;
                    for (String objectKey : object.keySet()) {
                        if (i++ == index) {
                            return objectKey;
                        }
                    }
                }
            }
        }
        return json;
    }

    public static Object getRandom(JsonElement json, SearchType type) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (!array.isEmpty()) {
                int pick = ThreadLocalRandom.current().nextInt(array.size());
                return type.equals(SearchType.VALUE) ? Parser.fromJson(array.get(pick)) : pick;
            }
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            int size = object.size();
            if (size > 0) {
                int pick = ThreadLocalRandom.current().nextInt(size);
                if (type.equals(SearchType.VALUE)) {
                    int i = 0;
                    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                        if (i++ == pick) {
                            return Parser.fromJson(entry.getValue());
                        }
                    }
                } else {
                    int i = 0;
                    for (String objectKey : object.keySet()) {
                        if (i++ == pick) {
                            return objectKey;
                        }
                    }
                }
            }
        }
        return json;
    }
}
