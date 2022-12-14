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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("All cached Jsons")
@Description({"Print out all your saved json in the cache"})
@Examples({"on load:",
        "\tsend all cached json",
        "",
        "\tsend all cached json as json formatted"
})
@Since("2.0.0")

public class ExprGetAllCachedJson extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprGetAllCachedJson.class, JsonElement.class, ExpressionType.COMBINED,
                "all cached json [(:as json formatted)]",
                "[only] %integer% of cached json [(:as json formatted)]"
        );
    }

    private Expression<Integer> integerExpression;
    private boolean formatted;
    private int pattern;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        Integer integerExpression = null;
        JsonElement json = hierarchyAdapter().toJsonTree(JSON_HASHMAP).getAsJsonObject();
        if (pattern == 1)
            integerExpression = this.integerExpression.getSingle(e);

        if (pattern == 0) {
            if (!formatted) {
                JsonArray array = new JsonArray();
                json.getAsJsonObject().keySet().forEach(array::add);
                return new JsonElement[]{array};
            } else {
                JsonObject object = new JsonObject();
                json.getAsJsonObject().entrySet().forEach(entry -> object.add(entry.getKey(), entry.getValue()));
                return new JsonElement[]{object};
            }
        } else {
            if (!formatted) {
                JsonArray array = new JsonArray();
                if (integerExpression == null) return new JsonElement[0];
                int index = 0;
                for (String key : json.getAsJsonObject().keySet()) {
                    if (index < integerExpression) {
                        index++;
                        array.add(key);
                    }
                }
                return new JsonElement[]{array};
            } else {
                JsonObject object = new JsonObject();
                if (integerExpression == null) return new JsonElement[0];
                int index = 0;
                for (Map.Entry<String, JsonElement> map : json.getAsJsonObject().entrySet()) {
                    if (index < integerExpression) {
                        index++;
                        object.add(map.getKey(), map.getValue());
                    }
                }
                return new JsonElement[]{object};
            }

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
        return "";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        formatted = parseResult.hasTag("as json formatted");
        if (pattern == 1) {
            integerExpression = (Expression<Integer>) exprs[0];
        }
        return true;
    }
}
