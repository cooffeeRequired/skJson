package cz.coffeerequired.skript.propertyexpressions;

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


@Name("Response status code")
@Examples("""
             # getting the Response status code;
             set {_response} to {_request}'s response
             \s
             send status code of {_response}
        \s""")
@Description({"Receive response status code", "That could be anything from 100 to 504"})
@Since("5.0")
@ApiStatus.Experimental
public class propExprResponseStatusCode extends PropertyExpression<Response, Integer> {

    @Override
    protected Integer @NotNull [] get(@NotNull Event event, Response @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Response::statusCode).toArray(Integer[]::new);
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "response status code of " + getExpr().toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Response>) expressions[0]);
        return true;
    }
}