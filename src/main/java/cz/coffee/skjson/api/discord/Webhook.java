package cz.coffee.skjson.api.discord;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.skript.requests.Requests;
import cz.coffee.skjson.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;

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
    public WebhookFunction create(JsonElement... json) {
        return create(Requests.RequestMethods.POST, json);
    }

    private List<String> attachments = new ArrayList<>();

    /**
     * Add attachment webhook.
     *
     * @param data the data
     */
    public void addAttachment(List<String> data) {
        attachments = data;
    }

    /**
     * Create webhook function.
     *
     * @param method  the method
     * @param headers the headers
     * @return the webhook function
     */
    public WebhookFunction create(Requests.RequestMethods method, JsonElement... headers) {
        JsonElement header = (headers != null && headers.length > 0 && headers[0] != null) ? headers[0] : null;
        if (webHookType.equals(WebHookType.DISCORD)) {
            return new WebhookFunction() {
                @Override
                public RequestResponse process(String web, JsonElement content) {
                    return null;
                }

                @Override
                public RequestResponse process(String id, String hex, JsonElement content) {
                    String discord_api = "https://discord.com/api/webhooks/" + id + "/" + hex;
                    RequestResponse rp = null;
                    try (var http = new RequestClient(discord_api)) {
                        http.method(method.name().toUpperCase()).setContent(content);
                        if (header != null) http.addHeaders(new WeakHashMap<>(new Gson().fromJson(header, Map.class)));
                        if (!attachments.isEmpty()) attachments.forEach(http::addAttachment);
                        rp = http.request();
                    } catch (Exception e) {
                        if (PROJECT_DEBUG) Util.enchantedError(e, e.getStackTrace(), "Exception-Webhook");
                    }
                    return rp;
                }
            };
        } else if (webHookType.equals(WebHookType.WEB)) {
            return new WebhookFunction() {
                @Override
                public RequestResponse process(String web, JsonElement content) {
                    RequestResponse rp = null;
                    try (var http = new RequestClient(web)) {
                        if (content == null) content = new JsonObject();
                        http.setContent(content);
                        if (header != null) http.addHeaders(new WeakHashMap<>(new Gson().fromJson(header, Map.class)));
                        if (!attachments.isEmpty()) attachments.forEach(http::addAttachment);
                        rp = http.request();
                    } catch (Exception e) {
                        if (PROJECT_DEBUG) Util.webhookLog(e.getMessage());
                    }
                    return rp;
                }

                @Override
                public RequestResponse process(String id, String hex, JsonElement content) {
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
