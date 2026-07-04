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
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


@Name("Request query params")
@Examples("""
             send query params of {_request}
             send {_request}'s query params
            \s
             set query params of {_request} to "key:value", "key1:value1"
             add "async:1" to query params of {_request}
             reset query params of {_request}
        \s""")
@Description({
        "Gets, sets, adds, or resets URL query parameters on a prepared request.",
        "Pairs use the `key:value` format; multiple pairs can be comma-separated."
})
@Since("3.0.2")
@ApiStatus.Experimental
public class propExprQueryParams extends PropertyExpression<Request, JsonElement> {

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event event, Request @NotNull [] requests) {
        return Arrays.stream(requests)
                .filter(Objects::nonNull)
                .map(Request::getQueryParams)
                .map(Parser::toJson)
                .toArray(JsonElement[]::new);
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        assert event != null;
        return "request params of %s" + getExpr().toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    @Override
    @SuppressWarnings("all")
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(String[].class, String.class);
            case ADD -> CollectionUtils.array(String[].class, String.class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    private HashMap<String, String> parseString(Object[] input) {
        HashMap<String, String> map = new HashMap<>();
        for (var e : input) {
            collectQueryParams(e, map);
        }
        return map;
    }

    private void collectQueryParams(Object value, HashMap<String, String> map) {
        if (value == null) {
            return;
        }
        if (value instanceof Object[] array) {
            for (Object element : array) {
                collectQueryParams(element, map);
            }
            return;
        }
        if (value instanceof Collection<?> collection) {
            for (Object element : collection) {
                collectQueryParams(element, map);
            }
            return;
        }
        if (value instanceof String str) {
            for (String part : str.split(",")) {
                addQueryParam(part.trim(), map);
            }
        }
    }

    private void addQueryParam(String pair, HashMap<String, String> map) {
        if (!pair.contains(":")) {
            return;
        }
        var parts = pair.split(":", 2);
        if (!parts[0].isEmpty()) {
            map.put(parts[0].trim(), parts[1].trim());
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        try {
            var request = getExpr().getSingle(event);
            assert request != null;
            if (mode == Changer.ChangeMode.SET) {
                var parsed = parseString(delta);
                request.setQueryParams(parsed);
            } else if (mode == Changer.ChangeMode.ADD) {
                var parsed = parseString(delta);
                request.addQueryParam(parsed);
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setQueryParams(new HashMap<>());
            }
        } catch (Exception ex) {
            SkJson.exception(ex, Objects.requireNonNull(getParser().getNode()).toString());
        }

    }
}
