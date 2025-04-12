package cz.coffeerequired.skript.propertyexpressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.requests.Response;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;


@Name("Response headers")
@Examples("""
             # getting the Response headers;
             set {_response} to {_request}'s response
             \s
             send headers of {_response}
        \s""")
@Description({"Receive response headers", "Returns headers from response"})
@Since("5.0")
@ApiStatus.Experimental
public class propExprResponseHeaders extends PropertyExpression<Response, JsonElement> {

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event event, Response @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Response::headers).toArray(JsonElement[]::new);
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "response headers of " + getExpr().toString(event, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Response>) expressions[0]);
        return true;
    }
}