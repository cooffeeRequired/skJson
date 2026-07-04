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
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;


@Name("body of request/response")
@Examples("""
                 <b>Request</b><br />
                 send body of {_request}
                 send {_request}'s body
                \s
                 set body of {_request} to parse "{""Allow"": false}" as json
                 set {_request}'s body to parse "{""Allow"": false}" as json
                \s
                 reset body of {_request}
                 reset {_request}'s body
                 \s
                 <b>Response</b><br />
                 set {_response} to last response of {_request}
                 send body of {_response}
                 send {_response}'s body
        \s""")
@Description({
        "Gets, sets, or resets the request body (JSON or string).",
        "Response bodies are read-only."
})
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
        assert expr instanceof Request;
        Request request = (Request) expr;
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
