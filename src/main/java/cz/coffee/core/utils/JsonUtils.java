package cz.coffee.core.utils;

import ch.njol.skript.Skript;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static cz.coffee.core.utils.AdapterUtils.assignFrom;
import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.NumberUtils.isNumber;
import static cz.coffee.core.utils.NumberUtils.parsedNumber;
import static cz.coffee.core.utils.Util.arrayIsSafe;
import static cz.coffee.core.utils.Util.jsonToObject;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Saturday (3/4/2023)
 */
public class JsonUtils {
    public static JsonElement convert(Object object) {
         try {
             if (object == null) return null;
             Class<?> clazz = object.getClass();
             if (clazz.equals(String.class)) {
                 if (((String) object).contains(" ")) {
                     // With whitespace
                     return new Gson().toJsonTree(object, object.getClass());
                 } else {
                     // Primitive
                     return JsonParser.parseString((String) object);
                 }
             }
             if (clazz.equals(Integer.class))
                 return new JsonPrimitive((Integer) object);
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
    public static int countKeys(@NotNull String key, @NotNull JsonElement json) {
        int count = 0;
        JsonElement value;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(json);

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
    public static int countValues(@NotNull JsonElement value, @NotNull JsonElement json) {
        int count = 0;
        JsonElement jsonElement;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(json);

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
    private static final String KEY_SUFFIX = "->List";
    public static void changeValue(JsonElement obj, LinkedList<String> keys, Object value) {
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        JsonElement current;
        elements.offerLast(obj);
        String lastKey = keys.removeLast();
        if (value == null) return;
        JsonElement parsedValue = parseItem(value, null, null, value.getClass());
        if (parsedValue == null) return;


        while ((current = elements.pollFirst()) != null) {
            for (String key : keys) {
                if (key.isEmpty()) continue;
                String sanitizedKey = key.replaceAll(KEY_SUFFIX, "");
                int index = isNumber(sanitizedKey) ? parsedNumber(sanitizedKey) : -1;
                //SkJson.console("&cKey: &f" + key + "  &bSanitized: &f" + sanitizedKey + "  &eMAIN: &f" + current);

                if (current instanceof JsonObject object) {
                    if (!object.has(sanitizedKey)) {
                        if (key.endsWith(KEY_SUFFIX)) {
                            object.add(sanitizedKey, new JsonArray());
                        } else {
                            object.add(sanitizedKey, new JsonObject());
                        }
                    }
                    current = object.get(sanitizedKey);
                } else if (current instanceof JsonArray array) {
                    if (index >= array.size()) {
                        if (key.endsWith(KEY_SUFFIX)) {
                            array.add(new JsonArray());
                        } else {
                            array.add(new JsonObject());
                        }
                    }
                    current = array.get(index);
                }
            }
            // final...
            if (current instanceof JsonObject object) {
                object.remove(lastKey);
                object.add(lastKey == null ? String.valueOf(object.size()) : lastKey, parsedValue);
            } else if (current instanceof JsonArray array) {
                array.remove(parsedValue);
                array.add(parsedValue);
            }
        }
    }
    public static void changeKey(JsonElement obj, LinkedList<String> keys, String newKey) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(obj);

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
            currentElement = obj;
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
    private static boolean isNewArray(final String value) {
        return value.endsWith("->List");
    }

    @SuppressWarnings("unused")
    public static void addValue(JsonElement obj, LinkedList<String> path, String definedKey, Object value) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(obj);

        for (String key : path.subList(0, path.size() - 1)) {
            JsonElement currentElement = currentElements.pollLast();
            if (currentElement == null || currentElement.isJsonNull()) {
                return;
            }
            if (currentElement.isJsonObject()) {
                JsonElement childElement = currentElement.getAsJsonObject().get(key);
                if (childElement == null || childElement.isJsonNull()) {
                    childElement = isNewArray(key) ? new JsonArray() : new JsonObject();
                    currentElement.getAsJsonObject().add(key.endsWith("->List") ? key.replaceAll("->List", "") : key, childElement);
                }
                currentElements.offerLast(childElement);
            } else if (currentElement.isJsonArray()) {
                JsonElement childElement = null;
                if (currentElement.getAsJsonArray().size() < parsedNumber(key)) {
                    Skript.error("Unsupported input for square bracket " + parsedNumber(key) + " array size is " + currentElement.getAsJsonArray().size());
                    return;
                }
                if (arrayIsSafe(currentElement.getAsJsonArray(), parsedNumber(key)))
                    childElement = currentElement.getAsJsonArray().get(parsedNumber(key));
                if (childElement == null || childElement.isJsonNull()) {
                    childElement = isNewArray(key) ? new JsonArray() : new JsonObject();
                    currentElement.getAsJsonArray().add(childElement);
                }
                currentElements.offerLast(childElement);
            }
        }

        String sanitizedKey = path.getLast().endsWith("->List") ? path.getLast().replaceAll("->List", "") : path.getLast();

        // Add the final value to the last key in the list
        JsonElement currentElement = currentElements.pollLast();
        if (currentElement == null || currentElement.isJsonNull()) {
            return;
        }
        if (currentElement.isJsonObject()) {
            System.out.println(currentElement);
            currentElement.getAsJsonObject().add(definedKey, parseItem(value, null, null, value.getClass()));
        } else if (currentElement.isJsonArray()) {
            int index = -1;
            if (isNumber(sanitizedKey)) {
                index = parsedNumber(sanitizedKey);
            }
            if (index >= currentElement.getAsJsonArray().size()) {
                for (int i = currentElement.getAsJsonArray().size(); i < index; i++) {
                    currentElement.getAsJsonArray().add(JsonNull.INSTANCE);
                }
                currentElement.getAsJsonArray().add(parseItem(value, null, null, value.getClass()));
            } else {
                currentElement.getAsJsonArray().set(index, parseItem(value, null, null, value.getClass()));
            }
        }
    }
    public static void removeByValue(JsonElement obj, LinkedList<String> keys, JsonElement value) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        JsonElement current;
        currentElements.offerLast(obj);

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
    public static void removeByIndex(JsonElement obj, List<String> keys) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(obj);

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
    public static void removeByKey(JsonElement obj, LinkedList<String> keys) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        currentElements.offerLast(obj);

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
    public static JsonElement getByKey(JsonElement obj, LinkedList<String> keys) {
        Deque<JsonElement> currentElements = new ConcurrentLinkedDeque<>();
        if (obj != null) currentElements.offerLast(obj);

        for (String key : keys) {
            JsonElement currentElement = currentElements.pollLast();
            if (currentElement == null || currentElement.isJsonNull()) {
                return null;
            }
            if (currentElement.isJsonObject()) {
                currentElement = currentElement.getAsJsonObject().get(key);
            } else if (currentElement.isJsonArray()) {
                if (arrayIsSafe(currentElement.getAsJsonArray(), parsedNumber(key))) {
                    currentElement = currentElement.getAsJsonArray().get(Integer.parseInt(key));
                } else {
                    return null;
                }
            }
            if (currentElement != null) currentElements.offerLast(currentElement);
        }

        return currentElements.pollLast();
    }
    public static LinkedList<Object> getNestedElements(JsonElement current) {
         LinkedList<Object> results = new LinkedList<>();
         if (current == null || current.isJsonPrimitive() || current.isJsonNull()) return null;
         if (current.isJsonObject())
         {
             ((JsonObject) current).entrySet().forEach(entry -> {
                 JsonElement value = entry.getValue();
                 if (value != null) {
                     Object assign = assignFrom(value);
                     results.add(value.isJsonPrimitive() ? jsonToObject(value) : assign == null ? value : assign);
                 }
             });
         }
         else if (current.isJsonArray())
         {
             ((JsonArray) current).forEach(value -> {
                 if (value != null) {
                     Object assign = assignFrom(value);
                     results.add(value.isJsonPrimitive() ? jsonToObject(value) : assign == null ? value : assign);
                 }
             });
         }
         return results;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkKeys(@NotNull String key, @NotNull JsonElement json) {
        boolean found = false;
        JsonElement value;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(json);

        while ((value = elements.pollFirst()) != null) {
            if (found) return true;
            if (value instanceof JsonArray) {
                for (JsonElement l : value.getAsJsonArray()) elements.offerLast(l);
            } else if (value instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                    if (entry.getKey().equals(key)) found = true;
                    if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                }
            }
        }
        return found;
    }
    public static boolean checkValues(@NotNull JsonElement value, @NotNull JsonElement json) {
         boolean found = false;
        JsonElement jsonElement;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(json);

        while ((jsonElement = elements.pollFirst()) != null) {
            if (found) return true;
            if (jsonElement instanceof JsonArray) {
                for (JsonElement l : jsonElement.getAsJsonArray()) {
                    if (l.equals(value)) found = true;
                    elements.offerLast(l);
                }
            } else if (jsonElement instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                    if (entry.getValue().equals(value)) found = true;
                    if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                }
            }
        }
        return found;
    }
}
