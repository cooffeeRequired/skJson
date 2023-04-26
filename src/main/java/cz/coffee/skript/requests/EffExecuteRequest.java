package cz.coffee.skript.requests;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.requests.HttpHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static cz.coffee.SkJson.RESPONSES;
import static cz.coffee.core.requests.HttpHandler.RequestContent.process;


@Name("Execute a http request")
@Description("You can execute a web request to rest api. with json encoded body/headers")
@Examples({
        "set {_headers} to json from text \"{'Content-type': 'application/json'}\"",
        "execute MOCK request to \"https://dummyjson.com/http/200\"",
        "execute GET request to \"https://dummyjson.com/products/ with headers \"Content-type: application/json\"",
        "execute POST request to \"https://dummyjson.com/products/add\" with headers \"Content-type: application/json\" and with data \"title: TEST\"",
        "send request's body with pretty print",
        "",
        "",
        "execute GET request to \"https://dummyjson.com/products/ with headers \"Content-type: application/json\"",
        "if request's code is 200:",
        "\tsend request's body with pretty print"
})
@Since("2.8.3, 2.8.0 performance & clean")

public class EffExecuteRequest extends AsyncEffect {
    private final List<String>  allowedMethods = List.of("POST", "GET", "PUT", "DELETE", "MOCK", "HEAD", "PATCH");
    private Expression<?> bodyExpression, headersExpression;
    private Expression<String> urlExpression, methodExpression;
    private String parsedMethod;
    private boolean withHeaders, withBody;

    /**
    Sanitize missing or forgot ", also sanitize the bukkit colors
     */
    public static String sanitize(Object o) {
        final String st = o.toString();
        return st.replace('§', '&').replace("\"", "");
    }


    static {
        Skript.registerEffect(EffExecuteRequest.class,
                "(execute|send|make) [new] (<.+>|%-string%) request to %string% [(:with headers) %-strings/json%] [and with (:body|:data) %-strings/json%]",
                "(execute|send|make) [new] (<.+>|%-string%) request to %string% [with (:body|:data) %-strings/json%] [and (:with headers) %-strings/json%]"
        );
    }

    @Override
    protected void execute(@NotNull Event e) {
        // Process method
        if (parsedMethod == null) {
            parsedMethod = methodExpression.getSingle(e);
            if (parsedMethod != null) parsedMethod = sanitize(parsedMethod);
        }

        // Process url
        String parsedUrl = urlExpression.getSingle(e);
        if (parsedUrl != null) parsedUrl = sanitize(parsedUrl);
        if (!allowedMethods.contains(parsedMethod)) {
            Skript.error("This method " + parsedMethod + " is not allowed. Only these methods are allowed: " + allowedMethods);
            return;
        }
        HttpHandler handler = HttpHandler.of(parsedUrl, parsedMethod);
        if (withHeaders) {
            Object[] h = headersExpression.getAll(e);
            Class<?> cl = headersExpression.getReturnType();
            HttpHandler.RequestContent headers = process(h, JsonElement.class.isAssignableFrom(cl));
            assert headers != null;
            handler.setMainHeaders(headers);
        }
        if (withBody) {
            Object[] b = bodyExpression.getAll(e);
            Class<?> cl = bodyExpression.getReturnType();
            HttpHandler.RequestContent body = process(b, JsonElement.class.isAssignableFrom(cl));
            assert body != null;
            handler.setBodyContent(body);
        }
        handler.asyncSend();
        if (RESPONSES[1] == null) RESPONSES[1] = RESPONSES[0];
        RESPONSES[0] = handler.getAll();
        handler.disconnect();
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "make new " + parsedMethod + " request to " + urlExpression.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        withBody = parseResult.hasTag("body") || parseResult.hasTag("data");
        withHeaders = parseResult.hasTag("with headers");
        if (exprs[0] == null) {
            String regex = parseResult.regexes.get(0).group(0);
            if (allowedMethods.contains(regex)) {
                parsedMethod = regex;
            } else {
                Skript.error("This method " + regex + " is not allowed. Only these methods are allowed: " + allowedMethods);
                return false;
            }
        } else {
            methodExpression = (Expression<String>) exprs[0];
        }
        urlExpression = (Expression<String>) exprs[1];

        if (matchedPattern == 0) {
            if (withHeaders) {
                headersExpression = LiteralUtils.defendExpression(exprs[2]);
                if (!LiteralUtils.canInitSafely(headersExpression)) return false;
            }
            if (withBody) {
                bodyExpression = LiteralUtils.defendExpression(exprs[3]);
                return LiteralUtils.canInitSafely(bodyExpression);
            }
        } else if (matchedPattern == 1) {
            if (withHeaders) {
                headersExpression = LiteralUtils.defendExpression(exprs[3]);
                if (!LiteralUtils.canInitSafely(headersExpression)) return false;
            }
            if (withBody) {
                bodyExpression = LiteralUtils.defendExpression(exprs[2]);
                return LiteralUtils.canInitSafely(bodyExpression);
            }
        }
        return true;
    }
}
