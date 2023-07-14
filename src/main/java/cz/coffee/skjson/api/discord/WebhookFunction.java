package cz.coffee.skjson.api.discord;

import com.google.gson.JsonElement;
import cz.coffee.skjson.api.Update.HttpWrapper;

import java.util.Map;

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
    HttpWrapper.Response process(String web, JsonElement content);

    /**
     * Process.
     *
     * @param id      the id
     * @param hex     the hex
     * @param content the content
     */
    HttpWrapper.Response process(String id, String hex, JsonElement content);
}
