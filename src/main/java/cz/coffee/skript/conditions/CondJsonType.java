package cz.coffee.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;



@SuppressWarnings("ALL")
@Name("Type of json")
@Description("You check json type of Json")
@Examples({
        "on load:",
        "\tif type of {_json} is primitive:",
        "\tsend true"
})
@Since("2.8.0 - performance & clean")

public class CondJsonType extends Condition {

    static {
        Skript.registerCondition(CondJsonType.class,
                "type of %json% (is|=) (1:primitive|2:[json]object|3:array)",
                "type of %json% (is(n't| not)|!=) (1:primitive|2:[json]object|3:array)"
        );
    }

    private int line, mark;
    private Expression<JsonElement> jsonElementExpression;

    @Override
    public boolean check(@NotNull Event event) {
        final JsonElement json = jsonElementExpression.getSingle(event);
        if (json == null) return false;
        if (!json.isJsonNull()) {
            if (line == 0) {
                if (mark == 1) return json.isJsonPrimitive();
                if (mark == 2) return json.isJsonObject();
                if (mark == 3) return json.isJsonArray();
            }
        }
        return false;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "type of " + jsonElementExpression.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        line = i;
        mark = parseResult.mark;
        jsonElementExpression = (Expression<JsonElement>) expressions[0];
        setNegated(i == 1);
        return true;
    }
}
