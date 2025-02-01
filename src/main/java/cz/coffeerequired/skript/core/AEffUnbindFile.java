package cz.coffeerequired.skript.core;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.CacheStorageWatcher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Name("Unbind the storage for the given id")
@Examples("""
            unbind json storage id "my-json-storage"
        """)
@Since("4.1 - API UPDATE")
public class AEffUnbindFile extends AsyncEffect {

    private Expression<String> expressionId;

    @Override
    protected void execute(Event event) {
        String id = expressionId.getSingle(event);
        if (id == null) return;

        var cache = Api.getCache();

        var file = new File[1];

        if (cache.containsKey(id)) {
            cache.get(id).forEach((j, v) -> file[0] = v);

            SkJson.debug("is storage bound: " + CacheStorageWatcher.Extern.hasRegistered(file[0]));

            if (file[0].getName().equals("Undefined")) {
                cache.removeIfPresent(id);
                SkJson.debug("Unbinding json storage id " + id);
            } else {
                if (CacheStorageWatcher.Extern.hasRegistered(file[0])) {
                    CacheStorageWatcher.Extern.unregister(file[0]);
                    SkJson.debug("Unregistering storage: " + file[0]);

                    SkJson.debug("Unbinding json storage id " + id);

                    cache.removeIfPresent(id);
                }
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "unbind json storage id " + expressionId.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        expressionId = (Expression<String>) expressions[0];
        return expressionId != null;
    }
}
