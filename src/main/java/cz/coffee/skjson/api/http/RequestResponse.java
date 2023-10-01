package cz.coffee.skjson.api.http;

import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import cz.coffee.skjson.utils.Util;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static cz.coffee.skjson.api.Config.LOGGING_LEVEL;
import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public interface RequestResponse {
    /**
     * Of response.
     *
     * @param requestHeaders  the request headers
     * @param responseHeaders the response headers
     * @param uri             the uri
     * @param body            the body
     * @param statusCode      the status code
     * @return the response
     */
    static RequestResponse of(HttpFields requestHeaders, HttpFields responseHeaders, URI uri, String body, int statusCode, boolean lenient) {
        return new RequestResponse() {
            @Override
            public RequestHeaders getRequestHeaders() {
                return new RequestHeaders(requestHeaders);
            }

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
                if (statusCode >= 200 && statusCode <= 340) {
                    try {
                        if (lenient) {
                            JsonFixer fixer = new JsonFixer(body);
                            String finalBody = fixer.removeTrailingComma();
                            return JsonParser.parseString(finalBody);
                        } else {
                            return JsonParser.parseString(body);
                        }
                    } catch (Exception e) {
                        if (PROJECT_DEBUG) {
                            Util.error(true, e.getMessage());
                            if (LOGGING_LEVEL > 2) Util.enchantedError(e, e.getStackTrace(), "Invalid JSON");
                        }
                    }
                    return JsonNull.INSTANCE;
                } else {
                    if (saveIncorrect) {
                        try {
                            return body;
                        } catch (Exception e) {
                            if (PROJECT_DEBUG) Util.error(e.getMessage());
                        }
                    }
                }
                return null;
            }

            @Override
            public Optional<URL> getRequestURL() {
                try {
                    return Optional.of(uri.toURL());
                } catch (MalformedURLException exception) {
                    if (PROJECT_DEBUG) Util.enchantedError(exception, exception.getStackTrace(), "Invalid URL");
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
     * Gets request headers.
     *
     * @return the request headers
     */
    RequestHeaders getRequestHeaders();

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
