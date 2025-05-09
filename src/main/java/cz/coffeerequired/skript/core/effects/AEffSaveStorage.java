package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.cache.CacheLink;
import cz.coffeerequired.support.Performance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;


@Name("Save cached json to file")
@Description("Saves the cached json to a file. This is used to save the cache to a file.")
@Examples("save json storage id \"my-json-storage\"")
@Since("4.1 - API UPDATE")
public class AEffSaveStorage extends AsyncEffect {

    private Mode mode;
    private Expression<String> expressionId;

    @Override
    protected void execute(Event event) {
        var cache = Api.getCache();

        if (mode.equals(Mode.SINGLE)) {
            String id = expressionId.getSingle(event);
            if (cache.containsKey(id)) {
                saveStorage(id, cache.getValuesById(id));
            }
        } else {
            cache.forEach(this::saveStorage);
        }

    }

    private void saveStorage(String id, CacheLink<JsonElement, File> cacheLink) {
        Performance.exceptionally(() -> {
            if (cacheLink.getFile().isPresent()) {
                var file = cacheLink.getFile().get();

                if (file.getName().equals("Undefined")) {
                    SkJson.severe("Can't save storage id &r'" + id + "'&c because it is virtual storage");
                    return;
                }

                JsonElement json;
                if ((json = cacheLink.jsonElement()) != null) {
                    FileHandler.write(file.toString(), json, new String[]{"replace=true"}).whenComplete((b, error) -> {
                        if (error != null) {
                            SkJson.exception(error, "Cannot save storage id " + id);
                        } else {
                            SkJson.debug("Saved storage id " + id);
                        }
                    });
                }
            }
        }).exception((e) -> SkJson.exception(e, "Cannot save storage id " + id));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "save " + (mode.equals(Mode.SINGLE) ? "storage id " + expressionId.toString(event, debug) : "all storages");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        mode = matchedPattern == 0 ? Mode.SINGLE : Mode.ALL;
        if (mode == Mode.SINGLE) {
            expressionId = (Expression<String>) expressions[0];
        }
        return true;
    }

    private enum Mode {
        SINGLE, ALL
    }
}
