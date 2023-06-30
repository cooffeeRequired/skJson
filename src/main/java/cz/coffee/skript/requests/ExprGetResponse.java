package cz.coffee.skript.requests;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import cz.coffee.core.requests.HttpHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;

import static cz.coffee.SkJson.RESPONSES;
import static cz.coffee.core.utils.AdapterUtils.parseItem;


@Name("Get last response")
@Description("Get last response of executed request")
@Examples({
        "execute HEAD request to \"https://dummyjson.com/products/1\"",
        "send body of current request with pretty print",
        "send code of current request",
        "send connection headers of current request",
        "send user headers of current requestL",
        "send url of current request"
})
@Since("2.8.3, 2.8.0 performance & clean")
public class ExprGetResponse extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprGetResponse.class, Object.class, ExpressionType.SIMPLE,
                "[SkJson] [(:current|:previous)] request's (2:body|3:code|4:connection headers|[user] (5:headers)|6:url)",
                "[skJson] (2:body|3:code|4:connection headers|[user] (5:headers)|6:url) of [(:current|:previous)] request"
        );
    }

    boolean isCurrent;
    private int tag;

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        Object[] result = new Object[1];

        if (RESPONSES[0] != null) {
            HttpHandler.Response current = RESPONSES[0];
            HttpHandler.Response previous = HttpHandler.Response.of(null, null, null, null, 0);
            if (!isCurrent && RESPONSES[1] != null) previous = RESPONSES[1];

            switch (tag) {
                case 2:
                    String st = isCurrent ? current.rawBody() : previous.rawBody();
                    if (!isCurrent && st == null)
                        return new Object[0];
                    else
                        result[0] = parseItem(st, st.getClass());
                    break;
                case 3:
                    result[0] = isCurrent ? current.getStatusCode() : previous.getStatusCode();
                    break;
                case 4:
                    result[0] = isCurrent ? current.connHeaders(true) : previous.connHeaders(true);
                    break;
                case 5:
                    result[0] = isCurrent ? current.headers(false) : previous.headers(true);
                    break;
                case 6:
                    try {
                        result[0] = isCurrent ? current.getUrl() : previous.getUrl();
                    } catch (MalformedURLException ex) {
                        return new Object[0];
                    }
                    break;
            }
        }
        return result;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return Classes.getDebugMessage(e);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        isCurrent = parseResult.hasTag("current");
        tag = parseResult.mark;
        return true;
    }

}
