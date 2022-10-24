package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("is Json empty")
@Description("Checks if a cached Json is empty.")
@Examples({
        "json {-e} is empty: ",
        "\tbroacast \"is empty\"",
})
@Since("1.0")

@SuppressWarnings({"unchecked","unused"})
public class CondEmpty extends Condition {

    static {
        Skript.registerCondition(CondEmpty.class,
                "json %jsonelement% is empty ",
                "json %jsonelement% is(n't| not) empty"
        );
    }

    private Expression<JsonElement> check;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        check = (Expression<JsonElement>) exprs[0];
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (check.getSingle(e).isJsonArray())
            return (pattern == 0) == check.getSingle(e).getAsJsonArray().isEmpty();
        if (check.getSingle(e).isJsonObject())
            return (pattern == 0) == check.getSingle(e).getAsJsonObject().entrySet().isEmpty();
        if (check.getSingle(e).isJsonNull())
            return true;
        if (check.getSingle(e).isJsonPrimitive())
            return check.getSingle(e).getAsJsonPrimitive() == null;

        return false;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "json " + check.toString(e,debug) + (isNegated() ? " is empty" : "isn't empty");
    }

}
