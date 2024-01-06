package cz.coffee.skjson.api.requests;

import com.google.gson.JsonElement;
import cz.coffee.skjson.api.http.RequestResponse;

/**
 * The interface Webhook function.
 */
public interface WebhookFunction {
    /**
     * Process.
     *
     * @param web     the web
     * @param content the content
     */
    RequestResponse process(String web, JsonElement content);

    /**
     * Process.
     *
     * @param id      the id
     * @param hex     the hex
     * @param content the content
     */
    RequestResponse process(String id, String hex, JsonElement content);
}
