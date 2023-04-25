package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.utils.FileUtils;
import cz.coffee.core.cache.JsonWatcher;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static cz.coffee.SkJson.JSON_STORAGE;

@Name("Link json file with defined cache.")
@Description("You can works with the cache instead of reopening the file again & again.")
@Examples({
        "on load:",
        "\tlink json file \"<path to file>\" as \"mine.id\"",
        "\tlink json file \"<path to file>\" as \"mine.id\" and make jsonwatcher listen"
})
@Since("2.8.0 - performance & clean")
public class EffLinkJsonFile extends Effect {

    static {
        Skript.registerEffect(EffLinkJsonFile.class, "link [json] file %string% as %string% [(:and make) [json]watcher listen]");
    }

    private Expression<String> exprFileString, expressionID;
    private boolean asAlive;


    @Override
    protected void execute(@NotNull Event event) {
        final Map<JsonElement, File> map = new HashMap<>();
        String fileString = exprFileString.getSingle(event);
        String id = expressionID.getSingle(event);
        if (id == null || fileString == null) return;
        final File file =  new File(fileString);
        JsonElement json = FileUtils.get(file);
        assert json != null;
        map.put(json, file);
        if (!asAlive) {
            JSON_STORAGE.put(id, map);
        } else {
            JSON_STORAGE.put(id, map);
            if (!JsonWatcher.isRegistered(file)) JsonWatcher.register(id, file);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "link json file " + exprFileString.toString(event, b) + " as " + expressionID.toString(event, b) + (asAlive ? " and make jsonwatcher listen" : "");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        asAlive = parseResult.hasTag("and make");
        exprFileString = (Expression<String>) expressions[0];
        expressionID = (Expression<String>) expressions[1];
        return true;
    }
}
