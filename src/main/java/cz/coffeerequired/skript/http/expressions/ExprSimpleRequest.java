package cz.coffeerequired.skript.http.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.RequestMethod;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Prepare Web request")
@Examples("""
             set {_request} to prepare GET request on "https://raw.githubusercontent.com/SkJsonTeam/skJson/main/skjson.json"
             set {_request}'s request content to "{A: true}"
             set {_request}'s headers to "{Content-Type: application/json+vhd}"
             execute {_request}
            \s
             if response status of {_request} is "OK":
                 send response content of {_request}
                 send response status code of {_request}
                 send response headers of {_request}
        \s""")
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
public class ExprSimpleRequest extends SimpleExpression<Request> {

    private Expression<RequestMethod> exprMethod;
    private Expression<String> exprUrl;

    @Override
    protected Request @Nullable [] get(Event event) {
        var method = exprMethod.getSingle(event);
        var url = exprUrl.getSingle(event);
        return new Request[]{new Request(url, method)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Request> getReturnType() {
        return Request.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "prepare " + exprMethod.toString(event, debug) + " request to " + exprUrl.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprMethod = defendExpression(expressions[0]);
        exprUrl = defendExpression(expressions[1]);
        return canInitSafely(exprMethod) && canInitSafely(exprUrl);
    }
}
