package cz.coffee.skriptgson.adapters.generic;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * The class represent a serializer/deserializer for another object than @ConfigurationSerializable
 * the {@link JsonGenericAdapter} interface javadocs.
 */

public interface JsonGenericAdapter<T> {

    String GSON_GENERIC_ADAPTER_KEY = "??";

    /**
     * <p>
     * This method will return a deserialization Object {@link T}
     *
     * @return JsonElement
     * </p>
     */
    @NotNull JsonElement toJson(T object);

    /**
     * <p>
     * This method will return a serialization {@link JsonElement} from {@link T}
     *
     * @return T
     * </p>
     */
    T fromJson(JsonElement json);


    /**
     * <p>
     * This method will check what type of serialized Json contain. {@link JsonElement}
     *
     * @return Clazz
     * </p>
     */
    Class<? extends T> typeOf(JsonElement json);

}