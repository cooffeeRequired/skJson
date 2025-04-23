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

import java.util.ArrayList;

@Name("Get cached json storage")
@Examples("""
            send json storage of id "test"
            send all json storages
        """)
@Since("2.8.0 - performance & clean")
@Description("Get cached json storage. This is used to get the cached json storage. If the id is not provided, it will return all json storages.")
public class ExprGetCacheStorage extends SimpleExpression<JsonElement> {

    private Expression<String> storedKeyExpr;
    private int line;

    @Override
    protected @Nullable JsonElement @Nullable [] get(Event event) {
        String storedKey = storedKeyExpr.getSingle(event);
        if (storedKey == null) return null;
        var cache = Api.getCache();
        if (line == 0) {
            if (cache.containsKey(storedKey)) {
                JsonElement[] json = new JsonElement[1];
                cache.get(storedKey).forEach((json_, f) -> json[0] = json_);
                return json;
            }
        } else {
            ArrayList<JsonElement> elements = new ArrayList<>();
            cache.forEach((s, m) -> m.forEach((json, f) -> elements.add(json)));
            return elements.toArray(new JsonElement[0]);
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
