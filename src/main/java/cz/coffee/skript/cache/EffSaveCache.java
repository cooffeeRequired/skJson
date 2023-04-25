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
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

import static cz.coffee.SkJson.JSON_STORAGE;

@Name("Save cached json to file")
@Description("It's allow save cached json back to the file")
@Examples({
        "on unload:",
        "\tsave cached json \"test\""
})
@Since("2.8.0 - performance & clean")
public class EffSaveCache extends AsyncEffect {

    static {
        Skript.registerEffect(EffSaveCache.class,
                "[:async] save [cached] json %string%",
                "[:async] save all [cached] jsons"
        );
    }

    private int line;
    private boolean async;
    private Expression<String> externalExprID;

    @Override
    protected void execute(@NotNull Event e) {
        if (line == 0) {
            String id = externalExprID.getSingle(e);
            for (Map.Entry<String, Map<JsonElement, File>> mapEntry : JSON_STORAGE.entrySet()) {
                for (Map.Entry<JsonElement, File> entry : mapEntry.getValue().entrySet()) {
                    if (mapEntry.getKey().equals(id)) {
                        FileUtils.write(entry.getValue(), entry.getKey(), async);
                        return;
                    }
                }
            }
        } else {
            for (Map.Entry<String, Map<JsonElement, File>> mapEntry : JSON_STORAGE.entrySet()) {
                for (Map.Entry<JsonElement, File> entry : mapEntry.getValue().entrySet()) {
                    FileUtils.write(entry.getValue(), entry.getKey(), async);
                }
                return;
            }
        }

    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if (line == 0) return "save cached json " + externalExprID.toString(e, debug);
        else
            return "save all cached jsons";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        async = parseResult.hasTag(("async"));
        line = matchedPattern;
        if (line == 0) {
            externalExprID = (Expression<String>) exprs[0];
        }
        return true;
    }
}
