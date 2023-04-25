package cz.coffee.skript.conditions;

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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Json is empty")
@Description("You can check if the json empty")
@Examples("""
        Command jsonIsEmpty:
            trigger:
                set {_json} to json object
                if {_json} is empty:
                    send true
                else:
                    send false
        """
)
@Since("2.8.0 - performance & clean")

public class CondJsonEmpty extends Condition {

    static {
        Skript.registerCondition(CondJsonEmpty.class,
                "json %json% is empty",
                "json %json% is(n't| not) empty"
        );
    }

    private int line;
    private Expression<JsonElement> jsonElementExpression;

    @Override
    public boolean check(@NotNull Event e) {
        final JsonElement JSON = jsonElementExpression.getSingle(e);
        boolean result = false;
        if (JSON == null) return true;
        if (JSON instanceof JsonNull) result = true;
        if (JSON instanceof JsonObject object) result = object.isEmpty();
        if (JSON instanceof JsonArray array) result = array.isEmpty();
        return (line == 0) == result;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json" + jsonElementExpression.toString(e, debug)+ " " + (line == 0 ? "is" : "does not") + " empty";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        line = matchedPattern;
        jsonElementExpression = (Expression<JsonElement>) exprs[0];
        setNegated(line == 1);
        return true;
    }
}
