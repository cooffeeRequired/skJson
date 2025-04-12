package cz.coffeerequired.skript.http.eventexpressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;

@Name("Watcher event value-expression File")
@Description({"value-expression for getting file/link from current watcher event", "Returns path of changed|watched file"})
@Since("2.9")
public class ExprEvtBody extends EventValueExpression<Object> {

    public ExprEvtBody() {
        super(Object.class);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {

        if (! getParser().isCurrentEvent(HttpReceivedResponse.class)) {
            return false;
        }
        return super.init(expressions, matchedPattern, isDelayed, parser);
    }
}
