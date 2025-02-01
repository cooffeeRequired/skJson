package cz.coffeerequired.skript.core;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.CacheStorageWatcher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;


@Name("JSON file is listened by storage watcher")
@Examples("""
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
            File[] tempFile = new File[1];
            cache.get(id).forEach((j, file) -> tempFile[0] = file);
            return (line == 0) == CacheStorageWatcher.Extern.hasRegistered(tempFile[0]);
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return String.format("json storage of id %s %s listened", JSONIdExpression.toString(event, debug), (line == 0 ? "is" : "isn't"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        JSONIdExpression = (Expression<String>) expressions[0];
        line = matchedPattern;
        setNegated(matchedPattern == 1);
        return JSONIdExpression == null;
    }
}
