/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;


@Name("Representing a raw json.")
@Description({"json from parsed json to a object"})
@Examples({"command saveLocToJson:",
        "\ttrigger:",
        "\t\tset {-e} to json from player's location",
        "\t\tsend raw {-e} with pretty print to console"
})
@Since("2.6.2")
public class ExprRawJson extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprRawJson.class, JsonElement.class, ExpressionType.SIMPLE, "raw json %json%");
    }

    private Expression<JsonElement> json;

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return String.format("raw json %s ", json.toString(e, debug));
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        json = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(json);
    }

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        JsonElement jsonElement = json.getSingle(e);
        return new JsonElement[]{jsonElement};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }
}
