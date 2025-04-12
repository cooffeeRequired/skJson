package cz.coffeerequired.skript.http.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ResponseRecieve extends SkriptEvent {
    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        return false;
    }

    @Override
    public boolean check(Event event) {
        return false;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "";
    }
}
