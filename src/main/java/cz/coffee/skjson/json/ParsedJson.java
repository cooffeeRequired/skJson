package cz.coffee.skjson.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cz.coffee.skjson.utils.Util;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Parsed json.
 */
public class ParsedJson {
    private final JsonElement input;

    /**
     * Gets json.
     *
     * @return the json
     */
    public JsonElement getJson() {
        return input;
    }

    /**
     * Instantiates a new Parsed json.
     *
     * @param input the input
     * @throws ParsedJsonException the parsed json exception
     */
    public ParsedJson(final JsonElement input) throws ParsedJsonException {
        if (input == null) throw new ParsedJsonException("Input cannot be null...");
        this.input = input;
    }


    public void removeAllByValue(LinkedList<String> keys, JsonElement value) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        JsonElement current;
        currentElements.offerLast(input);

        if (keys == null || keys.isEmpty()) {
            current = currentElements.pollLast();
            if (current == null || current.isJsonNull()) return;

            if (current.isJsonObject()) {
                JsonObject jsonObject = current.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
                entries.removeIf(entry -> entry.getValue().equals(value));
            } else if (current.isJsonArray()) {
                JsonArray jsonArray = current.getAsJsonArray();
                for (int i = jsonArray.size() - 1; i >= 0; i--) {
                    JsonElement element = jsonArray.get(i);
                    if (element.equals(value)) {
                        jsonArray.remove(i);
                    }
                }
            }
            return;
        }

        while (!keys.isEmpty()) {
            current = currentElements.pollLast();
            if (current == null || current.isJsonNull()) return;

            if (current.isJsonObject()) {
                JsonObject jsonObject = current.getAsJsonObject();
                String key = keys.pollFirst();
                currentElements.offerLast(jsonObject.get(key));
            } else if (current.isJsonArray()) {
                JsonArray jsonArray = current.getAsJsonArray();
                int index = Integer.parseInt(Objects.requireNonNull(keys.pollFirst()));
                currentElements.offerLast(jsonArray.get(index));
            }
        }

        current = currentElements.pollLast();
        if (current == null || current.isJsonNull()) return;

