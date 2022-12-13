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
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("Count of values or keys in the JSON")
@Description({"Return the result of count of keys/values are found in the Json."})
@Examples({"on load:",
        "\tset {_json} to new json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tsend count of value \"There\" of {_json}",
        "\tsend count of key \"There\" of json-id \"your\""
})
@Since("2.0.0")

public class ExprJsonCount extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(ExprJsonCount.class, Integer.class, ExpressionType.COMBINED,
                "(count|number) of (:value|:key) %string% of %jsonelement%",
                "(count|number) of (:value|:key) %string% of [cached] json-id %string%"
        );
    }

    private Expression<String> stringExpression;
    private Expression<JsonElement> jsonElementExpression;
    private Expression<String> stringIDExpression;
    private int typeOf;
    private int pattern;

    @Override
    protected @Nullable Integer @NotNull [] get(@NotNull Event e) {
        GsonErrorLogger err = new GsonErrorLogger();
        String searchedPhrase = stringExpression.getSingle(e);
        String stringIDExpression;

        JsonElement json = null;
        if (pattern == 0) {
            json = jsonElementExpression.getSingle(e);
        } else if (pattern == 1) {
            stringIDExpression = this.stringIDExpression.getSingle(e);
            if (!JSON_HASHMAP.containsKey(stringIDExpression)) {
                SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
                return new Integer[0];
            }
            json = hierarchyAdapter().toJsonTree(JSON_HASHMAP.get(stringIDExpression)).getAsJsonObject();
        }

        if (json == null) return new Integer[0];
        int founded = GsonUtils.count(searchedPhrase, json, typeOf == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
        return new Integer[]{founded};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "count of " + (typeOf == 1 ? "key " : "value ") + stringExpression.toString(e, debug) + " of " + jsonElementExpression.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        typeOf = (parseResult.hasTag("value") ? 2 : 0);
        typeOf = (parseResult.hasTag("key") ? 1 : 0);
        stringExpression = (Expression<String>) exprs[0];
        if (pattern == 0) {
            jsonElementExpression = (Expression<JsonElement>) exprs[1];
        } else if (pattern == 1) {
            stringIDExpression = (Expression<String>) exprs[1];
        }
        return true;
    }
}
