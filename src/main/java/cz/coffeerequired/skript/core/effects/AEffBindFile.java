package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.json.CacheStorageWatcher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;


@Name("Bind JSON file as simply string id")
@Description("Allows you create an simply string if for bound json file")
@Examples("""
        bind json file "jsons/mow.json" as "my-template-json-storage"
        bind json file "json/mow.json" as "my-template-json-storage" and let bind storage watcher
        """)
@Since("2.8.0 - performance & clean")
public class AEffBindFile extends AsyncEffect {

    private Expression<String> expressionFilePath, expressionJsonId;
    private boolean withBinding;


    @Override
    protected void execute(Event event) {
        String id = expressionJsonId.getSingle(event);
        String path = expressionFilePath.getSingle(event);
        if (id == null || path == null) return;

        var cache = Api.getCache();
        File file = new File(path);

        if (!file.exists()) {
            var error = new IOException("File " + path + " does not exist");
            SkJson.exception(error, "Cannot bind json-file");
            return;
        }

        if (cache.containsKey(id)) return;
        FileHandler.get(file).whenComplete((json, error) -> {
            if (error != null) {
                SkJson.exception(error, "Cannot bind json-file");
                return;
            }
            try {
                cache.addValue(id, json, file);
                if (withBinding && !CacheStorageWatcher.Extern.hasRegistered(file)) {
                    CacheStorageWatcher.Extern.register(id, file);
                }
            } catch (Exception ex) {
                SkJson.exception(ex, "Cannot bind json-file");
            }
        });
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (withBinding)
            return "bind json file " + expressionFilePath.toString(event, debug) + " to " + expressionJsonId.toString(event, debug) + " and let bind storage watcher";
        return "bind json file " + expressionFilePath.toString(event, debug) + " to " + expressionJsonId.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        withBinding = matchedPattern == 1;
        expressionFilePath = (Expression<String>) expressions[0];
        expressionJsonId = (Expression<String>) expressions[1];
        getParser().setHasDelayBefore(Kleenean.TRUE);
        return expressionFilePath != null && expressionJsonId != null;
    }
}
