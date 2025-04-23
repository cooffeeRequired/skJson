package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.cache.CacheStorageWatcher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Syntax;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Name("Cached storage watcher - un/bind from file")
@Description("Binds or unbinds a storage watcher to a file. This is used to watch for changes in the file and update the cache accordingly.")
@Syntax("bind storage watcher to <string>")
@Examples("""
            bind storage watcher to "player-storage"
            unbind storage watcher from "player-storage"
        """)
@Since("4.1 - API UPDATE")
public class AEffHandleWatcher extends AsyncEffect {

    private boolean isBindingMode;
    private Expression<String> expressionId;

    @Override
    protected void execute(Event event) {
        final String id = expressionId.getSingle(event);
        if (id == null) return;
        var cache = Api.getCache();

        CompletableFuture.runAsync(() -> {
            if (cache.containsKey(id)) {
                File[] file = new File[1];
                cache.get(id).forEach((j, f) -> file[0] = f);
                if (isBindingMode) {
                    if (!CacheStorageWatcher.Extern.hasRegistered(file[0])) {
                        try {
                            CacheStorageWatcher.Extern.register(id, file[0]);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    if (CacheStorageWatcher.Extern.hasRegistered(file[0])) {
                        CacheStorageWatcher.Extern.unregister(file[0]);
                    }
                }
            }
        });
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (!isBindingMode) return "unbind storage watcher from " + expressionId.toString(event, debug);
        return "bind storage watcher to " + expressionId.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        isBindingMode = matchedPattern == 0;
        expressionId = (Expression<String>) expressions[0];
        return expressionId == null;
    }
}
