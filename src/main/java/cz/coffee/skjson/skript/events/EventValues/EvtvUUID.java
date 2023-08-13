package cz.coffee.skjson.skript.events.EventValues;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

import java.util.UUID;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: neděle (13.08.2023)
 */
public class EvtvUUID extends EventValueExpression<UUID> {

    static {
        Skript.registerExpression(EvtvUUID.class, UUID.class, ExpressionType.SIMPLE, "[the] [event-](uuid|id)");
    }
    public EvtvUUID() {
        super(UUID.class);
    }
}
