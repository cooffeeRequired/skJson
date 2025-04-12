package cz.coffeerequired.skript.http.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResponseReceive extends SkriptEvent {
    @Override
    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        return event instanceof HttpReceivedResponse;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "http response received event";
    }
}
