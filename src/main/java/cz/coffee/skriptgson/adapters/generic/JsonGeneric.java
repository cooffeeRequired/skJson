package cz.coffee.skriptgson.adapters.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.jetbrains.annotations.NotNull;

public class JsonGeneric implements JsonGenericAdapter<Object> {
    @Override
    public @NotNull JsonElement toJson(Object object) {
        return new JsonNull();
    }

    @Override
    public Object fromJson(JsonElement json) {
        return null;
    }

    @Override
    public Class<?> typeOf(JsonElement json) {
        String potentialClass = json.getAsJsonObject().get(GSON_GENERIC_ADAPTER_KEY).getAsString();
        try {
            return Class.forName(potentialClass);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
