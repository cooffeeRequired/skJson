package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;


@SuppressWarnings({"unused","NullableProblems","unchecked"})
public class ExprKeyValueFromJson extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprKeyValueFromJson.class, String.class, ExpressionType.COMBINED,
                "key %string% from %jsonelement%"
        );
    }

    private Expression<String> key;
    private Expression<JsonElement> json;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        json = (Expression<JsonElement>) exprs[1];
        key = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event e) {
        String keySingle = key.getSingle(e);
        JsonElement jsonElement = json.getSingle(e);
        if (jsonElement == null || !jsonElement.isJsonObject())
            return null;
        return new String[]{new Gson().toJson(jsonElement.getAsJsonObject().get(keySingle))};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString( Event e, boolean debug) {
        return "key " + key.getSingle(e) + " from json";
    }

}
