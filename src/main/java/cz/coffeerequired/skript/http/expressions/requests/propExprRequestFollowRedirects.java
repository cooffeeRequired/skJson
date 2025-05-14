package cz.coffeerequired.skript.http.expressions.requests;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

@Name("Request follow redirects")
@Description("set/get or set requests configuration for follow redirects")
@Since("5.4")
@ApiStatus.Experimental
public class propExprRequestFollowRedirects extends PropertyExpression<Request, Boolean> {
    @Override
    protected Boolean[] get(Event event, Request[] requests) {
        return Arrays.stream(requests)
                .filter(Objects::nonNull)
                .map(Request::isFollowRedirects)
                .toArray(Boolean[]::new);
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "follow redirects of %s".formatted(getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(Boolean.class);
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
                if (delta[0] instanceof Boolean bool) {
                    request.setFollowRedirects(bool);
                }
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setFollowRedirects(true);
            }
        } catch (Exception ex) {
            SkJson.exception(ex, Objects.requireNonNull(getParser().getNode()).toString());
        }

    }
}
