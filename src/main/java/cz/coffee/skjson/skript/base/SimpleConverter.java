package cz.coffee.skjson.skript.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * The interface Simple converter.
 *
 * @param <T> the type parameter
 */
public interface SimpleConverter<T> {
    /**
     * The constant SERIALIZED_JSON_TYPE_KEY.
     */
    String SERIALIZED_JSON_TYPE_KEY = "..";

    /**
     * To json json element.
     *
     * @param source the source
     * @return the json element
     */
    @NotNull JsonElement toJson(T source) throws Exception;

    /**
     * From jsom t.
     *
     * @param json the json
     * @return the t
     */
    T fromJson(JsonObject json);

}
