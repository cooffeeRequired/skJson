package cz.coffeerequired.api.skript;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class BukkitSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    final Type objectStringMapType = new TypeToken<Map<String, Object>>() {}.getType();

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
        map.putAll(src.serialize());
        return context.serialize(map, objectStringMapType);
    }

    @Override
    public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Map<String, Object> map = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            
            if (value.isJsonNull()) {
                map.put(key, null);
            } else if (value.isJsonArray()) {
                java.util.ArrayList<Object> list = new java.util.ArrayList<>();
                for (JsonElement element : value.getAsJsonArray()) {
                    if (element.isJsonObject() && element.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                        list.add(deserialize(element, null, context));
                    } else {
                        list.add(deserializeValue(element, context));
                    }
                }
                map.put(key, list);
            } else if (value.isJsonObject()) {
                if (value.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    map.put(key, deserialize(value, null, context));
                } else {
                    Map<String, Object> nestedMap = new LinkedHashMap<>();
                    for (Map.Entry<String, JsonElement> nestedEntry : value.getAsJsonObject().entrySet()) {
                        nestedMap.put(nestedEntry.getKey(), deserializeValue(nestedEntry.getValue(), context));
                    }
                    map.put(key, nestedMap);
                }
            } else {
                map.put(key, deserializeValue(value, context));
            }
        }

        try {
            return ConfigurationSerialization.deserializeObject(map);
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize object: " + e.getMessage(), e);
        }
    }

    private Object deserializeValue(JsonElement element, JsonDeserializationContext context) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                Number num = primitive.getAsNumber();
                if (num.doubleValue() == num.intValue()) {
                    return num.intValue();
                } else {
                    return num.doubleValue();
                }
            } else {
                return primitive.getAsString();
            }
        } else if (element.isJsonObject() && element.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            return deserialize(element, null, context);
        }
        return null;
    }
}
