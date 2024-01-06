package cz.coffee.skjson.api.http;

import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import cz.coffee.skjson.utils.LoggingUtil;
import org.eclipse.jetty.http.HttpFields;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;
import static cz.coffee.skjson.utils.LoggingUtil.enchantedError;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public interface RequestResponse {
    /**
     * Of response.
     *
     * @param responseHeaders the response headers
     * @param uri             the uri
     * @param body            the body
     * @param statusCode      the status code
     * @return the response
     */
    static RequestResponse of(HttpFields responseHeaders, URI uri, String body, int statusCode, boolean lenient) {
        return new RequestResponse() {

            @Override
            public RequestHeaders getResponseHeader() {
                return new RequestHeaders(responseHeaders);
            }

            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public Object getBodyContent(boolean saveIncorrect) {
                try {
                    if (lenient) {
                        JsonFixer fixer = new JsonFixer(body);
                        String finalBody = fixer.removeTrailingComma();
                        return JsonParser.parseString(finalBody);
                    } else {
                        return JsonParser.parseString(body);
                    }
                } catch (Exception e) {
                    if (!saveIncorrect) {
                        LoggingUtil.warn("Expecting JSON but got a String! If you don't want get this message use `save incorrect response: true`");
                        return JsonNull.INSTANCE;
                    }
                    return body;
                }
            }

            @Override
            public Optional<URL> getRequestURL() {
                try {
                    return Optional.of(uri.toURL());
                } catch (MalformedURLException exception) {
                    if (PROJECT_DEBUG) enchantedError(exception, exception.getStackTrace(), "Invalid URL");
                    return Optional.empty();
                }
            }

            @Override
            public boolean isSuccessfully() {
                return statusCode >= 200 && statusCode < 230;
            }
        };
    }

    /**
     * Gets response header.
     *
     * @return the response header
     */
    RequestHeaders getResponseHeader();

    /**
     * Gets status code.
     *
     * @return the status code
     */
    int getStatusCode();


    Object getBodyContent(boolean saveIncorrect);

    /**
     * Gets request url.
     *
     * @return the request url
     */
    Optional<URL> getRequestURL();

    /**
     * Is successfully boolean.
     *
     * @return the boolean
     */
    boolean isSuccessfully();
}
