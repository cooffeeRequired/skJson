package cz.coffee.skript.debug;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
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


@Name("Simple skJson debugging")
@Description("You can get classes or you can check execution time of the running code")
@Examples({
        "SkJson debug on",
        "timer start",
        "set {_json} to json from text \"{'Hello': 'From debugger', 'Class': '%class 10%'}\"",
        "send {_json} with pretty print",
        "timer stop and debug off",
})
@NoDoc
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
                if (line == 1) {
                    Object[] objects = objectExpression.getArray(event);
                    for (Object object : objects)
                        SkJson.console("class of " + object + " is " + object.getClass().getName());
                } else {
                    if (isStart) codeRunTime(true);
                    else {
                        if (isDebuggingStopped) isDebuggingEnabled = false;
                        SkJson.console("&c&lDEBUG&f timer: execution takes &e" + codeRunTime(false) + "&f seconds");
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
