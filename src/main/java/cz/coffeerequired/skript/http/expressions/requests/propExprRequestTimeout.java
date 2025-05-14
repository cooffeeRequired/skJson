package cz.coffeerequired.skript.http.expressions.requests;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

@Name("Request timeout")
@Examples("""
             # get request timeout
             send {_request}'s timeout
            \s
             # set Request timeout
             set {_request}'s timeout to 5 seconds
             \s
             # reset Request timeout
             reset {_request}'s timeout
        \s""")
@Description("set/get or reset the current timeout of request")
@Since("5.4")
@ApiStatus.Experimental
public class propExprRequestTimeout extends PropertyExpression<Request, Timespan> {
    @Override
    protected Timespan[] get(Event event, Request[] requests) {
        return Arrays.stream(requests)
                .filter(Objects::nonNull)
                .map(Request::getTimeout)
                .toArray(Timespan[]::new);
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "timeout of %s".formatted(getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(Timespan.class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        try {
            var request = getExpr().getSingle(event);
            assert request != null;
            if (mode == Changer.ChangeMode.SET) {
                if (delta[0] instanceof Timespan timespan) {
                    request.setTimeout(timespan);
                }
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setTimeout(Timespan.fromDuration(Duration.of(5, ChronoUnit.SECONDS)));
            }
        } catch (Exception ex) {
            SkJson.exception(ex, Objects.requireNonNull(getParser().getNode()).toString());
        }

    }
}
