package cz.coffee.skjson.api.discord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.Update.HttpWrapper;
import cz.coffee.skjson.skript.requests.Requests;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Webhook.
 */
public class Webhook {

    /**
     * The enum Web hook type.
     */
    public enum WebHookType {
        /**
         * Discord web hook type.
         */
        DISCORD,
        /**
         * Web web hook type.
         */
        WEB
    }

    private HttpWrapper http;

    private final WebHookType webHookType;

    /**
     * Instantiates a new Webhook.
     *
     * @param type the type
     */
    public Webhook(WebHookType type) {
        this.webHookType = type;
    }

    /**
     * Create webhook function.
     *
     * @param json the json
     * @return the webhook function
     */
    public WebhookFunction create(JsonElement ...json) {
        return create(Requests.RequestMethods.POST, json);
    }

    private List<String> attachments = new ArrayList<String>();

    /**
     * Add attachment webhook.
     *
     * @param data the data
     * @return the webhook
     */
    public Webhook addAttachment(List<String> data) {
        attachments = data;
        return this;
    }

    /**
     * Create webhook function.
     *
     * @param method  the method
     * @param headers the headers
     * @return the webhook function
     */
    public WebhookFunction create(Requests.RequestMethods method, JsonElement ...headers) {
        JsonElement header = (headers != null && headers.length > 0 && headers[0] != null) ? headers[0] : null;
        if (webHookType.equals(WebHookType.DISCORD)) {
            return new WebhookFunction() {
                @Override
                public HttpWrapper.Response process(String web, JsonElement content) {
                    return null;
                }
                @Override
                public HttpWrapper.Response process(String id, String hex, JsonElement content) {
                    String discord_api = "https://discord.com/api/webhooks/" + id  + "/" + hex;
                    HttpWrapper.Response rp;
                    try (var http = new HttpWrapper(discord_api, method)) {
                        http.setContent(content);
                        if (header != null) http.setHeaders(header);
                        if (!attachments.isEmpty()) attachments.forEach(http::addAttachment);
                        rp = http.request().process();
                    }
                    return rp;
                }
            };
        } else if (webHookType.equals(WebHookType.WEB)) {
            return new WebhookFunction() {
                @Override
                public HttpWrapper.Response process(String web, JsonElement content) {
                    HttpWrapper.Response rp;
                    try (var http = new HttpWrapper(web, method)) {
                        if (content == null) content = new JsonObject();
                        http.setContent(content);
                        if (header != null) http.setHeaders(header);
                        if (!attachments.isEmpty()) attachments.forEach(http::addAttachment);
                        rp = http.request().process();
                    }
                    return rp;
                }
                @Override
                public HttpWrapper.Response process(String id, String hex, JsonElement content) {
                    return null;
                }
            };
        }
        return null;
    }

    @Override
    public String toString() {
        return "Webhook{" +
                "webHookType=" + webHookType +
                '}';
    }
}
