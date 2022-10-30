package cz.coffee.skriptgson.util;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonMap {
    public static List<String> getValues(JsonObject jElem) {
        List<String> counts = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jElem.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonArray()) {
                parseJsonArray(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                getValues(element.getAsJsonObject());
            }
            counts.add(entry.getKey());
        }
        return counts;
    }

    public static void parseJsonArray(JsonArray jArray) {
        for (int index = 0; index < jArray.size(); index++) {
            JsonElement element = jArray.get(index);
            if (element.isJsonArray()) {
                parseJsonArray(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                getValues(element.getAsJsonObject());
            }
        }
    }
}
