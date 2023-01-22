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
package cz.coffee.skript.test;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.utils.ErrorHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.utils.ErrorHandler.sendMessage;

@Since("2.5.0")
public class SkJsonEffectTest extends SimpleExpression<Boolean> {

    static {
        Skript.registerExpression(SkJsonEffectTest.class, Boolean.class, ExpressionType.SIMPLE,
                "[skJson] version check");
    }

    @Override
    protected Boolean @NotNull [] get(@NotNull Event e) {
        JsonElement element = null;
        try {
            element = JsonParser.parseString("{'A':'n'}");
        } catch (Exception exception) {
            sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
        }
        if (element != null) {
            return new Boolean[]{true};
        }
        return new Boolean[]{false};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }
}
