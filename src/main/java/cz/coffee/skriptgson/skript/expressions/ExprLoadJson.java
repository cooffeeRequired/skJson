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
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("Load cached Json File as string iD")
@Description({"You can load cached json as String id"})
@Examples({"on script load:",
        "\tset {_json} to [cached] json-id \"your\"",
        "\tsend {_json} with pretty print"
})
@Since("2.0.0")


public class ExprLoadJson extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprLoadJson.class, JsonElement.class, ExpressionType.COMBINED,
                "[cached] [json-]id %string%"
        );
    }

    private Expression<String> stringExpression;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        GsonErrorLogger err = new GsonErrorLogger();
        JsonElement element;
        String stringExpression = this.stringExpression.getSingle(e);
        if (stringExpression == null) return new JsonElement[0];
        if (JSON_HASHMAP.containsKey(stringExpression)) {
            element = hierarchyAdapter().toJsonTree((JSON_HASHMAP.get(stringExpression)));
            return new JsonElement[]{element};
        }
        SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
        return new JsonElement[0];
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
        return "cached json-id " + stringExpression.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        stringExpression = (Expression<String>) exprs[0];
        return true;
    }
}
