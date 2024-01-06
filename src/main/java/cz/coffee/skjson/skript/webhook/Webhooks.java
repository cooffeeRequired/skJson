package cz.coffee.skjson.skript.webhook;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.requests.Webhook;
import cz.coffee.skjson.api.requests.WebhookFunction;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.LoggingUtil;
import cz.coffee.skjson.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.LOGGING_LEVEL;
import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: pondělí (10.07.2023)
 */
public abstract class Webhooks {
    @SuppressWarnings("all")
    public static class WebhookEvent extends Event {
        private static final HandlerList handlers = new HandlerList();

        @Override
        public @NotNull HandlerList getHandlers() {
            return handlers;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }
    }

    @Name("Webhook, send webhook request to Discord/Web")
    @Description({
            "You can handle the discord webhooks via this section.",
            "Can handle also embeds, Request method is PATCH/POST/PUT as always.",
            "You can define the content, headers of each request..",
            "Aso the request shall be sent asynchronous",
            "You can use Json/String, also variables and functions in this section.",
            "! Recommended read that <a href=\"https://discord.com/developers/docs/resources/webhook\"> Discord webhooks api documentation</a>",
            "! Recommended read that <a href=\"https://message.style/app/\"> Embed generate webpage </a>\"",
            "! Recommended default json payload <a href=\"https://message.style/app/share/lc6XU1jd\"> Json Payload (Lorem) </a>"
    })
    @Examples("""
            command web:
                trigger:
                    async send web request "https://webhook.site/4e2e350b-4a8f-4863-85c5-e833e4ec110b":
                        attachments:
                            1: "C:\\Users\\nexti\\Documents\\Lekce\\index.html"
                        content: "{fromSkJson: '?', ?: true}"
                        
                        
            command without-embed:
                trigger:
                    async send discord request "https://discord.com/api/webhooks/1128075537919770798/y78NK-odks6Lod5kimmhcd9YWQfhFzPU1YA-VyD5bqWMGxaeYXxp5jTxpnNI9Yhw1Rgt":
                        header: "Content-Type: application/json"
                        data:
                            tts: true
                            content: "{'payload:' true}" # this can be any json encoded string or json
                        
                        
            command embed:
                trigger:
                    async send discord request "https://discord.com/api/webhooks/1128075537919770798/y78NK-odks6Lod5kimmhcd9YWQfhFzPU1YA-VyD5bqWMGxaeYXxp5jTxpnNI9Yhw1Rgt":
                        header: "Content-Type: application/json"
                        data:
                            username: "AAAA"
                            avatar-url: "https://google.com"
                            tts: true
                            content: "" # content never can be empty, so when you want to send only embed, you need to put here empty string
                            embed:
                                id: 102018 # when you put here null, or auto, the value will be generated automatically.
                                fields: "{}"
                                author: "{name: 'CoffeeRequired'}"
                                title: "Hello there"
                                thumbnail: "{url: 'https://cravatar.eu/helmhead/_F0cus__/600.png'}"
                                color: "##21a7c2" # that support all hex colors.. not minecraft
                        
            command embedAtt:
                trigger:
                    async send discord request "https://discord.com/api/webhooks/1128075537919770798/y78NK-odks6Lod5kimmhcd9YWQfhFzPU1YA-VyD5bqWMGxaeYXxp5jTxpnNI9Yhw1Rgt":
                        attachments:
                            1: "*/generate_doc.sk" # star means the parser will search for the file recursively from the root directory
                        data:
                            tts: false
                            content: "hello from attachments"
                    """)
    @Since("2.9")
    public static class WebHookSection extends Section {
        static {
            SkJson.registerSection(WebHookSection.class, "[:async] send (:web|:discord) request %string%");
        }

