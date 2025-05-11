package cz.coffeerequired.api.skript;

import ch.njol.skript.lang.Expression;
import cz.coffeerequired.SkJson;
import org.bukkit.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SkriptLoop extends AbstractLoop{

    protected Object loop;
    static Class<?> loopClass;
    static Method getCurrent, getLoopedExpression;

    static {
        try {
            loopClass = Class.forName("ch.njol.skript.lang.Loop");
            getCurrent = loopClass.getDeclaredMethod("getCurrent", Event.class);
            getLoopedExpression = loopClass.getDeclaredMethod("getLoopedExpression");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            SkJson.exception(e, "Couldn't find class '%s'", loopClass.getName());
        }
    }

    public SkriptLoop(Object loop) {
        super(loop);
    }

    public Class<?> getLoopClass() {
        return getObject() != null ? getObject().getClass() : null;
    }


    public Object getObject() {
        return loop;
    }

    public Object getCurrent(Event event) {
        Object o = getObject();
        if (o != null) {
            try {
                return getCurrent.invoke(o, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                SkJson.exception(e, "Couldn't invoke getCurrent(%s)", o.getClass().getName());
            }
        }
        return null;
    }

    public Expression<?> getLoopedExpression() {
        Object o = getObject();
        return getLoopedExpression(o);
    }

    static public Expression<?> getLoopedExpression(Object o) {
        if (o != null) {
            try {
                return (Expression<?>) getLoopedExpression.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                SkJson.exception(e, "Couldn't invoke getLoopedExpression()");
            }
        }
        return null;
    }
}
