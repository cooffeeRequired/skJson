package cz.coffee.skjson.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.google.gson.JsonElement;
import cz.coffee.skjson.skript.events.bukkit.EventWatcherSave;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: neděle (13.08.2023)
 */
public class WatcherEvent extends SkriptEvent {

    static {
        Skript.registerEvent("*JsonWatcher save", WatcherEvent.class, EventWatcherSave.class, "[json-] watcher save")
                .description("will only run when the json watcher notices a change in the file")
                .examples("on json watcher save")
                .since("2.8.6");

        EventValues.registerEventValue(EventWatcherSave.class, JsonElement.class,
                new Getter<>() {
                    @Override
                    public JsonElement get(EventWatcherSave event) {
                        return event.json();
                    }
                }, 0);

        EventValues.registerEventValue(EventWatcherSave.class, UUID.class,
                new Getter<>() {
                    @Override
                    public UUID get(EventWatcherSave event) {
                        return event.geId();
                    }
                }, 0);

        EventValues.registerEventValue(EventWatcherSave.class, File.class,
                new Getter<>() {
                    @Override
                    public File get(EventWatcherSave event) {
                        return event.link();
                    }
                }, 0);
    }
    @Override
    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        return event instanceof EventWatcherSave;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "Watcher event";
    }
}
