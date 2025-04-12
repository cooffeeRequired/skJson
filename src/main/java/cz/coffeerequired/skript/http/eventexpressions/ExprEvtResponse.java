package cz.coffeerequired.skript.http.eventexpressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.requests.Response;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;

@Name("Response of http request [non blocking]")
@Description({"Represent response of http request", "You can get those values (status, status code, body, headers)"})
@Since("5.0")
public class ExprEvtResponse extends EventValueExpression<Response> {

    public ExprEvtResponse() {
        super(Response.class);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {

        if (!getParser().isCurrentEvent(HttpReceivedResponse.class)) {
            return false;
        }
        return super.init(expressions, matchedPattern, isDelayed, parser);
    }
}
