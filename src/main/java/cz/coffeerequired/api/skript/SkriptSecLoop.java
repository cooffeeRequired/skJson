package cz.coffeerequired.api.skript;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.sections.SecLoop;
import org.bukkit.event.Event;

public class SkriptSecLoop extends AbstractLoop {

    public SkriptSecLoop(Object loop) {
        super(loop);
    }

    @Override
    public Class<?> getLoopClass() {
        SecLoop o = getObject();
        if (o != null) {
            return o.getClass();
        }
        return null;
    }

    @Override
    public SecLoop getObject() {
        return (SecLoop) loop;
    }

    @Override
    public Object getCurrent(Event event) {
        SecLoop o = getObject();
        if (o != null) {
            return o.getCurrent(event);
        }
        return null;
    }

    @Override
    public Expression<?> getLoopedExpression() {
        SecLoop o = getObject();
        if (o != null) {
            return o.getLoopedExpression();
        }
        return null;
    }
}
