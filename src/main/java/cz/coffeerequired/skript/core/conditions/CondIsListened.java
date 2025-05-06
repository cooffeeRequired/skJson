package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.cache.CacheStorageWatcher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Name("JSON file is listened by storage watcher")
@Examples("send true if json storage with id \"my-json-storage\" is listened")
@Since("4.1 - API UPDATE")

public class CondIsListened extends Condition {

    private int line;
    private Expression<String> JSONIdExpression;

    @Override
    public boolean check(Event event) {
        String id = JSONIdExpression.getSingle(event);
        if (id == null) return false;

        var cache = Api.getCache();
        if (cache.containsKey(id)) {
            return (line == 0) == CacheStorageWatcher.Extern.hasRegistered(cache.get(id).file());
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return String.format("[skjson] the json storage with id %s %s listened",
                JSONIdExpression.toString(event, debug),
                (line == 0 ? "is" : "isn't"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        JSONIdExpression = (Expression<String>) expressions[0];
        line = matchedPattern;
        setNegated(matchedPattern == 1);
        return JSONIdExpression != null;
    }
}
