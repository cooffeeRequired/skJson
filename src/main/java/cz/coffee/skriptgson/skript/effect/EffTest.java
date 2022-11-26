package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EffTest extends Effect {

    private Expression<String > code;

    static {
        Skript.registerEffect(EffTest.class, "test skript-gson %string%");
    }

    @Override
    protected void execute(@NotNull Event e) {

        String code2 = code.getSingle(e);

        if(Objects.equals(code2, "new utils")) {
            JsonElement json = JsonParser.parseString("{'A': {'B': {}}}");
            JsonElement parse = JsonParser.parseString("{'A': 'XXXXXXXXXXX'}");

            System.out.println("APPEND "+ GsonUtils.append(json, parse, null, "A:B"));

            System.out.println("CHECK KEY "+ GsonUtils.check(json, "B", GsonUtils.Type.KEY));
            System.out.println("CHECK VALUE NON JSON "+ GsonUtils.check(json, "1", GsonUtils.Type.VALUE));
            System.out.println("CHECK VALUE JSON "+ GsonUtils.check(json, "{'B': 1}", GsonUtils.Type.VALUE));


            System.out.println("Change KEY "+ GsonUtils.change(json, "A", "Y", GsonUtils.Type.KEY));
            System.out.println("Change VALUE "+ GsonUtils.change(json, "B", parse, GsonUtils.Type.VALUE));
        } else {
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        code = (Expression<String>) exprs[0];
        return true;
    }
}
