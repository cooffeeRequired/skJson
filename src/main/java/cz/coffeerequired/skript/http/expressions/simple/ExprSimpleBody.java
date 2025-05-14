package cz.coffeerequired.skript.http.expressions.simple;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;


@Name("body of request/response")
@Examples("""
                 <b>Request</b><br />
                 # getting the Request content;
                 send {_request}'s content
                 send body of {_request}
                \s
                 # setting the Request content;
                 set {_request}'s body to (json from "{'Allow': false}")
                 set body of {_request} to (json from "{'Allow': false}")
                \s
                 # reset the body of the Request
                 reset {_request}'s body
                 reset body of {_request}
                 \s
                 <b>Response</b><br />
                  # getting the Response body;
                 set {_response} to {_request}'s response
                 \s
                 send {_response}'s body
                 send body of {_response}
        \s""")
@Description({"set/reset or get the current request body", "get response body"})
@Since({"2.9.9-pre Api Changes", "5.1"})
@ApiStatus.Experimental
public class ExprSimpleBody extends SimplePropertyExpression<Object, Object> {
    @Override
    public @Nullable Object convert(Object from) {
        if (from instanceof Request request) {
            return request.getContent();
        } else if (from instanceof Response response) {
            return response.content();
        }
        return null;
    }

    @Override
    protected String getPropertyName() {
        return "body of http request/response";
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(JsonElement.class, String[].class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
        Object expr = getExpr().getSingle(event);
        if (expr instanceof Request request) {
            if (mode == Changer.ChangeMode.SET) {
                assert delta != null;
                for (var d : delta) {
                    if (d instanceof String str) {
                        request.setContent(Parser.toJson(str));
                    } else if (d instanceof JsonElement json) {
                        request.setContent(json);
                    }
                }
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setContent(new JsonObject());
            }
        }
    }
}
