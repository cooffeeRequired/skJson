package cz.coffee.skriptgson.skript.effect; /**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */

import com.google.gson.JsonElement;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings({"unused","unchecked", "NullableProblems"})
public class EffTest extends Effect {
    
    static {
        Skript.registerEffect(EffTest.class, "skript[-]gson %jsonelement%");
    }
    private Expression<JsonElement> input;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        input = (Expression<JsonElement>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event e) {
        Object single = input.getSingle(e);
        assert single != null;
        SkriptGson.info("Class check " + single.getClass().toString());
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "skript-gson test effect";
    }

}
