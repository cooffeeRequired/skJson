package cz.coffee.skjson.skript.request;

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
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.requests.Request;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.Logger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static cz.coffee.skjson.utils.Util.fstring;


@Name("Request query params")
@Examples("""
            # getting the Request query params;
            send {_request}'s query params
            send query params of {_request}
            
            # setting the Request query params;
            set {_request}'s query params to "key:value", "key1:value1"
            set query params of {_request} to "key:value", "key1:value1"
            
            # adding the query param to the URL
            
            # reset the query params of the Request
            reset {_request}'s query params
            reset query params of {_request}
        """)
@Description("set/add/reset or get the current request query params")
@Since("3.0.2")
@ApiStatus.Experimental
public class propExprQueryParams extends PropertyExpression<Request, JsonElement> {

    static {
        SkJsonElements.registerProperty(propExprQueryParams.class, JsonElement.class, "[request] query param(s|meters)", "requests");
    }

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event event, Request @NotNull [] requests) {
        return Arrays.stream(requests)
                .filter(Objects::nonNull)
                .map(Request::getQueryParams)
                .map(ParserUtil::parse)
                .toArray(JsonElement[]::new);
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        assert event != null;
        return fstring("request params of %s", getExpr().toString(event, b));
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

    private HashMap<String, String[]> parseString(Object[] input) {
        HashMap<String, String[]> map = new HashMap<>();
        for (var e : input) {
            if (e instanceof String str) {
                if (str.contains(":")) {
                    var parts = str.split(":");
                    map.put(parts[0], new String[]{parts[1]});
                }
            }
        }
        return map;
    }

    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        try {
            var request = getExpr().getSingle(event);
            assert request != null;
            if (mode == Changer.ChangeMode.SET) {
                var parsed = parseString(delta);
                request.setQueryParam(parsed);
            } else if (mode == Changer.ChangeMode.ADD) {
                var parsed = parseString(delta);
                request.addQueryParam(parsed);
            } else if (mode == Changer.ChangeMode.RESET) {
                request.setQueryParam(new HashMap<>());
            }
        } catch (Exception ex) {
            Logger.error(ex, null, getParser().getNode());
        }

    }
}
