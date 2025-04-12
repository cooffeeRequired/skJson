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
import cz.coffeerequired.api.cache.CacheStorageWatcher;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletionException;


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


    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void execute(Event event) {
        String id = expressionJsonId.getSingle(event);
        String path = expressionFilePath.getSingle(event);
        if (id == null || path == null) {
            SkJson.warning("Cannot bind JSON file: ID or path is null");
            return;
        }

        var cache = Api.getCache();
        if (path.startsWith("~")) {
            path = Bukkit.getPluginManager().getPlugin("Skript").getDataFolder().getPath() + "/scripts/" + path.substring(1);
        }

        File file = new File(path);

        if (!file.exists()) {
            SkJson.exception(new IOException("File " + path + " does not exist"), 
                    "Cannot bind JSON file: File does not exist at path: " + path);
            return;
        }

        if (cache.containsKey(id)) {
            SkJson.info("Cache already contains key: &e'" + id + "'&r, skipping binding");
            return;
        }
        
        FileHandler.get(file).whenComplete((json, error) -> {
            if (error != null) {
                String errorMessage = "Cannot bind JSON file: " + file.getPath();
                if (error instanceof CompletionException) {
                    Throwable cause = error.getCause();
                    if (cause != null) {
                        errorMessage += " - " + cause.getMessage();
                    }
                } else {
                    errorMessage += " - " + error.getMessage();
                }
                SkJson.exception(error, errorMessage);
                return;
            }
            
            try {
                cache.addValue(id, json, file);
                if (withBinding && !CacheStorageWatcher.Extern.hasRegistered(file)) {
                    try {
                        CacheStorageWatcher.Extern.register(id, file);
                        SkJson.debug("Successfully bound JSON file: " + file.getPath() + " with ID: " + id + " and registered watcher");
                    } catch (Exception ex) {
                        SkJson.exception(ex, "Failed to register file watcher for: " + file.getPath() + " with ID: " + id);
                    }
                } else {
                    SkJson.debug("Successfully bound JSON file: " + file.getPath() + " with ID: " + id);
                }
            } catch (IllegalArgumentException ex) {
                SkJson.exception(ex, "Invalid JSON format in file: " + file.getPath());
            } catch (Exception ex) {
                SkJson.exception(ex, "Unexpected error while binding JSON file: " + file.getPath() + " - " + ex.getMessage());
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
