package cz.coffee.skriptgson.util;

import com.google.gson.*;

import java.util.Map;

public class JsonMap {
    public static void updateValues(JsonObject jElem, Object... value) {
        JsonElement gson = JsonParser.parseString(String.valueOf(value[0]));
        for (Map.Entry<String, JsonElement> entry : jElem.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonArray()) {
                parseJsonArray(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                updateValues(element.getAsJsonObject());
            } else if (element.isJsonPrimitive()) {
                jElem.addProperty(entry.getKey(), String.valueOf(gson));
            }
            System.out.println(entry.getKey());
        }
    }

    public static void parseJsonArray(JsonArray jArray) {
        for (int index = 0; index < jArray.size(); index++) {
            JsonElement element = jArray.get(index);
            if (element.isJsonArray()) {
                parseJsonArray(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                updateValues(element.getAsJsonObject());
            }
        }
    }
}
