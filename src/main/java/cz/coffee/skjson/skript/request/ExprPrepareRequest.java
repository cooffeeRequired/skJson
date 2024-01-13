package cz.coffee.skjson.skript.request;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.requests.Request;
import cz.coffee.skjson.api.requests.RequestMethod;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Prepare Web request")
@Examples("""
            set {_request} to prepare new GET request on "https://raw.githubusercontent.com/SkJsonTeam/skJson/main/skjson.jsonn"
            set {_request}'s request content to @{"A": true}
            set {_request}'s headers to @{"Content-Type": "application/json+vhd"}
            send prepared {_request}
            
            if response status of {_request} is "OK":
                send response content of {_request}
                send response status code of {_request}
                send response headers of {_request}
        """)
@Description({
        "allowed methods are [GET, POST, PUT, HEAD, MOCK, DELETE, PATCH]",
        "allowed value type of content is Json or stringify json (Json as String) e.g. \"{\"\"Test\"\": true}\"",
        "allowed value type of header is Json or (Pairs e.g. \"Content-Type: application/Json\", \"Allow: restrict\")",
        "",
        "You can execute the request by 'send prepared {_request}', otherwise the request will be not sent, but the request will be still stored",
        "And you can get response status/content/headers like in the examples"
})
@Since("2.9.9-pre API changes")
@ApiStatus.Experimental
public class ExprPrepareRequest extends SimpleExpression<Request> {

    static {
        SkJsonElements.registerExpression(ExprPrepareRequest.class, Request.class, ExpressionType.SIMPLE,
                "prepare [new] %requestmethod% [request] on %string%"
        );
    }

    private Expression<RequestMethod> requestMethod;
    private Expression<String> requestUri;

    @Override
    protected Request @NotNull [] get(@NotNull Event event) {
        var method = requestMethod.getSingle(event);
        var uri = requestUri.getSingle(event);
        return new Request[]{new Request(uri, method)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Request> getReturnType() {
        return Request.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return Classes.getDebugMessage(this);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        requestMethod = defendExpression(expressions[0]);
        requestUri = defendExpression(expressions[1]);
        return canInitSafely(requestMethod) && canInitSafely(requestUri);
    }
}
