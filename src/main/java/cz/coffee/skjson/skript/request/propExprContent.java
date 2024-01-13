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
import com.google.gson.JsonObject;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

import static cz.coffee.skjson.parser.ParserUtil.parse;
import static cz.coffee.skjson.utils.Util.fstring;

@Name("Request content")
@Examples("""
                # getting the Request content;
                send {_request}'s content
                send content of {_request}
                
                # setting the Request content;
                set {_request}'s content to (json from "{'Allow': false}")
                set content of {_request} to (json from "{'Allow': false}")
                
                # reset the content of the Request
                reset {_request}'s content
                reset content of {_request}
        """)
@Description("set/reset or get the current request content")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class propExprContent extends PropertyExpression<Request, JsonElement> {

    static {
        SkJsonElements.registerProperty(propExprContent.class, JsonElement.class, "[request] content", "requests");
    }

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event event, Request @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Request::content).toArray(JsonElement[]::new);
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return fstring("content of %s", getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    @Override
    @SuppressWarnings("all")
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(JsonElement.class, String.class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        var request = getExpr().getSingle(event);
        assert request != null;
        if (mode == Changer.ChangeMode.SET) {
            for (var d : delta) {
                if (d instanceof String str) {
                    request.setContent(parse(str));
                } else if (d instanceof JsonElement json) {
                    request.setContent(json);
                }
            }
        } else if (mode == Changer.ChangeMode.RESET) {
            request.setContent(new JsonObject());
        }
    }
}