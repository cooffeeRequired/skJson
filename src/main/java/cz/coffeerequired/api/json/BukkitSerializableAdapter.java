package cz.coffeerequired.api.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BukkitSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        // Serialize to a map structure
        Map<String, Object> map = new HashMap<>(src.serialize());
        map.put("class", src.getClass().getName()); // Add the class name for deserialization
        return context.serialize(map);
    }

    @Override
    public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Deserialize to a map structure
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = context.deserialize(json, mapType);

        // Extract the class name
        String className = (String) map.remove("class");
        if (className == null) {
            throw new JsonParseException("Missing 'class' property in JSON for ConfigurationSerializable");
        }

        try {
            // Get the class and ensure it implements ConfigurationSerializable
            Class<?> clazz = Class.forName(className);
            if (!ConfigurationSerializable.class.isAssignableFrom(clazz)) {
                throw new JsonParseException("Class " + className + " does not implement ConfigurationSerializable");
            }

            // Reconstruct the object using the map
            return (ConfigurationSerializable) clazz.getDeclaredMethod("deserialize", Map.class).invoke(null, map);
        } catch (Exception e) {
            throw new JsonParseException("Could not deserialize class: " + className, e);
        }
    }
}
