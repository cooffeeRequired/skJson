package cz.coffee.skjson.skript.requests;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
        private Expression<?> saveIncorrect, lenient, waitingFor;

        private Variable<?> responseContent, responseHeaders, responseCode, responseURL;

        private boolean isAsync;

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
            if (saveNode != null) {
                try {
                    EntryContainer saveContainer = RequestUtil.SAVE_VALIDATOR.validate(saveNode);
                    if (saveContainer == null) return false;
                    responseContent = (saveContainer.getOptional("content", Variable.class, false));
                    responseHeaders = (saveContainer.getOptional("headers", Variable.class, false));
                    responseCode = (saveContainer.getOptional("status code", Variable.class, false));
                    responseURL = (saveContainer.getOptional("url", Variable.class, false));
                    this.waitingFor = (saveContainer.getOptional("wait for response", Expression.class, false));
                    if (waitingFor != null && !isAsync) {
                        Util.log("&cEntry 'Wait for Response' &ehas no meaning here because you are using synchronized running!");
                    }
                } catch (Exception ex) {
                    Util.enchantedError(ex, ex.getStackTrace(), "Request-89");
                }
            }
            return true;
        }

        @Override
        @SuppressWarnings({"unchecked", "BusyWait"})
        protected @Nullable TriggerItem walk(@NotNull Event e) {
            boolean saveIncorrect; boolean lenient; boolean waitingForResponse;
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
            if (this.waitingFor != null) {
                var waitingConvertedExpression = this.waitingFor.getConvertedExpression(Boolean.class);
                waitingForResponse = waitingConvertedExpression != null && waitingConvertedExpression.getSingle(e);
            } else {
                waitingForResponse = false;
            }


            if (e.isAsynchronous() || isAsync) {
                var done = CompletableFuture.supplyAsync(() -> {
                    var execute = this.execute(e, url, method, requestContent, requestHeaders, saveIncorrect, lenient);
                    return execute.orElse(null);
                });
                while (!done.isDone() && isAsync && waitingForResponse) {
                    try {
                        Thread.sleep(1);
                    } catch (Exception ex) {
                        Util.enchantedError(ex, ex.getStackTrace(), "Request(Waiting for while-loop)");
                    }
                }
            } else {
                this.execute(e, url, method, requestContent, requestHeaders, saveIncorrect, lenient);
            }
            return walk(e, false);
        }

        private Optional<RequestClient> execute(Event event, String url, String method, JsonElement requestContent, Pairs[] requestHeaders, boolean saveIncorrect, boolean lenient) {
            RequestClient client;
            try {
                client = new RequestClient(url);
                var response = client
                        .method(method)
                        .setHeaders(requestHeaders)
                        .setContent(requestContent)
                        .request(lenient).join();

                if (response != null) {
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
            } catch (Exception ex) {
                Util.enchantedError(ex, ex.getStackTrace(), "128-Request.java");
            }
            return Optional.empty();
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return "SkJson Request class";
        }
    }
}