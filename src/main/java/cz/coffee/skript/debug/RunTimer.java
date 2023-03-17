package cz.coffee.skript.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.Util.codeRunTime;
import static cz.coffee.core.Util.color;

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
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: pondělí (13.03.2023)
 */
public class RunTimer extends Effect {

    static {
        Skript.registerEffect(RunTimer.class,
                "skJson debug timer (:start|:stop)",
                "skJson debug class %objects%"
        );
    }

    private boolean isStart;
    private int line;
    private Expression<Object> objectExpression;


    @Override
    protected void execute(@NotNull Event event) {
        if (line == 0) {
            if (isStart) {
                codeRunTime(true);
            } else {
                System.out.println(color("&c&lDEBUG&f timer: execution takes &e"+codeRunTime(false) + " &fseconds"));
            }
        } else {
            Object[] objects = objectExpression.getArray(event);
            for (Object object : objects) {
                System.out.println("CLASS---: " + object.getClass() );
            }
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "RunTimer";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        line = i;
        if (line == 1) {
            objectExpression = LiteralUtils.defendExpression(expressions[0]);
            return LiteralUtils.canInitSafely(objectExpression);
        } else {
            isStart = parseResult.hasTag("start");
            return true;
        }
    }
}
