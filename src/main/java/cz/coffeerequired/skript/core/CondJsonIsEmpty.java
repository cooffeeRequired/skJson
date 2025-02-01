package cz.coffeerequired.skript.core;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Check if JSON is empty")
@Examples("""
        set {_json} to json from "{}"
        if json {_json} is empty:
            send true
        """)
public class CondJsonIsEmpty extends Condition {

    private Expression<JsonElement> jsonElementExpression;
    private boolean negated;

    @Override
    public boolean check(Event event) {
        final JsonElement JSON = jsonElementExpression.getSingle(event);
        boolean result = false;
        if (JSON == null) return true;
        if (JSON instanceof JsonNull) result = true;
        if (JSON instanceof JsonObject) result = JSON.getAsJsonObject().isEmpty();
        if (JSON instanceof JsonArray) result = JSON.getAsJsonArray().isEmpty();
        return (negated) == result;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "json" + jsonElementExpression.toString(event, debug) + " " + (negated ? "is" : "does not") + " empty";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        jsonElementExpression = (Expression<JsonElement>) expressions[0];
        negated = matchedPattern == 1;
        setNegated(negated);
        return true;
    }
}
