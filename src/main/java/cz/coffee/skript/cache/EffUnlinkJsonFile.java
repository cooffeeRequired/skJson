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
import cz.coffee.core.cache.JsonWatcher;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static cz.coffee.SkJson.JSON_STORAGE;


@SuppressWarnings("ALL")
@Name("UnLink or Unload json file")
@Description("You can unload the json file.")
@Examples({
        "on load:",
        "\tunlink json \"mine.id\""
})
@Since("2.8.0 - performance & clean")
public class EffUnlinkJsonFile extends AsyncEffect {

    static {
        Skript.registerEffect(EffUnlinkJsonFile.class, "unlink json %string%");
    }

    private Expression<String> exprID;


    @Override
    protected void execute(@NotNull Event event) {
        String id = exprID.getSingle(event);
        if (id == null) return;
        if (JSON_STORAGE.containsKey(id)) {
            AtomicReference<File> file = new AtomicReference<>();
            JSON_STORAGE.get(id).forEach((json, file0) -> file.set(file0));
            JsonWatcher.unregister(file.get());
            JSON_STORAGE.remove(id);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "unlink json " + exprID.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        exprID = (Expression<String>) expressions[0];
        return true;
    }
}
