package cz.coffee.skriptgson.skript.effect;


import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;


//TODO fix!
@SuppressWarnings({"unchecked","unused","NullableProblems"})
public class EffKeyValueToJson extends Effect {

    static {
        Skript.registerEffect(EffKeyValueToJson.class,
                "set json [value] (%string%|%jsonelement%) to %jsonelement%");
    }

    private Expression<String> key;
    private Expression<JsonElement> json;
    private Expression<JsonElement> value;
    private int pattern;

    @Override
    protected void execute(Event e) {
        String keySingle = key.getSingle(e);
        JsonElement jsonElement = json.getSingle(e);
        JsonElement valueSinge = value.getSingle(e);
        if (!(jsonElement == null || !jsonElement.isJsonObject())) {
            assert keySingle != null;
            jsonElement.getAsJsonObject().add(keySingle,valueSinge);

            System.out.println(new Gson().toJson(jsonElement));
        }
    }

    @Override
    public String toString(Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        json = (Expression<JsonElement>) exprs[2];
        key = (Expression<String>) exprs[0];
        value = (Expression<JsonElement>) exprs[1];
        return true;
    }
}
