package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Json deep copy")
@Description("Returns a deep copy of a JSON value so changes to the copy do not affect the original.")
@Since("5.5")
@Examples("""
        set {_original} to parse "{""count"": 1}" as json
        set {_copy} to deep copy of {_original}
        set value at path "count" in {_copy} to 2
        """)
public class ExprJsonCopy extends SimpleExpression<JsonElement> {

    private Expression<JsonElement> jsonExpression;

    @Override
    protected @Nullable JsonElement[] get(Event event) {
        JsonElement json = jsonExpression.getSingle(event);
        if (json == null) {
            return new JsonElement[0];
        }
        return new JsonElement[]{json.deepCopy()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "deep copy of " + jsonExpression.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        jsonExpression = defendExpression(expressions[0]);
        return canInitSafely(jsonExpression);
    }
}
