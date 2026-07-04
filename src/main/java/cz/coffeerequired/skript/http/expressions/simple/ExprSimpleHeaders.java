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
                 send headers of {_request}
                 send {_request}'s headers
                \s
                 set headers of {_request} to parse "{""Content-Type"": ""application/json""}" as json
                 set {_request}'s headers to parse "{""Content-Type"": ""application/json""}" as json
                \s
                 set headers of {_request} to "Content-Type: application/json", "Restrict: false"
                 reset headers of {_request}
                 \s
                 <b>Response</b><br />
                 set {_response} to last response of {_request}
                 send headers of {_response}
        \s""")
@Description({
        "Gets, sets, or resets request headers as JSON or `Key: value` pairs.",
        "Response headers are read-only and returned as JSON."
})
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
