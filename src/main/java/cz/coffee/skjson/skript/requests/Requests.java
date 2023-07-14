package cz.coffee.skjson.skript.requests;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.*;
import cz.coffee.skjson.api.Update.HttpWrapper;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The type Requests.
 */
public abstract class Requests {
    /**
     * The type Request.
     */
    @Name("Http Request")
    @Description("Create & handle requests via json")
    @Examples({
            "<b>Checkout this link https://dummyjson.com/docs/carts for examples of dummyJson api</b>",
            "on script load:",
            "\tasync make POST request to \"https://dummyjson.com/carts/add",
            "\t\theader: \"Content-Type: application/json\"",
            "\t\tcontent: json from text \"{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, quantity: 2}]}\"",
            "\t\tsave:",
            "\t\t\tcontent: {_content}",
            "\t\t\theader: {_header}",
            "\t\t\tstatus code: {_code}",
            "\t\t\turl: {_url}",
            "command response:",
            "\ttrigger:",
            "\t\tsend {_content} with pretty print"
    })
    @Since("2.9")
    public static class Request extends Section {

        /**
         * The type Request create.
         */
        public static class RequestCreate extends Event {

            @Override
            public @NotNull HandlerList getHandlers() {
                throw new IllegalStateException();
            }
        }

        static {
            Skript.registerSection(Request.class, "[:async] make [new] %requestmethod% request to %string%");
        }

        private Expression<RequestMethods> method;
        private Expression<String> url;
        private Expression<?> content;
        private Expression<?> header;
        private boolean async;

        private Variable<?> sContent, sHeader, sCode, sUrl;
        private static final Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> triggerItems) {
            method = (Expression<RequestMethods>) exprs[0];
            url = (Expression<String>) exprs[1];
            EntryValidator validator = EntryValidator.builder()
                    .addEntryData(new ExpressionEntryData<>("content", null, true, Object.class, RequestCreate.class))
                    .addEntryData(new ExpressionEntryData<>("header", null, true, Object.class, RequestCreate.class))
                    .addSection("save", true)
                    .build();

            EntryValidator saveValidator = EntryValidator.builder()
                    .addEntryData(new ExpressionEntryData<>("content", null, true, Variable.class, RequestCreate.class))
                    .addEntryData(new ExpressionEntryData<>("headers", null, true, Variable.class, RequestCreate.class))
                    .addEntryData(new ExpressionEntryData<>("status code", null, true, Variable.class, RequestCreate.class))
                    .addEntryData(new ExpressionEntryData<>("url", null, true, Variable.class, RequestCreate.class))
                    .build();

            EntryContainer container = validator.validate(sectionNode);
            if (container == null) return false;
            content = (Expression<?>) container.getOptional("content", Expression.class, false);
            header = (Expression<?>) container.getOptional("header", Object.class, false) ;
            SectionNode s = container.getOptional("save", SectionNode.class, false);
            try {
                if (s != null) {
                    EntryContainer saveContainer = saveValidator.validate(s);
                    if (saveContainer == null) return false;
                    sContent = (saveContainer.getOptional("content", Variable.class, false));
                    sHeader = (saveContainer.getOptional("headers", Variable.class, false));
                    sCode = (saveContainer.getOptional("status code", Variable.class, false));
                    sUrl = (saveContainer.getOptional("url", Variable.class, false));
                }
            } catch (Exception ex) {
                Util.requestLog("In the save section you can use only variables for saving data to them.");
                return false;
            }
            async = parseResult.hasTag("async");
            return true;
        }

        @Override
        protected @Nullable TriggerItem walk(@NotNull Event e) {
            if (async) {
                CompletableFuture.runAsync(() -> {
                    execute(e);
                });
            } else {
                execute(e);
            }
            return super.walk(e, false);
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(e);
        }

        private void execute(Event e) {
            Object unparsedRequestBody = null;
            if (content != null) unparsedRequestBody= content.getSingle(e);
            Object[] unparsedRequestHeaders = null;
            if (!(header instanceof UnparsedLiteral)) {
                if (header != null) unparsedRequestHeaders = header.getAll(e);
            }

            String url = this.url.getSingle(e);
            RequestMethods method = this.method.getSingle(e);
            JsonElement body = null;

            if (unparsedRequestBody == null) unparsedRequestBody = new JsonObject();

            if (unparsedRequestBody instanceof JsonElement json) {
                body = json;
            } else if (unparsedRequestBody instanceof String json) {
                try {
                    body = JsonParser.parseString(json);
                    if (body == null) body = gson.toJsonTree(json);
                } catch ( Exception ex) {
                    Util.requestLog(ex.getLocalizedMessage());
                }
            } else {
                Util.requestLog("Please provide Json or Stringify json content");
                return;
            }

            if (unparsedRequestHeaders == null) return;

            List<JsonObject> headers = new ArrayList<>();
            for (Object unparsedHeader : unparsedRequestHeaders) {
                if (unparsedHeader instanceof JsonObject json) {
                    headers.add(json);
                } else if (unparsedHeader instanceof String pair) {
                    JsonObject map = new JsonObject();
                    String[] pairs = pair.split(":");
                    map.addProperty(pairs[0], pairs[1]);
                    headers.add(map);
                } else {
                    Util.requestLog("Please provide Json or Stringify header");
                }
            }
            if (body == null) body = new JsonObject();

            try (var http = new HttpWrapper(url, method)) {
                http.setContent(body);
                headers.forEach(http::setHeaders);
                HttpWrapper.Response rp = http.request().process();

                if (sContent != null) {
                    String name = sContent.getName().getSingle(e);
                    boolean local = sContent.isLocal();
                    Variables.setVariable(name, rp.getBodyContent(), e, local);
                }

                if (sHeader != null) {
                    String name = sHeader.getName().getSingle(e);
                    boolean local = sHeader.isLocal();
                    Variables.setVariable(name, rp.getResponseHeader(), e, local);
                }

                if (sCode != null) {
                    String name = sCode.getName().getSingle(e);
                    boolean local = sCode.isLocal();
                    Variables.setVariable(name, rp.getStatusCode(), e, local);
                }

                if (sUrl != null) {
                    String name = sUrl.getName().getSingle(e);
                    boolean local = sUrl.isLocal();
                    Variables.setVariable(name, rp.getRequestURL(), e, local);
                }

            } catch (Exception exception) {
                Util.requestLog(exception.getMessage());
            }
        }
    }

    public enum RequestMethods {
        GET("get", 0),
        POST("post", 1),
        PUT("put", 2),
        DELETE("delete", 3),
        MOCK("mock", 4),
        HEAD("head", 5),
        PATCH("patch", 6);

        final String stringMethod;
        final int value;

        RequestMethods(String stringMethod, int value) {
            this.stringMethod = stringMethod;
            this.value = value;
        }

        @Override
        public String toString() {
            return stringMethod.toUpperCase();
        }
    }
}
