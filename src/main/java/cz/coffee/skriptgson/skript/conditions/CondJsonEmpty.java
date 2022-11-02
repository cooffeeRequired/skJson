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
        "\tbroadcast \"is empty\"",
})
@Since("1.0")

@SuppressWarnings({"unchecked","unused","NullableProblems"})
public class CondJsonEmpty extends Condition {

    static {
        Skript.registerCondition(CondJsonEmpty.class,
                "json[(-| )element] %jsonelement% is empty",
                "json[(-| )element] %jsonelement% is(n't| not) empty"
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

    /**
     * * Fixed problem (Repeatedly .getSingle(e)), also fixed Example..
     * ? Commit :
     */
    @Override
    public boolean check(Event e) {
        JsonElement object = check.getSingle(e);
        if ( object != null ) {
            if ( object.isJsonArray() ) {
                return ( pattern == 0) == object.getAsJsonArray().isEmpty();
            } else if ( object.isJsonObject() ) {
                return ( pattern == 0) == object.getAsJsonObject().entrySet().isEmpty();
            } else if ( object.isJsonPrimitive() ) {
                return ( pattern == 0) == object.getAsJsonPrimitive().isJsonNull();
            }
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "json " + check.toString(e,debug) + (isNegated() ? " is empty" : "isn't empty");
    }

}
