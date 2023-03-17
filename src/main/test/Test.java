import com.google.gson.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

import static cz.coffee.core.JsonUtils.checkKeys;
import static cz.coffee.core.NumberUtils.isNumber;
import static cz.coffee.core.NumberUtils.parsedNumber;
import static cz.coffee.core.Util.extractKeys;

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
 * Created: pondělí (13.03.2023)
 */

public class Test {


    private static final String KEY_SUFFIX = "->List";
    private static final JsonArray EMPTY_JSON_ARRAY = new JsonArray();
    private static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();

    private static JsonElement createMissing(JsonElement json, String key, Object o) {
        if (json.isJsonObject()) {
            String sanitizedKey = key.endsWith(KEY_SUFFIX) ? key.substring(0, key.length() - KEY_SUFFIX.length()) : key;
            if (!checkKeys(sanitizedKey, json)) {
                json.getAsJsonObject().add(sanitizedKey, key.endsWith(KEY_SUFFIX) ? EMPTY_JSON_ARRAY : EMPTY_JSON_OBJECT);
            }

        } else if (json.isJsonArray()) {
            boolean overFlow = ((int) o) > (json.getAsJsonArray().size() - 1);
            if (json.getAsJsonArray().isEmpty() || overFlow) {
                json.getAsJsonArray().add(key.endsWith(KEY_SUFFIX) ? EMPTY_JSON_ARRAY : EMPTY_JSON_OBJECT);
            }
        }
        return json;
    }

    public static void changeValue(JsonElement obj, LinkedList<String> keys, JsonPrimitive value) {
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        JsonElement current, child;
        elements.offerLast(obj);
        String lastKey = keys.removeLast();


        while ((current = elements.pollFirst()) != null) {
            for (String key : keys) {
                if (key.isEmpty()) continue;
                String sanitizedKey = key.replaceAll(KEY_SUFFIX, "");
                int index = isNumber(sanitizedKey) ? parsedNumber(sanitizedKey) : -1;

                // creating section
                if (current.isJsonArray()) {
                    JsonArray array = current.getAsJsonArray();
                    current = createMissing(array, key, index);
                } else if (current.isJsonObject()) {
                    JsonObject object = current.getAsJsonObject();
                    current = createMissing(object, key, null);
                }

                // Modding/looping section
                if (current.isJsonArray()) {
                    JsonArray array = current.getAsJsonArray();
                    if (array.isEmpty()) current = array;
                    else current = array.get(index);
                } else if (current.isJsonObject()) {
                    JsonObject object = current.getAsJsonObject();
                    child = object.get(sanitizedKey);
                    if (!(child instanceof JsonPrimitive || child instanceof JsonNull)) current = child;
                }
            }
            if (current.isJsonObject()) {
                current.getAsJsonObject().add(lastKey == null ? String.valueOf(current.getAsJsonObject().size()) : lastKey, value);
            } else if (current.isJsonArray()) {
                current.getAsJsonArray().add(value);
            }
        }
    }


    public static void main(String[] args) {
        final String string = "path[1]:C";
        LinkedList<String> keys = extractKeys(string, null, true);
        final JsonElement input = JsonParser.parseString("{'path': []}");
        System.out.println("BEFORE CHANGE: " + input);


        assert input != null;
        assert keys != null;
        changeValue(input, keys, new JsonPrimitive(false));


        System.out.println("AFTER CHANGE: " + input);



    }

}
