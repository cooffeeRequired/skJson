package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.util.JsonMap;
import org.bukkit.event.Event;


@Since("1.2.0")
@Name("Count of Value/Key")
@Description({"Displays the result of how many keys/values are found with JsonElement"})
@Examples({"on script load:",
        "\tset {-data} to json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tsend {-data}'s count key \"Hi\"",
        "\tsend count value \"There\" of {-data}"
})

@SuppressWarnings({"unused","NullableProblems","unchecked"})
public class ExprCountOfJson extends SimplePropertyExpression<JsonElement, Integer> {

    static {
        register(ExprCountOfJson.class, Integer.class,
                "(count|number) (:key|:value) %string%", "jsonelements");
    }

    private Expression<String> str;
    private int tag;

    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr(matchedPattern == 1 ? (Expression<? extends JsonElement>) exprs[0] : (Expression<? extends JsonElement>) exprs[1]);
        str = (matchedPattern == 1 ? (Expression<String>) exprs[1] : (Expression<String>) exprs[0]);
        tag = parseResult.hasTag("key") ? 1 : 2;
        return true;
    }

    @Override
    protected String getPropertyName() {
        return "(count|number) (:key|:value) %string%";
    }

    @Override
    public Integer convert(JsonElement jsonElement) {
        JsonMap map = new JsonMap();
        Event e = null;
        assert false;
        return map.countOccurrenceOfKey(getExpr().getSingle(e), str.getSingle(e), tag).getCount();
    }
    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }
}
