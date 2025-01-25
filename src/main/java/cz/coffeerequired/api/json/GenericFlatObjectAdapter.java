package cz.coffeerequired.api.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import cz.coffeerequired.SkJson;

import java.lang.reflect.Type;

public class GenericFlatObjectAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        if (src instanceof String) {
            try {
                return JsonParser.parseString(src.toString());
            } catch (JsonSyntaxException e) {
                SkJson.logger().exception("Unable to serialize string: " + src, e);
                return null;
            }
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("class", src.getClass().getName());

        Type type = TypeToken.get(src.getClass()).getType();
        JsonElement element = context.serialize(src, type);
        jsonObject.add("data", element);

        return jsonObject;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String className = jsonObject.get("class").getAsString();

        try {
            Class<?> clazz = Class.forName(className);
            Type type = TypeToken.get(clazz).getType();
            JsonElement element = jsonObject.get("data");

            @SuppressWarnings("unchecked")
            T instance = (T) context.deserialize(element, type);

            return instance;

        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Could not deserialize class: " + className, e);
        }
    }
}
