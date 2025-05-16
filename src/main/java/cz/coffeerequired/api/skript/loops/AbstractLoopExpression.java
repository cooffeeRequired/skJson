package cz.coffeerequired.api.skript.loops;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.regex.MatchResult;

public abstract class AbstractLoopExpression<T> extends SimpleExpression<T> {
    boolean itsIntendedLoop = false;
    protected AbstractLoop loop;
    protected String name;
    protected Class<? extends SimpleExpression<?>> clsTo;

    @SuppressWarnings("unchecked")
    protected static <T> T[] callGetMethod(Expression<T> expression, Event event) {
        try {
            Method getMethod = expression.getClass().getSuperclass().getDeclaredMethod("get", Event.class);
            getMethod.setAccessible(true);
            return (T[]) getMethod.invoke(expression, event);
        } catch (Exception ex) {
            throw Skript.exception(ex, "Failed to get value from expression %s", expression.toString(event, false));
        }
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        if (e == null)
            return name;
        if (itsIntendedLoop) {
            final Object current = loop.getCurrent(e);
            Object[] objects = callGetMethod(loop.getLoopedExpression(), e);

            if (current == null || objects == null)
                return Classes.getDebugMessage(null);
            return Classes.getDebugMessage(current);
        }
        return Classes.getDebugMessage(loop.getCurrent(e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parser) {
        SkJson.debug("init");

        name = parser.expr;
        MatchResult numberOfLoop = !parser.regexes.isEmpty() ? parser.regexes.getFirst() : null;
        var number = SkriptUtils.getLoopIndex(numberOfLoop);
        String expressionName = name.split("-")[1];
        int i = -1;
        if (number != null) i = number;

        SkJson.debug("loop expression class %s", this.getExpressionToLoop().getSimpleName());


        if (! Expression.class.isAssignableFrom(this.getExpressionToLoop()) || ! SimpleExpression.class.isAssignableFrom(this.getExpressionToLoop())) {
            SkJson.debug("!!! -> Expression '%s' is not an Expression subclass.".formatted(this.getExpressionToLoop().getName()));
            return false;
        }

        AbstractLoop loop = AbstractLoop.getSecLoop(i, expressionName, (Class<? extends Expression<?>>) this.getExpressionToLoop());

        if (loop == null) {
            Skript.error("here are multiple loops that match loop-%s. Use loop-%s-1/2/3/etc. to specify which loop's value you want.".formatted(expressionName, expressionName));
            return false;
        }

        if (loop.getObject() == null) {
            Skript.error("!!! -> There's no loop that matches 'loop-%s'.".formatted(expressionName));
            return false;
        }

        var initializer = new AbstractExpressionInitializer(vars, matchedPattern, isDelayed, parser);
        if (! this.isIntendedLoop(loop, initializer)) return false;

        this.loop = loop;
        return true;
    }

    public abstract boolean isIntendedLoop(AbstractLoop loop, final AbstractExpressionInitializer initializer);

    @SuppressWarnings("rawtypes")
    public abstract Class<? extends SimpleExpression> getExpressionToLoop();
}
