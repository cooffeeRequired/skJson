package cz.coffeerequired.api.json;

import ch.njol.skript.lang.parser.ParserInstance;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.skript.core.expressions.ExprSortJson;
import lombok.Getter;

import java.util.*;

import static cz.coffeerequired.api.json.JsonAccessorUtils.handle;

@SuppressWarnings("unused")
public class JsonAccessor {
    @Getter
    private final JsonElement json;

    public changer changer;
    public counter counter;
    public remover remover;
    public searcher searcher;

    public JsonAccessor(JsonElement json) {
        if (JsonAccessorUtils.isNull(json)) {
            SkJson.severe(ParserInstance.get().getNode(), "Json cannot be null");
        }
        this.json = json;
        this.changer = new changer(json);
        this.counter = new counter(json);
        this.remover = new remover(json);
        this.searcher = new searcher(json);
    }


    public JsonElement sort(ExprSortJson.SortType type) {
        if (type.equals(ExprSortJson.SortType.BY_KEY_ASC) || type.equals(ExprSortJson.SortType.BY_KEY_DESC)) {
            if (json instanceof JsonObject jsonObject) {
                var keys = new ArrayList<>(jsonObject.keySet());
                if (type.equals(ExprSortJson.SortType.BY_KEY_ASC)) keys.sort(Comparator.naturalOrder());
                else keys.sort(Comparator.reverseOrder());
                var result = new JsonObject();
                for (String key : keys) {
                    result.add(key, jsonObject.get(key));
                }
                return result;
            } else if (json instanceof JsonArray jsonArray) {
                SkJson.severe(ParserInstance.get().getNode(), "Cannot sort Json Arrays by keys, use by value instead");
                return null;
            }
        } else if (type.equals(ExprSortJson.SortType.BY_VALUE_ASC) || type.equals(ExprSortJson.SortType.BY_VALUE_DESC)) {
            if (json instanceof JsonObject jsonObject) {
                var entries = new ArrayList<>(jsonObject.entrySet());
                if (type.equals(ExprSortJson.SortType.BY_VALUE_ASC)) {
                    entries.sort(JsonValueComparator.byValueAscending());
                } else {
                    entries.sort(JsonValueComparator.byValueDescending());
                }

                var result = new JsonObject();
                for (var entry : entries) {
                    result.add(entry.getKey(), entry.getValue());
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "SerializedJson{" + "json=" + json + '}';
    }

    public record changer(JsonElement json) {
        public void add(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement value) {
            var current = getCurrentWithoutRemovingKey(tokens, json, true);
            if (current instanceof JsonArray array) {
                array.add(value);
            } else {
                SkJson.severe(ParserInstance.get().getNode(),"Changer issue. Trying to add value %s to %s. But &e'add'&c can be done only in Json Arrays", value, current);
            }
        }

        public void key(ArrayList<Map.Entry<String, PathParser.Type>> tokens, String newKey) {
            var c = getCurrent(tokens, json);
            JsonElement current = (JsonElement) c.get(1);
            var key = (String) c.getFirst();

            if (!current.isJsonObject()) {
                SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to change key %s to %s. But &e'key'&c can be done only in Json Objects", key, newKey);
                return;
            } else {
                ((JsonObject) current).add(newKey, current.getAsJsonObject().get(key));
                ((JsonObject) current).remove(key);
            }
        }

        public void value(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement value) {
            value(tokens, value, true);
        }

        public void value(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement value, boolean removeKey) {
            var deque = JsonAccessorUtils.listToDeque(tokens);
            var temp = removeKey ? deque.removeLast() : deque.getLast();
            var key = temp.getKey();
            JsonElement current = json;
            Map.Entry<String, PathParser.Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, true);
            }

            Number index;


            if (current instanceof JsonObject object) {
                object.add(key, value);
            } else if (current instanceof JsonArray array) {
                if ((index = JsonAccessorUtils.isNumeric(key)) != null) {
                    if (!current.isJsonArray()) {
                        SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to change index %s. But &e'value'&c can be done only in Json Arrays", key);
                        return;
                    }       

                    if (array.isEmpty()) {
                        array.add(value);
                    } else if (array.size() <= index.intValue()) {
                        array.add(value);
                    } else {
                        array.set(index.intValue(), value);
                    }
                }
            }
        }
    }

    public record counter(JsonElement json) {
        public int keys(String key) {
            int count = 0;
            Deque<JsonElement> deque = new ArrayDeque<>();
            deque.add(json);
            JsonElement current;

            while ((current = deque.pollFirst()) != null) {
                if (current instanceof JsonObject jsonObject) {
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        if (entry.getKey().equals(key)) count++;
                        if (!entry.getValue().isJsonPrimitive()) deque.offerLast(entry.getValue());
                    }
                } else if (current instanceof JsonArray jsonArray) {
                    for (JsonElement element : jsonArray) if (!element.isJsonPrimitive()) deque.offerLast(element);
                }
            }
            return count;
        }

        public <V> int values(V object) {
            int count = 0;
            Deque<JsonElement> deque = new ArrayDeque<>();
            deque.add(json);
            JsonElement current;

            JsonElement value = Parser.toJson(object);

            while ((current = deque.pollFirst()) != null) {
                if (current instanceof JsonObject jsonObject) {
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        if (entry.getValue().equals(value)) count++;
                        if (!entry.getValue().isJsonPrimitive()) deque.offerLast(entry.getValue());
                    }
                } else if (current instanceof JsonArray jsonArray) {
                    for (JsonElement element : jsonArray)
                        if (!element.isJsonPrimitive()) deque.offerLast(element);
                        else if (element.equals(value)) count++;
                }
            }

            return count;
        }
    }

    public record remover(JsonElement json) {
        public void byKey(ArrayList<Map.Entry<String, PathParser.Type>> tokens) {
            var c = getCurrent(tokens, json, false);
            JsonElement current = (JsonElement) c.get(1);
            var key = (String) c.getFirst();

            if (current instanceof JsonObject object) {
                object.remove(key);
            } else if (current instanceof JsonArray array) {
                Number num;
                if ((num = JsonAccessorUtils.isNumeric(key)) != null) {
                    if (array.isEmpty()) {
                        SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove index %s. But array is empty", key);
                        return;
                    } else if (array.size() <= num.intValue()) {
                        SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove index %s. But array size is %s", key, array.size());
                        return;
                    } else {
                        array.remove(num.intValue());
                    }
                } else {
                    SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove key %s. But key is not a number", key);
                }
            } else {
                SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove key %s. But &e'remove'&c can be done only in Json Objects or Json Arrays", key);
            }
        }

        public void reset(ArrayList<Map.Entry<String, PathParser.Type>> tokens) {
            JsonElement current = tokens == null ? json : getCurrentWithoutRemovingKey(tokens, json);
            if (current instanceof JsonObject jsonObject) {
                var deepCopy = jsonObject.deepCopy();
                deepCopy.entrySet().forEach(entry -> jsonObject.remove(entry.getKey()));
            } else if (current instanceof JsonArray jsonArray) {
                var deepCopy = jsonArray.deepCopy();
                deepCopy.forEach(jsonArray::remove);
            }
        }

        public void allByValue(ArrayList<Map.Entry<String, PathParser.Type>> tokens, Object value) {
            JsonElement current = tokens == null ? json : getCurrentWithoutRemovingKey(tokens, json);
            JsonElement valueElement = Parser.toJson(value);

            if (current instanceof JsonArray array) {
                var deepCopy = array.deepCopy();

                for (JsonElement element : deepCopy) {
                    if (element.equals(valueElement)) array.remove(element);
                }
            } else if (current instanceof JsonObject jsonObject) {
                var deepCopy = jsonObject.deepCopy();
                for (Map.Entry<String, JsonElement> entry : deepCopy.entrySet()) {
                    if (entry.getValue().equals(valueElement)) jsonObject.remove(entry.getKey());
                }
            } else {
                SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove value %s. But &e'remove'&c can be done only in Json Arrays or Json Objects", value);
            }
        }

        public void byIndex(ArrayList<Map.Entry<String, PathParser.Type>> tokens) {
            var c = getCurrent(tokens, json);
            JsonElement current = (JsonElement) c.get(1);
            var key = (String) c.getFirst();

            Number index;

            if ((index = JsonAccessorUtils.isNumeric(key)) != null) {
                if (!current.isJsonArray()) {
                    SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove index %s. But &e'remove'&c can be done only in Json Arrays", key);
                    return;
                }
                ((JsonArray) current).remove(index.intValue());
            }
        }

        public void byValue(ArrayList<Map.Entry<String, PathParser.Type>> tokens, Object value) {
            var deque = JsonAccessorUtils.listToDeque(tokens);
            JsonElement current = getCurrentWithoutRemovingKey(tokens, json);

            JsonElement valueElement = Parser.toJson(value);

            if (current instanceof JsonArray array) {
                array.remove(valueElement);
            } else if (current instanceof JsonObject jsonObject) {
                var deepCopy = jsonObject.deepCopy();
                for (Map.Entry<String, JsonElement> entry : deepCopy.entrySet()) {
                    if (entry.getValue().equals(valueElement)) jsonObject.remove(entry.getKey());
                }
            } else {
                SkJson.severe(ParserInstance.get().getNode(), "Changer issue. Trying to remove value %s. But &e'remove'&c can be done only in Json Arrays or Json Objects", value);
            }
        }
    }

    private static List<?> getCurrent(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement json, boolean setMode) {
        var deque = JsonAccessorUtils.listToDeque(tokens);
        var key = deque.removeLast().getKey();
        JsonElement current = json;
        Map.Entry<String, PathParser.Type> currentKey;

        while ((currentKey = deque.pollFirst()) != null) {
            current = handle(current, currentKey, setMode);
        }
        return List.of(key, current);
    }

    private static List<?> getCurrent(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement json) {
        return getCurrent(tokens, json, true);
    }

    private static JsonElement getCurrentWithoutRemovingKey(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement json, boolean setMode) {
        var deque = JsonAccessorUtils.listToDeque(tokens);
        JsonElement current = json;
        Map.Entry<String, PathParser.Type> currentKey;
        while ((currentKey = deque.pollFirst()) != null) {
            current = handle(current, currentKey, setMode);
        }
        return current;
    }

    private static JsonElement getCurrentWithoutRemovingKey(ArrayList<Map.Entry<String, PathParser.Type>> tokens, JsonElement json) {
        return getCurrentWithoutRemovingKey(tokens, json, false);
    }

    public record searcher(JsonElement json) {
        public Object keyOrIndex(ArrayList<Map.Entry<String, PathParser.Type>> tokens) {
            var c = getCurrent(tokens, json);
            JsonElement current = (JsonElement) c.get(1);
            var key = (String) c.getFirst();

            try {
                if (current instanceof JsonObject object) {
                    var searched = object.get(key);
                    if (searched == null) {
                        SkJson.warning("&l&c%s: key '%s' not found", "Search issue",  PathParser.getPathFromTokens(tokens));
                        return null;
                    }
                    return Parser.fromJson(searched);
                }

                if (current instanceof JsonArray array) {
                    Number index = JsonAccessorUtils.isNumeric(key);
                    if (index != null && index.intValue() <= array.size()) {
                        return Parser.fromJson(array.get(index.intValue()));
                    } else {
                        SkJson.warning("&l&c%s: index '%s' not found", "Search issue",  PathParser.getPathFromTokens(tokens));
                        return null;
                    }
                }
            } catch (Exception e) {
                SkJson.warning("&l&c%s: %s", "Search issue", e.getMessage());
            }
            return null;
        }
    }
}
