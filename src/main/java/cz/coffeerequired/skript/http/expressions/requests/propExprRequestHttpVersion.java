package cz.coffeerequired.skript.http.expressions.requests;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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
import java.util.regex.Pattern;

@Name("Request http version")
@Examples("""
             # get request http version of request
             send {_request}'s http version
            \s
             # set request http version of request
             set {_request}'s http version to "HTTP/1.1"
             \s
             # reset request http version of request
             reset {_request}'s http version
        \s""")
@Description("set/get or reset the current http version of request")
@Since("5.4")
@ApiStatus.Experimental
public class propExprRequestHttpVersion extends PropertyExpression<Request, String> {
    @Override
    protected String[] get(Event event, Request[] requests) {
        return Arrays.stream(requests)
                .filter(Objects::nonNull)
                .map(Request::getHttpVersion)
                .toArray(String[]::new);
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "http version of %s".formatted(getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(String.class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    final static Pattern pattern = Pattern.compile("HTTP/\\d+(\\.\\d+)?");

    private boolean isCorrect(String version) {
        return pattern.matcher(version).matches();
    }
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        try {
            var request = getExpr().getSingle(event);
            assert request != null;
            if (mode == Changer.ChangeMode.SET) {
                assert delta != null;
                if (delta[0] instanceof String version) {
                    if (isCorrect(version)) {
                        request.setHttpVersion(version);
                    } else {
                        SkJson.severe(getParser().getNode(), "Cannot set http version cuz of invalid http version: %s. Needed format is HTTP/<number>".formatted(version));
                    }
                }
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setHttpVersion("HTTP/1.1");
            }
        } catch (Exception ex) {
            SkJson.exception(ex, Objects.requireNonNull(getParser().getNode()).toString());
        }

    }
}
