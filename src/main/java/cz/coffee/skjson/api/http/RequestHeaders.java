package cz.coffee.skjson.api.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public class RequestHeaders {
    final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().setLenient().create();
    private final HttpFields headers;

    /**
     * Instantiates a new Header.
     *
     * @param headers the headers
     */
    public RequestHeaders(HttpFields headers) {
        this.headers = headers;
    }

    /**
     * Json json element.
     *
     * @return the json element
     */
    public JsonElement json() {
        return GSON.toJsonTree(headers);
    }

    /**
     * Text string.
     *
     * @return the string
     */
    public String text() {
        return GSON.toJson(headers);
    }

    /**
     * Raw http headers.
     *
     * @return the http headers
     */
    public HttpFields raw() {
        return headers;
    }
}
