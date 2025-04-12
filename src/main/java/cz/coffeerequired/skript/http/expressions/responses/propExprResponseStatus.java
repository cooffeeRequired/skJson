package cz.coffeerequired.skript.http.expressions.responses;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.requests.RequestStatus;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;


@Name("Response status")
@Examples("""
             # getting the Response status;
             set {_response} to {_request}'s response
             \s
             if {_response}'s status is "OK":
                 send body of {_response}
        \s""")
@Description({"Receive response status", "That could be OK, UNKNOWN or FAILED"})
@Since("5.0")
@ApiStatus.Experimental
public class propExprResponseStatus extends PropertyExpression<Response, String> {

    @Override
    protected String @NotNull [] get(@NotNull Event event, Response @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Response::status).map(RequestStatus::toString).toArray(String[]::new);
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "response status of " + getExpr().toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Response>) expressions[0]);
        return true;
    }
}