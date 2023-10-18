package cz.coffee.skjson.skript.requests;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;
import static cz.coffee.skjson.skript.requests.RequestUtil.Pairs;

/**
 * The type Requests.
 */
public abstract class Request {
    /**
     * The type Request.
     */
    @Name("Http Request")
    @Description({
            "Create & handle requests via json",
            "<b>Checkout this link <a href=\"https://dummyjson.com/docs/carts\"> Test json </a> for examples of dummyJson api</b>",
            "<b> Allowed all basic types of requests [GET, POST, PUT, DELETE, PATCH, HEAD, MOCK, MODIFY, ENTRY, NOTE] </b>"
    })
    @Examples({
            "on script load:",
            "\tasync make POST request to \"https://dummyjson.com/carts/add\":",
            "\t\theaders: \"Content-Type: application/json\"",
            "\t\tcontent: json from text \"{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, quantity: 2}]}\"",
            "\t\tsave incorrect response: true",
            "\t\tlenient: true",
            "\t\tsave:",
            "\t\t\twait for response: true",
            "\t\t\tcontent: {-content}",
            "\t\t\theaders: {-header}",
            "\t\t\tstatus code: {-code}",
            "\t\t\turl: {-url}",
            "command response:",
            "\ttrigger:",
            "\t\tsend {-content} with pretty print"
    })
    @Since("2.9")
    public static class SecRequest extends Section {

        private Expression<?> requestBody, requestHeaders;
        private Expression<String> requestUrl;
        private Expression<RequestMethods> requestMethod;
        private Expression<?> saveIncorrect, lenient;
        private Trigger onCompleteTrigger;

        private Variable<?> responseContent, responseHeaders, responseCode, responseURL;

        private boolean isAsync;
        private volatile boolean requestIsDone = false;
        private volatile boolean returnInOnComplete = false;

        static {
            SkJson.registerSection(SecRequest.class, "[:async] make [new] %requestmethod% request to %string%");
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] expr, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> triggerItems) {
            EntryContainer container = RequestUtil.VALIDATOR.validate(sectionNode);
            isAsync = parseResult.hasTag("async");
            if (container == null) {
                return false;
            }
            requestBody = container.getOptional("content", Expression.class, false);
            requestHeaders = container.getOptional("headers", Expression.class, false);
            requestUrl = (Expression<String>) expr[1];
            requestMethod = (Expression<RequestMethods>) expr[0];
            this.saveIncorrect = container.getOptional("save incorrect response", Expression.class, false);
            this.lenient = container.getOptional("lenient", Expression.class, false);
            var saveNode = container.getOptional("save", SectionNode.class, false );
            var onCompleteNode = (container.getOptional("on complete", SectionNode.class, false));
            if (onCompleteNode != null) {
                for (Node w : onCompleteNode) {
                    if (w.toString().contains("return")) {
                        if (!Config.ALLOWED_IMPLICIT_REQUEST_RETURN) {
                            Util.warn("You don't have allowed this beta feature, if you want use these implicit request return, you may turn on that in your config.yml");
                            return false;
                        }
                        returnInOnComplete = true;
                        break;
                    }
                }
                this.onCompleteTrigger = loadCode(onCompleteNode, "on complete");
            }
            if (saveNode != null) {
                try {
                    EntryContainer saveContainer = RequestUtil.SAVE_VALIDATOR.validate(saveNode);
                    if (saveContainer == null) return false;
                    responseContent = (saveContainer.getOptional("content", Variable.class, false));
                    responseHeaders = (saveContainer.getOptional("headers", Variable.class, false));
                    responseCode = (saveContainer.getOptional("status code", Variable.class, false));
                    responseURL = (saveContainer.getOptional("url", Variable.class, false));
                } catch (Exception ex) {
                    Util.enchantedError(ex, ex.getStackTrace(), "Request-89");
                }
            }
            return true;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected @Nullable TriggerItem walk(@NotNull Event e) {
            boolean saveIncorrect; boolean lenient;
            var requestContent = RequestUtil.validateContent(this.requestBody, e);
            var requestHeaders = RequestUtil.validateHeaders(this.requestHeaders, e);
            var url = this.requestUrl != null ? this.requestUrl.getSingle(e) : "";
            var method = this.requestUrl != null ? (Objects.requireNonNull(this.requestMethod.getSingle(e)).toString().toUpperCase()) : "GET";

            if (this.saveIncorrect != null) {
                var incorrectConvertedExpression = this.saveIncorrect.getConvertedExpression(Boolean.class);
                saveIncorrect = incorrectConvertedExpression != null && incorrectConvertedExpression.getSingle(e);
            } else {
                saveIncorrect = false;
            }
            if (this.lenient != null) {
                var lenientConvertedExpression = this.lenient.getConvertedExpression(Boolean.class);
                lenient = lenientConvertedExpression != null && lenientConvertedExpression.getSingle(e);
            } else {
                lenient = false;
            }
            this.execute(e, url, method, requestContent, requestHeaders, saveIncorrect, lenient);
            while (!requestIsDone && returnInOnComplete) Thread.onSpinWait();
            return walk(e, true);
        }

        private void setVariables(RequestResponse response, boolean saveIncorrect, Event event) {
            if (responseContent != null) {
                var name = responseContent.getName().getSingle(event);
                boolean local = responseContent.isLocal();
                Variables.setVariable(name, response.getBodyContent(saveIncorrect), event, local);
            }

            if (responseHeaders != null) {
                var name = responseHeaders.getName().getSingle(event);
                boolean local = responseHeaders.isLocal();
                Variables.setVariable(name, response.getRequestHeaders().text(), event, local);
            }

            if (responseCode != null) {
                var name = responseCode.getName().getSingle(event);
                boolean local = responseCode.isLocal();
                Variables.setVariable(name, response.getStatusCode(), event, local);
            }

            if (responseURL != null) {
                var name = this.responseURL.getName().getSingle(event);
                boolean local = responseURL.isLocal();
                Variables.setVariable(name, response.getRequestURL(), event, local);
            }
        }

        private void execute(Event event, String url, String method, JsonElement requestContent, Pairs[] requestHeaders, boolean saveIncorrect, boolean lenient) {
            requestIsDone = false;
            RequestClient client;
            try {
                client = new RequestClient(url);
                var responseCompletableFuture = client
                        .method(method)
                        .setHeaders(requestHeaders)
                        .setContent(requestContent)
                        .request(lenient);


                if (isAsync || event.isAsynchronous())  {
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            while (!responseCompletableFuture.isDone()) {
                                Thread.onSpinWait();
                            }
                            var response = responseCompletableFuture.get();
                            if (response != null) {
                                setVariables(response, saveIncorrect, event);
                                if (this.onCompleteTrigger != null) {
                                    this.onCompleteTrigger.execute(event);
                                    requestIsDone = true;
                                }
                            }
                            return 1;
                        } catch (Exception ex) {
                            Util.enchantedError(ex, ex.getStackTrace(), "216-Async Request");
                            requestIsDone = true;
                            return -1;
                        }
                    }).join();
                } else {
                    var response = responseCompletableFuture.get();
                    if (response != null) setVariables(response, saveIncorrect, event);
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("Illegal character in query")) {
                    Util.error("The url is incorrect... URL: "+ url);
                } else {
                    if (PROJECT_DEBUG) Util.enchantedError(ex, ex.getStackTrace(), "214-Request.java");
                }
                requestIsDone = true;
            }
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return "SkJson Request class";
        }
    }
}
