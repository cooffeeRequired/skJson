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
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;


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
                saveStorage(id, cache.get(id));
            }
        } else {
            cache.forEach(this::saveStorage);
        }

    }

    private void saveStorage(String id, ConcurrentHashMap<JsonElement, File> map) {
        map.forEach((j, f) -> {
            try {
                if ("Undefined".equals(f.getName())) {
                    SkJson.severe("Can't save storage id &r'" + id + "'&c because it is virtual storage");
                    return;
                }

                FileHandler.write(f.toString(), j, new String[]{"replace=true"}).whenComplete((b, error) -> {
                    if (error != null) {
                        SkJson.exception(error, "Cannot save storage id " + id);
                    } else {
                        SkJson.debug("Saved storage id " + id);
                    }
                });
            } catch (Exception ex) {
                SkJson.exception(ex, "Cannot save storage id " + id);
            }
        });
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
