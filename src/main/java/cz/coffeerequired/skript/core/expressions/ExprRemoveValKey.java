package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.types.JsonPath;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class ExprRemoveValKey extends SimpleExpression<JsonElement> {

    Expression<JsonPath> valPathExpression;
    private boolean isValue;

    @Override
    protected JsonElement @Nullable [] get(Event event) {
        Object[] vals = valPathExpression.getAll(event);

        if (isValue) {
            var object = new JsonObject();
            object.addProperty("...changer-properties...", ".");
            object.addProperty("type", "value");
            var values = new JsonArray();
            for (Object val : vals) {
                values.add(GsonParser.toJson(val));
            }
            object.add("values", values);
            return new JsonElement[]{object};
        } else if (SkriptUtils.anyElementIs(vals, (v) -> v instanceof String)) {
            var object = new JsonObject();
            object.addProperty("type", "key");
            object.addProperty("...changer-properties...", ".");
            var values = new JsonArray();
            for (Object val : vals) {
                values.add((String) val);
            }
            object.add("values", values);
            return new JsonElement[]{object};

        }
        return new JsonElement[0];
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "(value\\key) expression " + valPathExpression.toString(event, debug) + " of json element";
    }


    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        valPathExpression = LiteralUtils.defendExpression(expressions[0]);
        isValue = parseResult.hasTag("value");
        return LiteralUtils.canInitSafely(valPathExpression);
    }
}
