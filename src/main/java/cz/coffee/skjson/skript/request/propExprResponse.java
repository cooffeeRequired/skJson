package cz.coffee.skjson.skript.request;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.requests.Request;
import cz.coffee.skjson.api.requests.Response;
import cz.coffee.skjson.parser.ParserUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

import static cz.coffee.skjson.utils.Util.fstring;

@Name("Response content, headers, status code, status")
@Examples("""
                send response status of {_request}
                send response status code of {_request}
                send response content of {_request}
                send response headers of {_request}
        """)
@Description({"get all response properties", "The status codes could be \"OK\", \"UNKNOWN\", \"FAILED\""})
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class propExprResponse extends PropertyExpression<Request, Object> {

    static {
        SkJsonElements.registerProperty(propExprResponse.class, Object.class,
                "response [:content|:headers|:status code|:status]",
                "requests"
        );
    }

    private String tag;

    @Override
    protected Object @NotNull [] get(@NotNull Event event, Request @NotNull [] source) {
        var rsp = Arrays.stream(source)
                .filter(Objects::nonNull)
                .map(Request::response);
        return switch (tag) {
            case "content" -> rsp.map((it) -> {
                var content = it.content();
                if (content == null) return null;
                if (content instanceof JsonElement element) {
                    return ParserUtil.from(element);
                } else {
                    return content;
                }
            }).toArray();
            case "headers", "header" -> rsp.map(Response::headers).toArray();
            case "status code" -> rsp.map(Response::statusCode).toArray();
            case "status" -> Arrays.stream(source).filter(Objects::nonNull).map(r -> r.status().toString()).toArray();
            default -> throw new IllegalStateException("Unexpected value: " + tag);
        };
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return fstring("response content of %s", getExpr().toString(event, debug));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        setExpr((Expression<? extends Request>) expressions[0]);
        tag = parseResult.tags.get(0);
        return true;
    }
}
