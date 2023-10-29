package cz.coffee.skjson.api.discord;

import com.google.gson.JsonElement;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.skript.request.RequestMethods;
import cz.coffee.skjson.utils.Util;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

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
        return create(RequestMethods.POST, json);
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
    public WebhookFunction create(RequestMethods method, JsonElement... headers) {
        if (webHookType.equals(WebHookType.DISCORD)) {
            return new WebhookFunction() {
                @Override
                public RequestResponse process(String web, JsonElement content) {
                    return null;
                }

                @Override
                public RequestResponse process(String id, String hex, JsonElement content) {
                    String discord_api = "https://discord.com/api/webhooks/" + id + "/" + hex;
                    RequestResponse[] response = new RequestResponse[1];
                    RequestClient client = new RequestClient(discord_api).method(method.toString()).setHeaders(headers);
                    try {
                        if (!attachments.isEmpty()) {
                            attachments.forEach(client::addAttachment);
                            response[0] = client
                                    .postAttachments(content)
                                    .request().join();
                        } else {
                            response[0] = client.setContent(content).request(true).join();
                        }
                    } catch (Exception e) {
                        if (PROJECT_DEBUG) {
                            Util.webhookLog(e.getMessage());
                        }
                    }
                    return response[0];
                }
            };
        } else if (webHookType.equals(WebHookType.WEB)) {
            return new WebhookFunction() {
                @Override
                public RequestResponse process(String web, JsonElement content) {
                    RequestResponse[] response = new RequestResponse[1];
                    try {
                        RequestClient client = new RequestClient(web).method(method.toString()).setHeaders(headers);
                        if (!attachments.isEmpty()) {
                            attachments.forEach(client::addAttachment);
                            response[0] = client
                                    .postAttachments(content)
                                    .request().join();
                        } else {
                            response[0] = client.setContent(content).request().join();
                        }
                    } catch (Exception e) {
                        if (PROJECT_DEBUG) {
                            Util.webhookLog(e.getMessage());
                        }
                    }
                    return response[0];
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
