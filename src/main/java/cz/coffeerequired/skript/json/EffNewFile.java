package cz.coffeerequired.skript.json;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffNewFile extends Effect {
    @Override
    protected void execute(Event event) {

    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "json from ...";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
