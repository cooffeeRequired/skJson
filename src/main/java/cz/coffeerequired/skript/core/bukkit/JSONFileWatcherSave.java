package cz.coffeerequired.skript.core.bukkit;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

@Getter
public class JSONFileWatcherSave extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final File linkedFile;
    private final String id;
    private final UUID uuid;
    @Setter
    private JsonElement json;

    public JSONFileWatcherSave(File linkedFile, String id, UUID uuid, JsonElement content) {
        super(true);
        this.linkedFile = linkedFile;
        this.id = id;
        this.uuid = uuid;
        this.json = content;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
