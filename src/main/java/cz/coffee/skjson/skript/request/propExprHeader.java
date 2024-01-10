package cz.coffee.skjson.skript.request;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.requests.Pairs;
import cz.coffee.skjson.api.requests.Request;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static cz.coffee.skjson.utils.Util.fstring;

@Name("Request headers")
@Examples("""
        # getting the Request headers;
        send {_request}'s headers
        send headers of {_request}
        
        # setting the Request content;
        set {_request}'s headers to (json from "{'Content-Type': 'application/json'}")
        set headers of {_request} to (json from "{'Content-Type': 'application/json'}")
        
        #or
        set {_request}'s headers to "Content-Type: application/json", "Restrict: false"
        set headers of {_request} to "Content-Type: application/json", "Restrict: false"
""")
@Description("set or get the current request headers")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class propExprHeader extends PropertyExpression<Request, JsonElement> {

    static {
        SkJson.registerProperty(propExprHeader.class, JsonElement.class, "[request] header[s]", "requests");
    }
    @Override
    protected JsonElement @NotNull [] get(@NotNull Event event, Request @NotNull [] source) {
        var output = new JsonElement[source.length];
        for (var i = 0; i < source.length; i++) {
            var pairs = source[i].header();
            if (pairs == null) continue;
            var o = new JsonObject();
            for (Pairs pair : pairs) {
                o.addProperty(pair.getKey(), pair.getValue());
            }
            output[i] = o;
        }
        return output;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return fstring("headers of %s", getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        return true;
    }

    @Override
    @SuppressWarnings("all")
    public Class<?> [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return switch (mode) {
            case SET ->  CollectionUtils.array(JsonElement.class, String[].class);
            default -> null;
        };
    }

    @Override
    public void change(@NotNull Event event, Object @NotNull [] delta, Changer.@NotNull ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET) {
            var request = getExpr().getSingle(event);
            Pairs[] pairs = new Pairs[delta.length];
            assert request != null;
            for (var i = 0; i < delta.length; i++) {
                var d = delta[i];
                if (d instanceof String str) {
                    pairs[i] = new Pairs(str);
                } else if (d instanceof JsonElement json) {
                    pairs[i] = new Pairs(json);
                }
            }
            request.setHeader(pairs);
        }
    }
}