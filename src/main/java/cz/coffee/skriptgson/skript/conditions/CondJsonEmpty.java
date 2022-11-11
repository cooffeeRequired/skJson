package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Since("1.3.0")
@Name("is Json empty")
@Description("Checks if Json is empty.")
@Examples({"on script load:",
        "\tset {-e} to json from string \"{'Hello': 'There'\"}",
        "\tjson {-e} is empty: ",
        "\t\tbroadcast \"is empty\"",
})

public class CondJsonEmpty extends Condition {

    static {
        Skript.registerCondition(CondJsonEmpty.class,
                "json[element] %jsonelement% is empty",
                "json[element] %jsonelement% is(n't| not) empty"
        );
    }

    private Expression<JsonElement> check;
    private int pattern;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        check = (Expression<JsonElement>) exprs[0];
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        JsonElement object = check.getSingle(e);
        if(object == null)
            return false;

        if(object.isJsonObject()) {
            return (pattern == 0) == object.getAsJsonObject().entrySet().isEmpty();
        } else if(object.isJsonArray()) {
            return (pattern == 0) == object.getAsJsonArray().isEmpty();
        } else if(object.isJsonPrimitive()) {
            return (pattern == 0) == object.getAsJsonPrimitive().isJsonNull();
        }
        return false;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json " + check.toString(e,debug) + (isNegated() ? " is empty" : "isn't empty");
    }
}
