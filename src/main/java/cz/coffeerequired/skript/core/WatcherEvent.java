package cz.coffeerequired.skript.core;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import cz.coffeerequired.skript.core.bukkit.JSONFileWatcherSave;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WatcherEvent extends SkriptEvent {
    @Override
    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(@NotNull Event event) {
        return event instanceof JSONFileWatcherSave;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "Watcher event";
    }
}
