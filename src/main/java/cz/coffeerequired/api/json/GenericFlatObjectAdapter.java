package cz.coffeerequired.api.json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import static cz.coffeerequired.api.Api.SERIALIZED_TYPE_KEY;

public class GenericFlatObjectAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {

        if (src.getClass().isAssignableFrom(String.class)) return JsonParser.parseString(src.toString());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(SERIALIZED_TYPE_KEY, src.getClass().getName());

        for (Field field : src.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);

                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                    Object value = field.get(src);
                    if (value != null) {
                        jsonObject.addProperty(field.getName(), value.toString());
                    }
                }
            } catch (InaccessibleObjectException e) {
                SkJson.logger().warning("Skipping inaccessible field: " + field.getName());
            } catch (IllegalAccessException e) {
                SkJson.logger().exception("Unable to access field: " + field.getName(), e);
            }
        }

        return jsonObject;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get(SERIALIZED_TYPE_KEY).getAsString();

        try {
            Class<?> clazz = Class.forName(className);
            @SuppressWarnings("unchecked")

            T instance = (T) clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                        JsonElement element = jsonObject.get(field.getName());
                        if (element != null) {
                            Object value = context.deserialize(element, field.getType());
                            field.set(instance, value);
                        }
                    }
                } catch (InaccessibleObjectException e) {
                    SkJson.logger().warning("Skipping inaccessible field during deserialization: " + field.getName());
                } catch (IllegalAccessException e) {
                    SkJson.logger().exception("Unable to set field: " + field.getName(), e);
                }
            }
            return instance;

        } catch (Exception e) {
            throw new JsonParseException("Could not deserialize class: " + className, e);
        }
    }
}
