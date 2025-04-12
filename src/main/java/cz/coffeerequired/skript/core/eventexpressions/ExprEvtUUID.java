package cz.coffeerequired.skript.core.eventexpressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;

import java.util.UUID;

@Name("Watcher event value-expression UUID")
@Description({"value-expression for getting uuid from current watcher event", "Returns the uuid of the watcher"})
@Since("2.9")
public class ExprEvtUUID extends EventValueExpression<UUID> {

    public ExprEvtUUID() {
        super(UUID.class);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {

        if (! getParser().isCurrentEvent(JsonFileChanged.class)) {
            return false;
        }
        return super.init(expressions, matchedPattern, isDelayed, parser);
    }
}