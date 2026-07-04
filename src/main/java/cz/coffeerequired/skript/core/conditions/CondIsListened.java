package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Description;
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
@Description({
        "Checks whether a cache id has an active file watcher.",
        "Aliases: `json cache … is watched` and `json file watcher for … is active`."
})
@Examples("""
        if json cache "homesdb" is watched:
            send true
        if the json storage with id "homesdb" is listened:
            send true
        """)
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
            boolean expectListened = line % 2 == 0;
            return expectListened == CacheStorageWatcher.Extern.hasRegistered(cache.getValuesById(id).file());
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
        setNegated(matchedPattern % 2 == 1);
        return JSONIdExpression != null;
    }
}
