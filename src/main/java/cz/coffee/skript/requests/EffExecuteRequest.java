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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.coffee.core.requests.HttpHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: pátek (17.03.2023)
 */
@Name("Execute a http (post|get) request")
@Description("You can execute a web request to rest api. with json encoded body/headers")
@Examples({
        "set {_headers} to json from text \"{'Content-type': 'application/json}\"",
        "execute GET request to \"https://dummyjson.com/products/1\" with headers {_headers}",
        "send request's body with pretty print"
})
@Since("2.8.0 performance & clean")

public class EffExecuteRequest extends AsyncEffect {

    static {
        Skript.registerEffect(EffExecuteRequest.class,
                "execute GET request to %string% [(:with headers) %-string/json%]",
                "execute POST request to %string% [:(with headers) %-string/json%] [[and] [with] (:body) %-string/json%]"
        );
    }

    private int pattern;
    private Expression<String> urlExpression;
    private Expression<?> bodyExpression, headersExpression;
    private boolean withHeaders, withBody;
    protected static HttpHandler.HandlerBody response;

    @Override
    protected void execute(@NotNull Event e) {
        final String url = urlExpression.getSingle(e);
        HttpHandler handler;
        Object headers;
        Object body;
        try {
            if (pattern == 0) {
                JsonObject finalHeaders;
                handler = new HttpHandler(url, "GET");
                if (withHeaders) {
                    headers = headersExpression.getSingle(e);
                    if (headers instanceof String s) {
                        finalHeaders = JsonParser.parseString(s).getAsJsonObject();
                    } else {
                        finalHeaders = (JsonObject) headers;
                    }
                    if (finalHeaders != null && finalHeaders.isJsonObject()) {
                        finalHeaders.getAsJsonObject().entrySet().forEach(entry -> {
                            handler.addHeader(entry.getKey(), entry.getValue().toString());
                        });
                    }
                }
                handler.asyncSend();
                response = handler.getBody();
                handler.disconnect();
            } else if (pattern == 1) {
                handler = new HttpHandler(url, "POST");
                JsonObject finalHeaders;
                JsonObject finalBody;
                if (withHeaders) {
                    headers = headersExpression.getSingle(e);
                    if (headers instanceof String s) {
                        finalHeaders = JsonParser.parseString(s).getAsJsonObject();
                    } else {
                        finalHeaders = (JsonObject) headers;
                    }
                    if (finalHeaders != null && finalHeaders.isJsonObject()) {
                        finalHeaders.getAsJsonObject().entrySet().forEach(entry -> {
                            handler.addHeader(entry.getKey(), entry.getValue().toString());
                        });
                    }
                }
                if (withBody) {
                    body = bodyExpression.getSingle(e);
                    if (body instanceof String s) {
                        finalBody = JsonParser.parseString(s).getAsJsonObject();
                    } else {
                        finalBody = (JsonObject) body;
                    }
                    if (finalBody != null && finalBody.isJsonObject()) {
                        finalBody.getAsJsonObject().entrySet().forEach(body_ -> {
                            handler.addBodyContent(body_.getKey(), body_.getValue().toString());
                        });
                    }
                }
                handler.asyncSend();
                response = handler.getBody();
                handler.disconnect();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return String.format("execute %s request to %s", (pattern == 0 ? "GET" : "POST"), urlExpression.toString(e, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        pattern = matchedPattern;
        urlExpression = (Expression<String>) exprs[0];
        headersExpression = LiteralUtils.defendExpression(exprs[1]);
        withBody = parseResult.hasTag("body");
        withHeaders = parseResult.hasTag("with headers");

        if (withHeaders) {
            if (LiteralUtils.canInitSafely(headersExpression)) {
                if (!headersExpression.getReturnType().isAssignableFrom(String.class) || !headersExpression.getReturnType().isAssignableFrom(JsonElement.class)) {
                    Skript.error("Invalid headers input, you can use only raw json string, or Json");
                    return false;
                }
            } else {
                return false;
            }
        }
        if (pattern == 1) {
            bodyExpression = LiteralUtils.defendExpression(exprs[2]);
            if (withBody) {
                if (LiteralUtils.canInitSafely(bodyExpression)) {
                    if (bodyExpression.getReturnType().isAssignableFrom(String.class) || headersExpression.getReturnType().isAssignableFrom(JsonElement.class)) {
                        return true;
                    } else {
                        Skript.error("Invalid body input, you can use only raw json string, or Json");
                        return false;
                    }
                } else {
                    return true;
                }
            }
        } else if (pattern == 0) {
            if (withHeaders) {
                return LiteralUtils.canInitSafely(headersExpression);
            }
        }
        return true;
    }
}
