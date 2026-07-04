package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Api;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Get cached json storage")
@Examples("""
            set {_data} to json storage of id "homesdb"
            send json cache "utilconfig"
            loop all json caches:
                broadcast loop-value
        """)
@Since("2.8.0 - performance & clean")
@Description({
        "Returns the in-memory JSON cache bound to the given id.",
        "Aliases: `json storage of id …`, `cached json …`, `json cache …`.",
        "Without an id, returns every loaded cache."
})
public class ExprGetCacheStorage extends SimpleExpression<JsonElement> {

    private Expression<String> storedKeyExpr;
    private boolean allCaches;

    @Override
    protected @Nullable JsonElement @Nullable [] get(Event event) {
        var cache = Api.getCache();
        if (allCaches) {
            return cache.getJsons();
        }

        String storedKey = storedKeyExpr.getSingle(event);
        if (storedKey == null) {
            return new JsonElement[0];
        }
        var cacheLink = cache.getValuesById(storedKey);
        if (cacheLink == null) {
            return new JsonElement[0];
        }
        return new JsonElement[]{cacheLink.jsonElement()};
    }

    @Override
    public boolean isSingle() {
        return !allCaches;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        if (allCaches) {
            return "all json caches";
        }
        return "json cache " + storedKeyExpr.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        allCaches = matchedPattern >= 3;
        if (!allCaches && expressions.length > 0) {
            storedKeyExpr = (Expression<String>) expressions[0];
        }
        return allCaches || storedKeyExpr != null;
    }
}
