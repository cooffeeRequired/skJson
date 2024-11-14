package cz.coffeerequired.api.json;

import com.google.gson.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.util.Map;

public class BukkitSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Object> map = src.serialize();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("class", src.getClass().getName());

        map.forEach((key, value) -> {
            JsonElement jsonElement = context.serialize(value);
            jsonObject.add(key, jsonElement);
        });

        return jsonObject;
    }

    @Override
    public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String className = jsonObject.get("class").getAsString();
        try {
            Class<?> clazz = Class.forName(className);
            if (!ConfigurationSerializable.class.isAssignableFrom(clazz)) {
                throw new JsonParseException("Class " + className + " does not implement ConfigurationSerializable");
            }

            Map<String, Object> map = context.deserialize(jsonObject, Map.class);
            return (ConfigurationSerializable) clazz.getDeclaredMethod("deserialize", Map.class).invoke(null, map);

        } catch (Exception e) {
            throw new JsonParseException("Could not deserialize class: " + className, e);
        }
    }
}


