package cz.coffee.skjson.skript.request;

import ch.njol.skript.classes.Changer;
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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;

import java.util.List;

@Name("[WEB] Request")
@Description({"Create & handle requests via json",
        "Checkout this link https://dummyjson.org for json examples of dummyJson api",
        "Allowed all basic types of requests [GET, POST, PUT, DELETE, PATCH, HEAD, MOCK, MODIFY, ENTRY, NOTE]"})
@Examples("""
        on load:
            make new GET request to "https://dummyjson.com/products/2" and store it in {_data}:
               content: {_content}
               status code: {_code}
            execute {_data} and wait
            send {_content}
                
                
        on script load:
            async make POST request to "https://dummyjson.com/carts/add":
                headers: "Content-Type: application/json"
                content: json from text "{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, quantity: 2}]}"
                save incorrect response: true
                lenient: true
                save:
                    content: {-content}
                    headers: {-header}
                    status code: {-code}
                    url: {-url}
        command response:
            trigger:
                send {-content} with pretty print
        """
)
@Since("2.9.4")
public class SecCreateRequest extends Section {

    static {
        SkJson.registerSection(SecCreateRequest.class,
                "[:async] (create|make) [new] %requestmethod% request to %string%",
                "[:async] (create|make) [new] %requestmethod% request to %string% [with header[s] %-json/strings% [and with body %-json/strings%]] and (stored|store it) in %-objects%"
        );
    }

    private Expression<RequestMethods> _method;
    private Expression<String> _url;
    private Expression<?> _headers;
    private Expression<?> _body;
    private Expression<?> _store;
    private SectionNode coreSection;
    private int pattern;


    private boolean isAsync;
    private Expression<Boolean> saveIncorrect;
    private Expression<Boolean> lenient;
    private Variable<?> rContent, rHeaders, rCode, rUrl;


    private void saveToVars(RequestResponse response, boolean saveIncorrect, Event event) {
        if (response != null) {
            if (rContent != null)
                rContent.change(event, new Object[]{response.getBodyContent(saveIncorrect)}, Changer.ChangeMode.SET);
            if (rHeaders != null)
                rHeaders.change(event, new Object[]{response.getResponseHeader().text()}, Changer.ChangeMode.SET);
            if (rCode != null) rCode.change(event, new Object[]{response.getStatusCode()}, Changer.ChangeMode.SET);
            if (rUrl != null) rUrl.change(event, new Object[]{response.getRequestURL()}, Changer.ChangeMode.SET);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult, @NotNull SectionNode sectionNode, @NotNull List<TriggerItem> list) {
        pattern = i;
        _method = LiteralUtils.defendExpression(expressions[0]);
        if (i == 1) {
            _url = (Expression<String>) expressions[1];
            if (expressions[2] != null) {
                _headers = LiteralUtils.defendExpression(expressions[2]);
                if (!LiteralUtils.canInitSafely(_headers)) return false;
            }

            if (expressions[3] != null) {
                _body = LiteralUtils.defendExpression(expressions[3]);
                if (!LiteralUtils.canInitSafely(_body)) return false;
            }

            _store = LiteralUtils.defendExpression(expressions[4]);
            if (!(_store instanceof Variable<?>)) {
                throw RequestException.wrongStoreVar();
            }
            coreSection = sectionNode;
            return LiteralUtils.canInitSafely(_store);
        } else if (i == 0) {
            EntryContainer container = RequestUtil.VALIDATOR.validate(sectionNode);
            isAsync = parseResult.hasTag("async");
            if (container == null) {
                return false;
            }
            _body = container.getOptional("content", Expression.class, false);
            _headers = container.getOptional("headers", Expression.class, false);
            _url = (Expression<String>) expressions[1];
            this.saveIncorrect = container.getOptional("save incorrect response", Expression.class, false);
            this.lenient = container.getOptional("lenient", Expression.class, false);
            var saveNode = container.getOptional("save", SectionNode.class, false);
            if (saveNode != null) {
                try {
                    EntryContainer saveContainer = RequestUtil.SAVE_VALIDATOR.validate(saveNode);
                    if (saveContainer == null) return false;
                    rContent = (saveContainer.getOptional("content", Variable.class, false));
                    rHeaders = (saveContainer.getOptional("headers", Variable.class, false));
                    rCode = (saveContainer.getOptional("status code", Variable.class, false));
                    rUrl = (saveContainer.getOptional("url", Variable.class, false));
                } catch (Exception ex) {
                    LoggingUtil.enchantedError(ex, ex.getStackTrace(), "Request-92");
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("all")
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        String url = _url != null ? _url.getSingle(event) : "http://localhost";
        JsonElement body = RequestUtil.validateContent(_body, event);
        RequestUtil.Pairs[] headers = RequestUtil.validateHeaders(_headers, event);
        String method = _method != null ?
                (_method.getSingle(event) != null ?
                        _method.getSingle(event) : RequestMethods.GET).toString() : "GET";

        if (pattern == 1) {
            Request request = new Request(this.coreSection, url, event);
            request.setMethod(method)
                    .setBody(body)
                    .setHeaders(headers)
                    .lenient(true)
                    .saveIncorrect(true);
            _store.change(event, new Request[]{request}, Changer.ChangeMode.SET);
            return super.walk(event, false);
        } else if (pattern == 0) {
            boolean lenient;
            boolean saveInc;
            if (this.lenient != null) {
                var lenientConvertedExpression = this.lenient.getConvertedExpression(Boolean.class);
                lenient = this.lenient != null ? lenientConvertedExpression.getSingle(event) : false;
            } else {
                lenient = false;
            }

            if (this.saveIncorrect != null) {
                var incorrectConvertedExpression = this.saveIncorrect.getConvertedExpression(Boolean.class);
                saveInc = this.saveIncorrect != null ? incorrectConvertedExpression.getSingle(event) : false;
            } else {
                saveInc = true;
            }

            if (isAsync) {
                Bukkit.getScheduler().runTaskAsynchronously(SkJson.getInstance(), () -> {
                    try (var client = new RequestClient(url)) {
                        var response = client
                                .method(method)
                                .setHeaders(headers)
                                .setContent(body)
                                .request(lenient).join();
                        if (response != null) {
                            saveToVars(response, saveInc, event);
                        }
                    }
                });
            } else {
                try (var client = new RequestClient(url)) {
                    var response = client
                            .method(method)
                            .setHeaders(headers)
                            .setContent(body)
                            .request(lenient).join();
                    if (response != null) {
                        saveToVars(response, saveInc, event);
                    }
                }
            }
        }

        return walk(event, true);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "New request section ";
    }
}
