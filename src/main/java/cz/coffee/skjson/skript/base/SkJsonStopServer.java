package cz.coffee.skjson.skript.base;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffee.skjson.SkJson;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: středa (06.09.2023)
 */
public class SkJsonStopServer extends Effect {

    static {
        SkJson.registerEffect(SkJsonStopServer.class, "SkJson stop server with code [%-integer%]");
    }

    private Expression<Integer> code;
    @Override
    protected void execute(Event e) {
        if (code == null) {
            System.exit(0);
        }
        Integer c = code.getSingle(e);
        if (c == null) {
            System.exit(0);
        } else if (c == 1) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return e.toString();
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        code = (Expression<Integer>) exprs[0];
        return true;
    }
}
