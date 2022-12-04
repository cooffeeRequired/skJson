package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.util.Utils.newGson;


@Name("All or any count loaded JSON")
@Description({"You can view all saved Jsons, or just part of them"})
@Examples({"on script load", "  send all loaded json"})
@Since("1.4.0")

public class ExprAllLoadedJsons extends SimpleExpression<JsonElement> {

    private Expression<Integer> id;
    private boolean isAll;


    static {
        Skript.registerExpression(ExprAllLoadedJsons.class, JsonElement.class, ExpressionType.COMBINED,
                "(all|(:only) %-integer%) loaded json"
        );
    }


    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        JsonObject json = newGson().toJsonTree(JSON_HASHMAP).getAsJsonObject();
        if(!isAll) {
            return new JsonElement[]{json};
        } else {
            int i = id.getSingle(e);
            int index = 0;
            JsonObject object = new JsonObject();
            for(String key : json.keySet()) {
                if(index < i) {
                    index++;
                    object.add(key,json.get(key));
                }
            }
            return new JsonElement[]{object};
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (isAll ? "all " : "only " + id.toString(e,debug) + " ") + "loaded json";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        isAll = parseResult.hasTag("only");
        id = (Expression<Integer>) exprs[0];
        return true;
    }
}
