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

@Name("JsonWatcher - Stop listening to file")
@Description("That will be stop listen file for given id")
@Examples({"on script load",
        "\tstop jsonwatcher listen to id \"mine.id\""
})
@Since("2.8.0 - performance & clean")
public class EffStopListen extends AsyncEffect {

    static {
        Skript.registerEffect(EffStopListen.class, "stop [json]watcher listen to [id] %string%");
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

        if (JsonWatcher.isRegistered(file)) JsonWatcher.unregister(file);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "stop jsonwatcher listen to id" + exprId.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        exprId = (Expression<String>) expressions[0];
        return true;
    }
}
