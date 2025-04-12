package cz.coffeerequired.skript.http.expressions.responses;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;


@Name("Response body")
@Examples("""
             # getting the Response body;
             set {_response} to {_request}'s response
             \s
             send {_response}'s body
             send body of {_response}
        \s""")
@Description("Receive response content")
@Since("5.0")
@ApiStatus.Experimental
public class propExprResponseBody extends PropertyExpression<Response, Object> {

    @Override
    protected Object @NotNull [] get(@NotNull Event event, Response @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Response::content).toArray(Object[]::new);
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "response body of " + getExpr().toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Response>) expressions[0]);
        return true;
    }
}