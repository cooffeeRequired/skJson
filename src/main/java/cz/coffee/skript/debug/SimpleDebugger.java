package cz.coffee.skript.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import cz.coffee.SkJson;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.utils.Util.codeRunTime;

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

@Name("Simple skJson debugging")
@Description("You can get classes or you can check execution time of the running code")
@Examples({
        "SkJson debug on",
        "timer start",
        "set {_json} to json from text \"{'Hello': 'From debugger', 'Class': '%class 10%'}\"",
        "send {_json} with pretty print",
        "timer stop and debug off",
})
@Since("")

public class SimpleDebugger extends Effect {

    static boolean isDebuggingEnabled = false;

    static {
        Skript.registerEffect(SimpleDebugger.class,
                "[skJson] [debug] timer (:start|(:stop) [and (:debug off)])",
                "[skJson] class %objects%",
                "[skJson] debug (:on|:off)"
        );
    }

    private boolean isStart, enabledSignal, isDebuggingStopped;
    private int line;
    private Expression<Object> objectExpression;


    @Override
    protected void execute(@NotNull Event event) {
        if (line == 0 || line == 1) {
            if (isDebuggingEnabled) {
                // TODO detect console or player
                if (line == 1) {
                    Object[] objects = objectExpression.getArray(event);
                    for (Object object : objects) SkJson.console("class of " + object + " is " + object.getClass().getName());
                } else  {
                    if (isStart) codeRunTime(true);
                    else {
                        if (isDebuggingStopped) isDebuggingEnabled = false;
                        SkJson.console("&c&lDEBUG&f timer: execution takes &e"+codeRunTime(false) + "&f seconds");
                    }
                }
            } else {
                SkJson.error("Your debugging mode is off.");
            }
        } else if (line == 2) {
            isDebuggingEnabled = enabledSignal;
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "SkJson simple debugger";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        line = i;
        if (line == 1) {
            objectExpression = LiteralUtils.defendExpression(expressions[0]);
            return LiteralUtils.canInitSafely(objectExpression);
        } else if (line == 0) {
            isStart = parseResult.hasTag("start");
            isDebuggingStopped = parseResult.hasTag("debug off");
            return true;
        } else {
            enabledSignal = parseResult.hasTag("on");
            return true;
        }
    }
}
