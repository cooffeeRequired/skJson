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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Since("1.3.0")
@Name("JSON empty")
@Description("You can simply check if the file is empty.")
@Examples({"on script load:",
        "\tset {_j} to new json from string \"{'Hello': 'There'}\"",
        "\t{_j} is empty: ",
        "\t\tbroadcast \"is empty\"",
})

public class CondJsonEmpty extends Condition {

    static {
        Skript.registerCondition(CondJsonEmpty.class,
                "json %jsonelement% is empty",
                "json %jsonelement% is(n't| not) empty"
        );
    }

    private Expression<JsonElement> exprJson;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        exprJson = (Expression<JsonElement>) exprs[0];
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        JsonElement json = exprJson.getSingle(e);
        if (json == null) return false;

        if (json instanceof JsonObject object)
            return (pattern == 0) == object.entrySet().isEmpty();
        else if (json instanceof JsonArray array)
            return (pattern == 0) == array.isEmpty();
        else if (json instanceof JsonPrimitive primitive)
            return (pattern == 0) == primitive.isJsonNull();
        return false;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json " + exprJson.toString(e, debug) + (isNegated() ? " is empty" : "isn't empty");
    }
}
