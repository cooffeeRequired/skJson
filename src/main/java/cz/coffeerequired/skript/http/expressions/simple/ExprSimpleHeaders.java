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
import cz.coffeerequired.api.requests.Pairs;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
@Name("headers of request/response")
@Examples("""
                 <b>Request</b><br />
                 # getting the Request headers;
                 send {_request}'s headers
                 send headers of {_request}
                \s
                 # setting the Request content;
                 set {_request}'s headers to (json from "{'Content-Type': 'application/json'}")
                 set headers of {_request} to (json from "{'Content-Type': 'application/json'}")
                \s
                 #or
                 set {_request}'s headers to "Content-Type: application/json", "Restrict: false"
                 set headers of {_request} to "Content-Type: application/json", "Restrict: false"
                \s
                 # reset the headers of the Request
                 reset {_request}'s headers
                 reset headers of {_request}
                 \s
                 <b>Response</b><br />
                 # getting the Response headers;
                 set {_response} to {_request}'s response
                 \s
                 send headers of {_response}
        \s""")
@Description({"set or get the current request headers", "get response headers"})
@Since({"2.9.9-pre Api Changes", "5.1"})
@ApiStatus.Experimental
public class ExprSimpleHeaders extends SimplePropertyExpression<Object, JsonElement> {
    @Override
    public @Nullable JsonElement convert(Object from) {
        if (from instanceof Request request) {
            var pairs = request.getHeader();
            if (pairs == null) return null;
            var o = new JsonObject();
            for (Pairs pair : pairs) {
                o.addProperty(pair.getKey(), pair.getValue());
            }
            return o;
        } else if (from instanceof Response response) {
            return response.headers();
        }
        return null;
    }

    @Override
    protected String getPropertyName() {
        return "headers of http request/response";
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    @SuppressWarnings("all")
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(JsonElement.class, String[].class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        Object expr = getExpr().getSingle(event);

        if (expr instanceof Request request) {
            if (mode == Changer.ChangeMode.SET) {
                LinkedList<Pairs> pairs = new LinkedList<>();
                for (Object d : delta) {
                    if (d instanceof String str) {
                        pairs.add(new Pairs(str));
                    } else if (d instanceof JsonElement json) {
                        json.getAsJsonObject().entrySet().forEach(entry
                            -> pairs.add(
                                new Pairs(entry.getKey() + ":" + entry.getValue().getAsString())
                            )
                        );
                    }
                }
                request.setHeader(pairs.toArray(new Pairs[0]));
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setHeader(new Pairs[0]);
            }
        }
    }
}
