package cz.coffeerequired.api.json.bukkit;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

/**
 * This event is dispatched when something happens in JsonFileWatcher.
 *
 */
@Getter
public class EventFileWatcher extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final File link;
    private final String id;
    private final UUID uuid;
    @Setter private JsonElement content;

    /**
     * Constructs a new EventFileWatcher.
     *
     * @param isAsync whether the event is asynchronous
     * @param link the file link associated with this event
     * @param id the ID associated with this event
     * @param uuid the UUID associated with this event
     * @param content the content associated with this event
     */
    public EventFileWatcher(boolean isAsync, File link, String id, UUID uuid, JsonElement content) {
        super(isAsync);
        this.link = link;
        this.id = id;
        this.uuid = uuid;
        this.content = content;
    }

    /**
     * Gets the list of event handlers.
     *
     * @return the list of event handlers
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