        private EntryValidator getEntryValidator(String section) {
            return switch (section) {
                case "web" -> EntryValidator.builder()
                        .addEntryData(new ExpressionEntryData<>("header", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("content", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("attachment", null, true, Object.class))
                        .addSection("attachments", true)
                        .addSection("contents", true)
                        .missingRequiredEntryMessage(entry -> String.format("field %s need to be set, cause %s doesn't have default value!", entry, entry))
                        .build();
                case "discord" -> EntryValidator.builder()
                        .addEntryData(new ExpressionEntryData<>("attachment", null, true, Object.class))
                        .addSection("attachments", true)
                        .addEntryData(new ExpressionEntryData<>("header", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("components", null, true, Expression.class))
                        .addEntryData(new ExpressionEntryData<>("actions", null, true, Expression.class))
                        .addSection("data", false)
                        .missingRequiredEntryMessage(entry -> String.format("field %s need to be set, cause %s doesn't have default value!", entry, entry))
                        .build();
                case "discord-data" -> EntryValidator.builder()
                        .addEntryData(new ExpressionEntryData<>("username", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("avatar-url", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("content", null, true, Object.class))
                        .addSection("contents", true)
                        .addEntry("tts", null, false)
                        .addEntryData(new ExpressionEntryData<>("components", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("actions", null, true, Object.class))
                        .addSection("embed", true)
                        .missingRequiredEntryMessage(entry -> String.format("field %s need to be set, cause %s doesn't have default value!", entry, entry))
                        .build();
                case "discord-embed" -> EntryValidator.builder()
                        .addEntry("id", String.valueOf(new Random().nextInt(27)), true)
                        .addEntryData(new ExpressionEntryData<>("fields", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("author", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("title", null, true, Object.class))
                        .addEntryData(new ExpressionEntryData<>("thumbnail", null, false, Object.class))
                        .addEntry("color", null, true)
                        .missingRequiredEntryMessage(entry -> String.format("field %s need to be set, cause %s doesn't have default value!", entry, entry))
                        .build();
                default -> null;
            };
        }

        private boolean async;
        private boolean isDiscord, isWeb, isEmbed;
        private Expression<?> contentExpression, componentsExpression, actionsExpression, contentHeaders, contentBody;
        private Expression<?> embedFields, embedAuthor, embedTitle, embedThumbnail, contentUsername, contentAvatarURL;
        private String contentTTS, embedID, embedColor;
        private Expression<String> url;
        private final List<String> contents = new ArrayList<>();
        private final List<String> attachments = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> triggerItems) {
            url = (Expression<String>) exprs[0];
            async = parseResult.hasTag("async");
            isDiscord = parseResult.hasTag("discord");
            isWeb = parseResult.hasTag("web");
            if (isDiscord) {
                EntryValidator validator = getEntryValidator("discord");
                if (validator == null) return false;
                EntryContainer container = validator.validate(sectionNode);
                if (container == null) return false;
                contentHeaders = container.getOptional("header", Expression.class, false);
                SectionNode dataSection = container.getOptional("data", SectionNode.class, false);
                SectionNode attachments = container.getOptional("attachments", SectionNode.class, false);
                if (attachments != null) {
                    attachments.convertToEntries(-1);
                    for (Node attachment : attachments) {
                        String key = attachment.getKey();
                        if (key != null) this.attachments.add(attachments.get(key, "").replaceAll("\"", ""));
                    }
                }
                try {
                    validator = getEntryValidator("discord-data");
                    if (validator == null || dataSection == null) return false;
                    EntryContainer dataContainer = validator.validate(dataSection);
                    if (dataContainer == null) return false;
                    contentExpression = dataContainer.getOptional("content", Expression.class, false);
                    if (contentExpression == null) {
                        SectionNode contents = dataContainer.getOptional("contents", SectionNode.class, false);
                        if (contents == null) return false;
                        contents.convertToEntries(-1);
                        for (Node content : contents) {
                            String key = content.getKey();
                            if (key != null) this.contents.add(contents.get(key, "").replaceAll("\"", ""));
                        }
                    }
                    componentsExpression = dataContainer.getOptional("components", Expression.class, false);
                    actionsExpression = dataContainer.getOptional("actions", Expression.class, false);
                    contentAvatarURL = dataContainer.getOptional("avatar-url", Expression.class, false);
                    contentUsername = dataContainer.getOptional("username", Expression.class, false);
                    contentTTS = dataContainer.getOptional("tts", String.class, false);
                    SectionNode embedSection = dataContainer.getOptional("embed", SectionNode.class, false);
                    if (embedSection != null) {
                        isEmbed = true;
                        validator = getEntryValidator("discord-embed");
                        if (validator == null) return false;
                        EntryContainer embedContainer = validator.validate(embedSection);
                        if (embedContainer == null) return false;
                        embedID = embedContainer.getOptional("id", String.class, false);
                        embedFields = embedContainer.getOptional("fields", Expression.class, false);
                        embedAuthor = embedContainer.getOptional("author", Expression.class, false);
                        embedTitle = embedContainer.getOptional("title", Expression.class, false);
                        embedThumbnail = embedContainer.getOptional("thumbnail", Expression.class, false);
                        embedColor = embedContainer.getOptional("color", String.class, false);
                    }

                } catch (Exception ex) {
                    if (PROJECT_DEBUG && LOGGING_LEVEL >= 2)
                        LoggingUtil.error(ex.getLocalizedMessage(), Objects.requireNonNull(getParser().getNode()));
                }
                return true;
            }

            if (isWeb) {
                EntryValidator validator = getEntryValidator("web");
                if (validator == null) return false;
                EntryContainer container = validator.validate(sectionNode);
                if (container == null) return false;


                SectionNode attachments = container.getOptional("attachments", SectionNode.class, false);
                if (attachments != null) {
                    attachments.convertToEntries(-1);
                    for (Node attachment : attachments) {
                        String key = attachment.getKey();
                        if (key != null) this.attachments.add(attachments.get(key, "").replaceAll("\"", ""));
                    }
                }

                contentBody = container.getOptional("content", Expression.class, false);
                contentHeaders = container.getOptional("header", Expression.class, false);

                try {
                    if (contentBody == null) {
                        SectionNode contents = container.getOptional("contents", SectionNode.class, false);
                        if (contents == null) return false;
                        contents.convertToEntries(-1);
                        for (Node content : contents) {
                            String key = content.getKey();
                            if (key != null) this.contents.add(contents.get(key, "").replaceAll("\"", ""));
                        }
                    }
                } catch (Exception e) {
                    LoggingUtil.enchantedError(e, e.getStackTrace(), "WebHooks cannot convert to Entries");
                }
                return true;
            }
            return false;
        }

        @Override
        protected @Nullable TriggerItem walk(@NotNull Event e) {
            if (async) {
                CompletableFuture.runAsync(() -> execute(e));
            } else {
                execute(e);
            }
            return super.walk(e, false);
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(e);
        }

        private JsonElement parseHeader(Event e, Expression<?> ex) {
            if (ex == null) return new JsonObject();
            Object[] o = ex.getAll(e);
            JsonObject returnObject = new JsonObject();
            for (Object unparsed : o) {
                if (unparsed instanceof String st) {
                    if (st.contains(":")) {
                        String[] split = st.split(":");
                        returnObject.addProperty(split[0], split[1]);
                    }
                } else if (unparsed instanceof JsonElement jE) {
                    if (jE.isJsonObject()) {
                        jE.getAsJsonObject().entrySet().forEach(entry -> returnObject.add(entry.getKey(), entry.getValue()));
                    }
                }
            }
            return returnObject;
        }

        private void execute(@NotNull Event event) {
            Bukkit.getScheduler().runTaskAsynchronously(SkJson.getInstance(), () -> {
                JsonElement headers = parseHeader(event, contentHeaders);

                if (isDiscord) {
                    Object content = assignContent(event, contentExpression);
                    Object username = assignContent(event, contentUsername);
                    Object avatarURL = assignContent(event, contentAvatarURL);
                    if (content == null) content = "";

                    if (content.toString().length() < 2 && this.contents.size() > 1) {
                        StringBuilder builder = new StringBuilder();
                        for (String s : this.contents) builder.append(s).append("\n");
                        content = builder.toString();
                    }

                    Boolean tts = Boolean.valueOf(contentTTS);
                    Object actions = assignContent(event, actionsExpression);
                    Object components = assignContent(event, componentsExpression);
                    if (actions == null) actions = new JsonObject();
                    if (components == null) components = new JsonArray();
                    Webhook webhook = new Webhook(Webhook.WebHookType.DISCORD);

                    if (!this.attachments.isEmpty()) webhook.addAttachment(this.attachments);

                    RequestResponse rp;
                    WebhookFunction fn = webhook.create(headers);
                    String url = this.url.getSingle(event);
                    assert url != null;
                    String[] urlChunks = url.split("/");
                    JsonObject json = new JsonObject();
                    json.add("username", ParserUtil.parse(username));
                    json.add("avatar-url", ParserUtil.parse(avatarURL));
                    json.add("content", ParserUtil.parse(content));
                    json.addProperty("tts", tts);
                    json.add("components", ParserUtil.parse(components));
                    json.add("actions", ParserUtil.parse(actions));


                    if (isEmbed) {
                        String id = embedID;
                        JsonArray embed = new JsonArray();
                        JsonElement fields = parseEmbedValue(event, embedFields);
                        JsonElement author = parseEmbedValue(event, embedAuthor);
                        JsonElement title = parseEmbedValue(event, embedTitle);
                        JsonElement thumbnail = parseEmbedValue(event, embedThumbnail);
                        String color = embedColor;

                        if (fields.isJsonNull() || !fields.isJsonArray()) fields = new JsonArray();
                        if (author.isJsonNull() || !author.isJsonObject()) author = new JsonObject();
                        if (thumbnail.isJsonNull() || !thumbnail.isJsonObject()) thumbnail = new JsonObject();
                        if (id.isEmpty() || !Util.isNumber(id)) id = String.valueOf(new Random().nextInt(929233221));
                        if (color.isEmpty()) color = String.valueOf(Long.parseLong("21a7c2".toUpperCase(), 16));
                        color = color.replaceAll("[\"#]", "").toUpperCase();
                        color = String.valueOf(Long.parseLong(color, 16));
                        JsonObject embed1 = new JsonObject();
                        embed1.addProperty("id", Long.parseLong(id));
                        embed1.add("fields", fields);
                        embed1.add("author", author);
                        embed1.add("title", title);
                        embed1.add("thumbnail", thumbnail);
                        embed1.addProperty("color", Long.parseLong((color)));

                        embed.add(embed1);
                        json.add("embeds", embed);
                    }
                    rp = (fn.process(urlChunks[5], urlChunks[6], json));
                    if (rp != null && rp.isSuccessfully()) {
                        if (LOGGING_LEVEL > 1) LoggingUtil.webhookLog("The payload was sent &asuccesfully.");
                    } else {
                        if (PROJECT_DEBUG && LOGGING_LEVEL > 1) {
                            assert rp != null;
                            LoggingUtil.webhookLog("The payload was sent &cunsuccesfully. Cause of " + rp.getBodyContent(true));
                        }
                    }
                } else if (isWeb) {
                    Webhook webhook = new Webhook(Webhook.WebHookType.WEB);
                    if (!this.attachments.isEmpty()) {
                        webhook.addAttachment(this.attachments);
                    }
                    RequestResponse rp;
                    WebhookFunction fn = webhook.create(headers);
                    String url = this.url.getSingle(event);
                    assert url != null;
                    JsonElement content = parseEmbedValue(event, contentBody);

                    if (content.isJsonNull() && !contents.isEmpty()) {
                        JsonObject json = new JsonObject();
                        int i = 0;
                        for (String s : contents) {
                            json.addProperty(String.valueOf(i), s);
                            i++;
                        }
                        content = json;
                    }

                    rp = (fn.process(url, content));
                    if (rp != null && rp.isSuccessfully()) {
                        if (LOGGING_LEVEL > 1) LoggingUtil.webhookLog("The payload was sent &asuccesfully.");
                    } else {
                        if (PROJECT_DEBUG && LOGGING_LEVEL > 1) {
                            assert rp != null;
                            LoggingUtil.webhookLog("The payload was sent &asuccesfully. Cause of " + rp.getBodyContent(false));
                        }
                    }
                }
            });
        }

        private Object assignContent(Event event, Expression<?> ex) {
            if (ex == null) return null;
            return ex.getSingle(event);
        }

        private JsonElement parseEmbedValue(Event e, Expression<?> ex) {
            try {
                if (ex == null) return JsonNull.INSTANCE;
                ex = LiteralUtils.defendExpression(ex);
                if (!LiteralUtils.canInitSafely(ex)) return JsonNull.INSTANCE;
                Object single = ex.getSingle(e);

                if (single instanceof JsonElement json) {
                    return json;
                } else if (single instanceof String str) {
                    return ParserUtil.parse(str);
                }
                return JsonNull.INSTANCE;
            } catch (Exception exs) {
                if (PROJECT_DEBUG) LoggingUtil.enchantedError(exs, exs.getStackTrace(), "Webhooks.parseEmbedValue");
                return JsonNull.INSTANCE;
            }
        }
    }
}
