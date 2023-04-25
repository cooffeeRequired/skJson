package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.cache.JsonWatcher;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

import static cz.coffee.SkJson.JSON_STORAGE;

@Name("JsonWatcher - Start listening to file")
@Description("You can register listener for json file, and while the file is updated the cache for this file will be also so.")
@Examples({"on script load",
        "\tmake jsonwatcher listen to \"mine.id\""
})
@Since("2.8.0 - performance & clean")
public class EffMakeListen extends AsyncEffect {

    static {
        Skript.registerEffect(EffMakeListen.class, "make [json]watcher listen to %string%");
    }

    private Expression<String> exprId;

    @Override
    protected void execute(@NotNull Event event) {
        final String id = exprId.getSingle(event);
        if (id == null) return;
        File file = null;
        for (Map.Entry<String, Map<JsonElement, File>> stringMapEntry : JSON_STORAGE.entrySet()) {
            for (Map.Entry<JsonElement, File> jsonElementFileEntry : stringMapEntry.getValue().entrySet()) {
                if (stringMapEntry.getKey().equals(id)) {
                    file = jsonElementFileEntry.getValue();
                    break;
                }
            }
            if (file != null) break;
        }
        if (!JsonWatcher.isRegistered(file)) JsonWatcher.register(id, file);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "make jsonwatcher listen to " + exprId.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        exprId = (Expression<String>) expressions[0];
        return true;
    }
}
