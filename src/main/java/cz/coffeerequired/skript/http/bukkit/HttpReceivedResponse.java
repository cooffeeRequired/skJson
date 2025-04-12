package cz.coffeerequired.skript.http.bukkit;

import cz.coffeerequired.api.requests.Response;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


@Setter
@Getter
public class HttpReceivedResponse extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Response response;

    public HttpReceivedResponse() {
        super(false);
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HttpReceivedResponse that = (HttpReceivedResponse) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(response);
    }
}
