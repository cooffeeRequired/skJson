package cz.coffeerequired.api.json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.skript.SkriptClass;
import cz.coffeerequired.api.skript.SkriptClassesConverter;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class Parser {

    @Getter
    static Gson gson = SkriptClass.getGson();

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getDeserializationClass(JsonObject object) throws ClassNotFoundException {
        if (object.has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            var alias = object.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY).getAsString();
            var clz = ConfigurationSerialization.getClassByAlias(alias);
            return clz != null ? (Class<T>) clz : (Class<T>) Class.forName(alias);
        }
        if (object.has(SkriptClassesConverter.SERIALIZED_TYPE_KEY)) {
            return (Class<T>) Class.forName(object.get(SkriptClassesConverter.SERIALIZED_TYPE_KEY).getAsString());
        }
        return (Class<T>) object.getClass();
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(JsonElement json) {
        switch (json) {
            case null -> {
                return null;
            }
            case JsonObject object -> {
                try {
                    Class<? extends T> cls = getDeserializationClass(object);
                    var deserialized = gson.fromJson(json, cls);
                    SkJson.debug("&e->deserialize %s %s", deserialized.getClass(), deserialized);
                    return deserialized;
                } catch (Exception e) {
                    SkJson.exception(e, "Failed to parse object");
                    return null;
                }
            }
            case JsonArray array -> { return (T) array; }
            default -> {
                var deserialized = gson.fromJson(json, Object.class);
                SkJson.debug("&e->deserialize %s %s", deserialized.getClass(), deserialized);
                return (T) deserialized;
            }
        }
    }

    public static String toString(Object src) {
        try {
            return gson.toJson(src);
        } catch (Exception e) {
            SkJson.debug("Failed to serialize object: %s", e.getMessage());
            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", src.getClass().getName());
            fallback.addProperty("_toString", String.valueOf(src));
            return fallback.toString();
        }
    }

    public static JsonElement toJson(Object src) {
        try {
            if (src instanceof String str) {
                try {
                    return JsonParser.parseString(str);
                } catch (Exception e) {
                    return gson.toJsonTree(str);
                }
            }
            var serialized = gson.toJsonTree(src);
            SkJson.debug("Serialized object: %s", serialized);
            return serialized;
        } catch (Exception e) {
            SkJson.debug("Failed to serialize object: %s", e.getMessage());
            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", src.getClass().getName());
            fallback.addProperty("_toString", String.valueOf(src));

            var serialized = gson.toJsonTree(src);
            SkJson.debug("&cFallback - Serialized object: %s", serialized);
            return serialized;
        }
    }
}
