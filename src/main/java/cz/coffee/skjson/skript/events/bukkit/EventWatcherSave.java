package cz.coffee.skjson.skript.events.bukkit;

import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

public class EventWatcherSave extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final File link;
    private final Object id;
    private final UUID uuid;
    private JsonElement json;

    public EventWatcherSave(File link, String id, UUID uuid) {
        super(true);
        this.id = id;
        this.link = link;
        this.uuid = uuid;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public JsonElement json() {
        return this.json;
    }

    public Object id() {
        return this.id;
    }

    public UUID geId() {
        return this.uuid;
    }

    public void setJson(JsonElement json) {
        this.json = json;
    }

    public File link() {
        return this.link;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }


}
