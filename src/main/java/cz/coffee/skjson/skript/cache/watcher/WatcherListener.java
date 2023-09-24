package cz.coffee.skjson.skript.cache.watcher;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skjson.api.Cache.JsonCache;
import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Config;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public abstract class WatcherListener {
    @Name("JsonWatcher - Start/Stop listening to file")
    @Description("You can register listener for json file, and while the file is updated the cache for this file will be also so.")
    @Examples({"on script load",
            "\tmake json watcher listen to \"mine.id\"",
            "\tstop json watcher listen to \"mine.id\""
    })
    @Since("2.8.0 - performance & clean")
    public static class MakeStopListen extends Effect {

        static {
            Skript.registerEffect(MakeStopListen.class, "(:make|:stop) [json] watcher listen to %string%");
        }

        private Expression<String> exprId;
        private boolean make, stop;

        @Override
        protected void execute(@NotNull Event event) {
            final String id = exprId.getSingle(event);
            JsonCache<String, JsonElement, File> cache = Config.getCache();

            if (id == null) return;
            CompletableFuture.runAsync(() -> {
                if (cache.containsKey(id)) {
                    AtomicReference<File> file = new AtomicReference<>();
                    cache.get(id).forEach((key, f) -> file.set(f));
                    if (make) if (!JsonWatcher.isRegistered(file.get())) JsonWatcher.register(id, file.get());
                    else if (stop) if (JsonWatcher.isRegistered(file.get())) JsonWatcher.unregister(file.get());
                }
            });
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            return "make json watcher listen to " + exprId.toString(event, b);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
            exprId = (Expression<String>) expressions[0];
            make = parseResult.hasTag("make");
            stop = parseResult.hasTag("stop");
            return true;
        }
    }

    @Name("Json file is listening")
    @Description("Check if the file for given id is listening via JsonWatcher")
    @Examples({
            "on load:",
            "\tif json \"test\" is listen:",
            "\t\tsend true"
    })
    @Since("2.8.0 - performance & clean")

    public static class CondJsonIsListened extends Condition {

        static {
            Skript.registerCondition(CondJsonIsListened.class,
                    "json %string% is listen",
                    "json %string% is(n't| not) listen"
            );
        }

        private int line;
        private Expression<String> exprId;

        @Override
        public boolean check(@NotNull Event event) {
            String id = exprId.getSingle(event);

            JsonCache<String, JsonElement, File> cache = Config.getCache();

            if (cache.containsKey(id)) {
                AtomicReference<File> file = new AtomicReference<>();
                cache.get(id).forEach((key, f) -> file.set(f));
                return (line == 0) == JsonWatcher.isRegistered(file.get());
            }
            return false;
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


}
