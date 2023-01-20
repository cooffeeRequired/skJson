/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */

package cz.coffee.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.utils.Type;
import cz.coffee.utils.json.JsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;


@Name("Count of values or keys in the JSON")
@Description({"Return the result of count of keys/values are found in the Json."})
@Examples({"on load:",
        "\tset {_json} to new json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tsend count of value \"There\" of {_json}",
        "\tsend count of key \"There\" of json \"your\""
})
@Since("2.5.0")

public class SimpleExprJsonCount extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(SimpleExprJsonCount.class, Integer.class, ExpressionType.SIMPLE,
                "count of (:value|:key) %object% of %json%"
        );
    }

    private Expression<JsonElement> exprJson;
    private Expression<Object> exprSearch;
    private boolean isKey;
    @Override
    protected @Nullable Integer @NotNull [] get(@NotNull Event e) {
        JsonUtils ju = new JsonUtils();
        JsonElement json = exprJson.getSingle(e);
        Object search = exprSearch.getSingle(e);
        assert json != null;
        assert search != null;
        return new Integer[]{ju.count(search.toString(), json, isKey ? Type.KEY : Type.VALUE)};
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
        return "count of "+(isKey ? " key " : " value ") + exprSearch.toString(e, debug) + " of " + exprJson.toString(e, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        isKey = parseResult.hasTag("key");
        exprJson = (Expression<JsonElement>) exprs[1];
        exprSearch = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(exprSearch);
    }
}
