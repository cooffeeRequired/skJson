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
import cz.coffeerequired.api.cache.CacheStorageWatcher;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Unbind the storage for the given id")
@Examples("unbind json storage of id \"my-json-storage\"")
@Description("Unbinds the storage for the given id. This is used to unbind the storage from the file.")
@Since("4.1 - API UPDATE")
public class AEffUnbindFile extends AsyncEffect {

    private Expression<String> expressionId;

    @Override
    protected void execute(Event event) {
        String id = expressionId.getSingle(event);
        if (id == null) return;

        var cache = Api.getCache();
        SkJson.debug("Trying to unbind storage id '" + id + "' from file");
        if (cache.containsKey(id)) {
            var fileOptional = cache.get(id).getFile();
            if (fileOptional.isPresent()) {
                var file_ = fileOptional.get();

                if (file_.getName().equals("Undefined")) {
                    cache.removeIfPresent(id);
                    return;
                }

                if (CacheStorageWatcher.Extern.hasRegistered(file_)) {
                    CacheStorageWatcher.Extern.unregister(file_);
                    SkJson.debug("Unbound storage watcher id &e'" + id + "'&r from file " + file_.getName());
                }

                cache.removeIfPresent(id);
                SkJson.debug("Unbound storage id &e'" + id + "'&r from file '" + file_.getName() + "'");
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
