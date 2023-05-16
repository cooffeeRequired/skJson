package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.cache.JsonWatcher;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

import static cz.coffee.SkJson.JSON_STORAGE;

@SuppressWarnings("ALL")
@Name("Json file is listening")
@Description("Check if the file for given id is listening via JsonWatcher")
@Examples({
        "on load:",
        "\tif cached json \"test\" is listen:",
        "\t\tsend true"
})
@Since("2.8.0 - performance & clean")

public class CondJsonIsListened extends Condition {

    static {
        Skript.registerCondition(CondJsonIsListened.class,
                "[cached] json [id] %string% is listen",
                "[cached] json [id] %string% is(n't| not) listen"
        );
    }

    private int line;
    private Expression<String> exprId;

    @Override
    public boolean check(@NotNull Event event) {
        String id = exprId.getSingle(event);


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
        return (line == 0) == JsonWatcher.isRegistered(file);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return String.format("json id %s %s listened", exprId.toString(event, b), (line == 0 ? "is" : "isn't"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        exprId = (Expression<String>) expressions[0];
        line = i;
        setNegated(i == 1);
        return true;
    }
}
