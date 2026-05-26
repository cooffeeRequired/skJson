package cz.coffeerequired.api.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonMerge {

    private JsonMerge() {}

    public static JsonElement merge(JsonElement target, JsonElement source, boolean deep) {
        if (target == null || target.isJsonNull()) {
            return source == null ? target : source.deepCopy();
        }
        if (source == null || source.isJsonNull()) {
            return target.deepCopy();
        }
        if (target.isJsonObject() && source.isJsonObject()) {
            JsonObject result = target.getAsJsonObject().deepCopy();
            JsonObject from = source.getAsJsonObject();
            for (var entry : from.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (deep && result.has(key)) {
                    JsonElement existing = result.get(key);
                    if (existing.isJsonObject() && value.isJsonObject()) {
                        result.add(key, merge(existing, value, true));
                        continue;
                    }
                    if (existing.isJsonArray() && value.isJsonArray()) {
                        JsonArray merged = existing.getAsJsonArray().deepCopy();
                        value.getAsJsonArray().forEach(merged::add);
                        result.add(key, merged);
                        continue;
                    }
                }
                result.add(key, value.deepCopy());
            }
            return result;
        }
        if (target.isJsonArray() && source.isJsonArray()) {
            JsonArray result = target.getAsJsonArray().deepCopy();
            source.getAsJsonArray().forEach(result::add);
            return result;
        }
        return source.deepCopy();
    }
}
