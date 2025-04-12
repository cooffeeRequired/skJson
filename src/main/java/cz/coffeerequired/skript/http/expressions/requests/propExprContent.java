package cz.coffeerequired.skript.propertyexpressions;

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
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;


@Name("Request content")
@Examples("""
                 # getting the Request content;
                 send {_request}'s content
                 send body of {_request}
                \s
                 # setting the Request content;
                 set {_request}'s body to (json from "{'Allow': false}")
                 set body of {_request} to (json from "{'Allow': false}")
                \s
                 # reset the body of the Request
                 reset {_request}'s body
                 reset body of {_request}
        \s""")
@Description("set/reset or get the current request body")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class propExprContent extends PropertyExpression<Request, JsonElement> {

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event event, Request @NotNull [] source) {
        return Arrays.stream(source).filter(Objects::nonNull).map(Request::getContent).toArray(JsonElement[]::new);
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "content of " + getExpr().toString(event, debug);
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
            case SET -> CollectionUtils.array(JsonElement.class, String[].class);
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
                    request.setContent(GsonParser.toJson(str));
                } else if (d instanceof JsonElement json) {
                    request.setContent(json);
                }
            }
        } else if (mode == Changer.ChangeMode.RESET) {
            request.setContent(new JsonObject());
        }
    }
}