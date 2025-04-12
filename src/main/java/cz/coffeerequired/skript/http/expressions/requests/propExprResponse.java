package cz.coffeerequired.skript.http.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.requests.Request;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Response of request [sync]")
@Examples("send response of {_request}")
@Description({"Get last response from request"})
@Since({"2.9.9-pre Api Changes", "5.0"})
@ApiStatus.Experimental
public class propExprResponse extends PropertyExpression<Request, Response> {

    @Override
    protected Response @NotNull [] get(@NotNull Event event, Request @NotNull [] source) {
        return Arrays.stream(source).map(Request::getResponse).toArray(Response[]::new);
    }

    @Override
    public @NotNull Class<? extends Response> getReturnType() {
        return Response.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "last response of %s" + getExpr().toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }
}
