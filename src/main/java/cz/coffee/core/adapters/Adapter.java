package cz.coffee.core.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public interface Adapter<T> {
    String SERIALIZED_JSON_TYPE_KEY = "..";

    /**
     * <p>
     * This method will return a deserialization Object {@link T}
     *
     * @return JsonElement
     * </p>
     */
    @NotNull JsonElement toJson(T source);
    /**
     * <p>
     * This method will return a serialization {@link JsonElement} from {@link T}
     *
     * @return T
     * </p>
     */
    T fromJson(JsonObject json);
}
