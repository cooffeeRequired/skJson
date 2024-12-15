package cz.coffeerequired.skript.cache;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.support.Performance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprGetCacheStorage extends SimpleExpression<JsonElement> {

    private Expression<String> storedKeyExpr;
    private int line;

    @Override
    protected @Nullable JsonElement[] get(Event event) {
        var p = new Performance();

        String storedKey = storedKeyExpr.getSingle(event);
        if (storedKey == null) return null;
        var cache = Api.getCache();
        if (line == 0) {
            if (cache.containsKey(storedKey)) {
                JsonElement[] json = new JsonElement[1];
                cache.get(storedKey).forEach((json_, _) -> json[0] = json_);
                p.stop();
                SkJson.logger().info("Reading memory cache: " + p.toHumanTime());
                return json;
            }
        }
        return new JsonElement[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        if (line == 0) {
            assert event != null;
            return "get json " + storedKeyExpr.toString(event, b);
        } else {
            return "all jsons";
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        line = matchedPattern;
        storedKeyExpr = (Expression<String>) expressions[0];
        return true;
    }
}
