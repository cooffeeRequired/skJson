package cz.coffeerequired.api.skript.loops;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.sections.SecLoop;
import org.bukkit.event.Event;

import static cz.coffeerequired.support.SkriptUtils.currentLoops;

public abstract class AbstractLoop {

    protected Object loop;

    public AbstractLoop(Object loop) {
        this.loop = loop;
    }

    public abstract Class<?> getLoopClass();
    public abstract Object getObject();
    public abstract Object getCurrent(Event event);
    public abstract Expression<?> getLoopedExpression();

    public static SkriptSecLoop getSecLoop(int i, String expressionName, Class<? extends Expression<?>> cls) {
        if (cls == null) return null;
        int j = 1;
        SecLoop loop = null;
        for (SecLoop l : currentLoops()) {
            if (l.getLoopedExpression().getClass().isAssignableFrom(cls)) {
                if (j < i) {
                    j++;
                    continue;
                }
                if (loop != null) {
                    return null;
                }
                loop = l;
                if (j == i)
                    break;
            }
        }
        return new SkriptSecLoop(loop);
    }

}