/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
package cz.coffee.skript.effects;

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

import static cz.coffee.adapter.DefaultAdapters.assignFrom;
import static cz.coffee.utils.SimpleUtil.printPrettyStackTrace;
import static cz.coffee.utils.config.Config._STACKTRACE_LENGTH;

@Name("Basic Json Converter, from Json to any object for i.e. (Location, ItemStack, Inventory)")
@Description({
        "You can deserialize correct json to skript-type, for example a tool a location, etc.",
        "<p><code> Also you don't need use the converter, cause the json is converted automatically, use that only in some case.</code></p>"
})
@Examples({"command save_location",
        "\ttrigger:",
        "\t\tset {-json} to json from sender's location",
        "\t\tsend \"Saved location as JSON &e%{-json}%\"",
        "",
        "command jsonTeleport:",
        "\ttrigger",
        "\t\t set {_location} to parsed {-json}",
        "\t\tsend \"You will be tp to &b%{_location}%&r from Json\"",
        "\t\tteleport sender to {_location}"
})
@Since("2.6.21 - CleanUp code")
public class EffSimpleParse extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(EffSimpleParse.class, Object.class, ExpressionType.SIMPLE,
                "parsed %object%"
        );
    }

    private Expression<Object> json;

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "parsed " + json.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        json = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(json);
    }

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        Object object = json.getSingle(e);

        try {
            if (object instanceof JsonElement) {
                return new Object[]{assignFrom((JsonElement) object)};
            }
        } catch (Exception ex) {
            printPrettyStackTrace(ex, _STACKTRACE_LENGTH);
        }
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }
}