        if (current.isJsonObject()) {
            JsonObject jsonObject = current.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            entries.removeIf(entry -> entry.getValue().equals(value));
        } else if (current.isJsonArray()) {
            JsonArray jsonArray = current.getAsJsonArray();
            for (int i = jsonArray.size() - 1; i >= 0; i--) {
                JsonElement element = jsonArray.get(i);
                if (element.equals(value)) {
                    jsonArray.remove(i);
                }
            }
        }
    }


    /**
     * Remove by value.
     *
     * @param keys  the keys
     * @param value the value
     */
    public void removeByValue(LinkedList<String> keys, JsonElement value) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        JsonElement current;
        currentElements.offerLast(input);

        for (String key : keys) {
            current = currentElements.pollLast();
            if (current == null || current.isJsonNull()) {
                return;
            }
            if (current.isJsonObject()) {
                currentElements.offerLast(current.getAsJsonObject().get(key));
            } else if (current.isJsonArray()) {
                currentElements.offerLast(current.getAsJsonArray().get(Integer.parseInt(key)));
            }
        }

        current = currentElements.pollLast();
        if (current == null || current.isJsonNull()) return;
        if (current.isJsonObject()) {
            JsonObject jsonObject = current.getAsJsonObject();
            String found = null;
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().equals(value)) {
                    found = entry.getKey();
                    break;
                }
            }
            jsonObject.remove(found);
        } else if (current.isJsonArray()) {
            JsonArray jsonArray = current.getAsJsonArray();
            jsonArray.remove(value);
        }
    }

    /**
     * Remove by index.
     *
     * @param keys the keys
     */
    public void removeByIndex(List<String> keys) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(input);

        for (String key : keys.subList(0, keys.size() - 1)) {
            JsonElement currentElement = currentElements.pollLast();
            if (currentElement == null || currentElement.isJsonNull()) {
                return;
            }
            if (currentElement.isJsonObject()) {
                currentElements.offerLast(currentElement.getAsJsonObject().get(key));
            } else if (currentElement.isJsonArray()) {
                currentElements.offerLast(currentElement.getAsJsonArray().get(Integer.parseInt(key)));
            }
        }

        JsonElement currentElement = currentElements.pollLast();
        if (currentElement == null || currentElement.isJsonNull()) {
            return;
        }

        if (currentElement.isJsonArray()) {
            int index = Integer.parseInt(keys.get(keys.size() - 1));
            JsonArray array = currentElement.getAsJsonArray();
            if (index < 0 || index >= array.size()) {
                return;
            }
            array.remove(index);
        }
    }

    /**
     * Remove by key.
     *
     * @param keys the keys
     */
    public void removeByKey(LinkedList<String> keys) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(input);

        for (String key : keys.subList(0, keys.size() - 1)) {
            JsonElement currentElement = currentElements.pollLast();
            if (currentElement == null || currentElement.isJsonNull()) {
                return;
            }
            if (currentElement.isJsonObject()) {
                currentElements.offerLast(currentElement.getAsJsonObject().get(key));
            } else if (currentElement.isJsonArray()) {
                currentElements.offerLast(currentElement.getAsJsonArray().get(Integer.parseInt(key)));
            }
        }

        String lastKey = keys.getLast();
        JsonElement currentElement = currentElements.pollLast();
        if (currentElement == null || currentElement.isJsonNull()) {
            return;
        }

        if (currentElement.isJsonObject()) {
            currentElement.getAsJsonObject().remove(lastKey);
        } else if (currentElement.isJsonArray()) {
            JsonArray array = currentElement.getAsJsonArray();
            for (JsonElement element : array) {
                if (element.isJsonObject() && element.getAsJsonObject().has(lastKey)) {
                    array.remove(element);
                    break;
                }
            }
        }
    }

    /**
     * By key json element.
     *
     * @param keys the keys
     * @return the json element
     */
    public JsonElement byKey(LinkedList<String> keys) {
        Deque<JsonElement> currentElements = new LinkedList<>();
        currentElements.offerLast(this.input);

        for (String key : keys) {
            JsonElement currentElement = currentElements.pollLast();
            if (currentElement == null || currentElement.isJsonNull()) return null;

            if (currentElement.isJsonObject()) {
                currentElement = currentElement.getAsJsonObject().get(key);
            } else if (currentElement.isJsonArray()) {
                currentElement = currentElement.getAsJsonArray().get(Util.parseNumber(key));
            }
            if (currentElement != null) currentElements.offerLast(currentElement);
        }
        return currentElements.pollLast();
    }

    /**
     * Keys int.
     *
     * @param key the key
     * @return the int
     */
    public int keys(@NotNull String key) {
        int count = 0;
        JsonElement value;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(input);

        while ((value = elements.pollFirst()) != null) {
            if (value instanceof JsonArray) {
                for (JsonElement l : value.getAsJsonArray()) elements.offerLast(l);
            } else if (value instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                    if (entry.getKey().equals(key)) count++;
                    if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                }
            }
        }
        return count;
    }

    /**
     * Is expression boolean.
     *
     * @return the boolean
     */
    public boolean isExpression() {
        AtomicBoolean r = new AtomicBoolean(true);
        if (input.isJsonObject()) {
            input.getAsJsonObject().entrySet().forEach(entry -> {
                if (!(entry.getKey().equals("element-index") || entry.getKey().equals("element-path"))) {
                    r.set(false);
                }
            });
        }
        return r.get();
    }

    /**
     * Values int.
     *
     * @param value the value
     * @return the int
     */
    public int values(@NotNull JsonElement value) {
        int count = 0;
        JsonElement jsonElement;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(input);

        while ((jsonElement = elements.pollFirst()) != null) {
            if (jsonElement instanceof JsonArray) {
                for (JsonElement l : jsonElement.getAsJsonArray()) elements.offerLast(l);
            } else if (jsonElement instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                    if (entry.getValue().equals(value)) count++;
                    if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                }
            }
        }
        return count;
    }

    /**
     * Change key.
     *
     * @param keys   the keys
     * @param newKey the new key
     */
    public void changeKey(LinkedList<String> keys, String newKey) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(input);

        for (String key : keys.subList(0, keys.size() - 1)) {
            JsonElement currentElement = currentElements.pollLast();
            if (currentElement == null || currentElement.isJsonNull()) {
                return;
            }
            if (currentElement.isJsonObject()) {
                currentElements.offerLast(currentElement.getAsJsonObject().get(key));
            } else if (currentElement.isJsonArray()) {
                currentElements.offerLast(currentElement.getAsJsonArray().get(Integer.parseInt(key)));
            }
        }

        String lastKey = keys.getLast();
        JsonElement currentElement = currentElements.pollLast();
        if (currentElement == null || currentElement.isJsonNull()) {
            currentElement = input;
        }

        if (currentElement.isJsonObject()) {
            JsonObject jsonObject = currentElement.getAsJsonObject();
            JsonElement value = jsonObject.remove(lastKey);
            if (value != null) {
                jsonObject.add(newKey, value);
            }
        } else if (currentElement.isJsonArray()) {
            int index = Integer.parseInt(lastKey);
            JsonArray jsonArray = currentElement.getAsJsonArray();
            if (index >= 0 && index < jsonArray.size()) {
                jsonArray.remove(index);
                jsonArray.set(index, new JsonPrimitive(newKey));
            }
        }
    }

    /**
     * Is object boolean.
     *
     * @return the boolean
     */
    public boolean isObject() {
        return input.isJsonObject();
    }

    /**
     * Is null boolean.
     *
     * @return the boolean
     */
    public boolean isNull() {
        return input.isJsonNull();
    }

    /**
     * Change value.
     *
     * @param keys  the keys
     * @param value the value
     */
    public void changeValue(LinkedList<String> keys, JsonElement value) {
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        JsonElement current;
        if (keys == null) return;
        elements.offerLast(input);
        String lastKey = keys.removeLast();
        if (value == null) return;

        while ((current = elements.pollFirst()) != null) {
            for (String key : keys) {
                if (key.isEmpty()) continue;
                String sanitizedKey = key.replaceAll("->List", "");
                int index = Util.isNumber(sanitizedKey) ? Util.parseNumber(sanitizedKey) : -1;

                if (current instanceof JsonObject object) {
                    if (!object.has(sanitizedKey)) {
                        if (key.endsWith("->List")) {
                            object.add(sanitizedKey, new JsonArray());
                        } else {
                            object.add(sanitizedKey, new JsonObject());
                        }
                    }
                    current = object.get(sanitizedKey);
                } else if (current instanceof JsonArray array) {
                    if (index >= array.size()) {
                        if (key.endsWith("->List")) {
                            array.add(new JsonArray());
                        } else {
                            array.add(new JsonObject());
                        }
                    }
                    current = array.get(index);
                }
            }

            // final...
            if (current instanceof JsonObject) {
                JsonObject object = (JsonObject) current;
                String last = lastKey == null ? String.valueOf(object.size()) : lastKey;
                object.add(last, value);


            } else if (current instanceof JsonArray array) {
                int index = -1;
                for (int i = 0; i < array.size(); i++) {
                    if (i == Util.parseNumber(lastKey)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    array.set(index, value);
                } else {
                    array.remove(value);
                    array.add(value);
                }
            }
        }
    }
}
