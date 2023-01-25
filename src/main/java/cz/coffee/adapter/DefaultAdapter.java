package cz.coffee.adapter;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * The class represent a serializer/deserializer for another object than @ConfigurationSerializablee
 * the {@link DefaultAdapter} interface javadocs.
 */
public interface DefaultAdapter<T> {

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

    /**
     * <p>
     * This method will check what type of serialized Json contain. {@link JsonElement}
     *
     * @return Clazz
     * </p>
     */
    Class<? extends T> typeOf(JsonObject json);
}


