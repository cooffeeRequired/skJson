package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.util.newSkriptGsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class EffTest extends Effect {

    static {
        Skript.registerEffect(EffTest.class, "Test Gson");
    }

    @Override
    protected void execute(Event e) {
        JsonElement json = JsonParser.parseString("{'A': {'B': {}}}");
        JsonElement parse = JsonParser.parseString("{'A': 'XXXXXXXXXXX'}");

        System.out.println("APPEND "+newSkriptGsonUtils.append(json, parse, null, "A:B"));

        System.out.println("CHECK KEY "+newSkriptGsonUtils.check(json, "B", newSkriptGsonUtils.Type.KEY));
        System.out.println("CHECK VALUE NON JSON "+newSkriptGsonUtils.check(json, "1", newSkriptGsonUtils.Type.VALUE));
        System.out.println("CHECK VALUE JSON "+newSkriptGsonUtils.check(json, "{'B': 1}", newSkriptGsonUtils.Type.VALUE));


        System.out.println("Change KEY "+newSkriptGsonUtils.change(json, "A", "Y", newSkriptGsonUtils.Type.KEY));
        System.out.println("Change VALUE "+newSkriptGsonUtils.change(json, "B", parse, newSkriptGsonUtils.Type.VALUE));

    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }
}
