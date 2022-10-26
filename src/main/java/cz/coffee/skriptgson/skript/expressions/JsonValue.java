package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.event.Event;


@SuppressWarnings({"unused","NullableProblems","unchecked"})
public class JsonValue extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(JsonValue.class, Object.class, ExpressionType.COMBINED,
                "json-value %integer/string%(->)%boolean/integer/string/jsonelement%");
    }

    private Expression<Object> first;
    private Expression<Object> second;

    @Override
    protected JsonElement[] get(Event e) {
        JsonElement jsonElem;

        String finalStr = "[[" + first.getSingle(e) + "]," + second.getSingle(e) + "]";

        jsonElem = JsonParser.parseString(finalStr);


        return new JsonElement[]{jsonElem};
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
    public String toString(Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        first = (Expression<Object>) exprs[0];
        second = (Expression<Object>) exprs[1];


        return true;
    }
}
