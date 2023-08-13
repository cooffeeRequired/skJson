package cz.coffee.skjson.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.google.gson.JsonElement;
import cz.coffee.skjson.skript.events.bukkit.EventWatcherSave;

import java.io.File;
import java.util.UUID;

public class SimpleEvents {
    static {
        Skript.registerEvent("*JsonWatcher save", SimpleEvent.class, EventWatcherSave.class, "[json-] watcher save")
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
}
