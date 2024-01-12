package cz.coffee.skjson.api;

import com.google.gson.*;
import cz.coffee.skjson.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DynamicObjectSerializer<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        Class<?> clazz = src.getClass();
        for (Field field : getAllFields(clazz)) {
            try {
                field.setAccessible(true);
                Object value = field.get(src);
                jsonObject.add(field.getName(), context.serialize(value));
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return jsonObject;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jsonObject = json.getAsJsonObject();
        Class<?> clazz = (Class<?>) typeOfT;
        T instance;

        try {
            Constructor<? extends T> constructor = (Constructor<? extends T>) clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();

            for (Field field : getAllFields(clazz)) {
                if (jsonObject.has(field.getName())) {
                    field.setAccessible(true);
                    field.set(instance, context.deserialize(jsonObject.get(field.getName()), field.getType()));
                }
            }
        } catch (ReflectiveOperationException e) {
            Logger.error(e);
            return null;
        }

        return instance;
    }

    private Field[] getAllFields(Class<?> clazz) {
        Map<String, Field> fieldsMap = new HashMap<>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!fieldsMap.containsKey(field.getName())) {
                    fieldsMap.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fieldsMap.values().toArray(new Field[0]);
    }
}
