package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static cz.coffee.skriptgson.util.PluginUtils.gsonText;

@Name("MapJson")
@Description("Map Json")
@Since("1.0")


// TODO FIX THAT ****

@SuppressWarnings({"unused","NullableProblems","unchecked"})
public class ExprMapJsonValues extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprMapJsonValues.class, JsonElement.class, ExpressionType.PROPERTY,
                "json values %jsonelements%");
    }

    @Nullable
    private Expression<JsonElement> exprJson;


    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        System.out.println("TEST");
        exprJson = (Expression<JsonElement>) exprs[0];
        if ( exprJson instanceof Variable && !exprJson.isSingle()) {
            SkriptGson.warning("ALL");
        }
        return true;
    }

    @Override
    protected JsonElement[] get(Event e) {
        final ArrayList<JsonElement> je = new ArrayList<>();
        assert exprJson != null;
        for ( final JsonElement nje : exprJson.getArray(e)) {
            if ( nje.isJsonObject()) {
                for (int i = 0; i < nje.getAsJsonObject().size(); i++) {
                    System.out.println(gsonText(nje));
                }
            } else if ( nje.isJsonArray() ){
                for (int i = 0; i < nje.getAsJsonObject().size(); i++) {
                    System.out.println(gsonText(nje));
                }
            } else {

            }
        }
        return new JsonElement[]{JsonParser.parseString(gsonText(je))};
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return null;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return null;
    }
}
