package cz.coffeerequired.api.json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class BukkitSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("class", src.getClass().getName());

        for (Field field : src.getClass().getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                try {
                    Object value = field.get(src);
                    if (value != null) {
                        jsonObject.add(field.getName(), context.serialize(value));
                    }
                } catch (IllegalAccessException e) {
                    SkJson.logger().exception(e.getMessage(), e);
                }
            }
        }

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

            ConfigurationSerializable instance = (ConfigurationSerializable) clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isPublic(field.getModifiers())) {
                    JsonElement element = jsonObject.get(field.getName());
                    if (element != null) {
                        Object value = context.deserialize(element, field.getType());
                        field.set(instance, value);
                    }
                }
            }
            return instance;

        } catch (Exception e) {
            throw new JsonParseException("Could not deserialize class: " + className, e);
        }
    }
}
