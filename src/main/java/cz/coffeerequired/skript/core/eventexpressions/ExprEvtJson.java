package cz.coffeerequired.skript.core.eventexpressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;

@Name("Watcher event value-expression json content")
@Description({"value-expression for getting json content from current watcher event", "Returns json element (content) of the watched|changed file"})
@Since("5.0")
public class ExprEvtJson extends EventValueExpression<JsonElement> {

    public ExprEvtJson() {
        super(JsonElement.class);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {

        if (!getParser().isCurrentEvent(JsonFileChanged.class)) {
            return false;
        }
        return super.init(expressions, matchedPattern, isDelayed, parser);
    }
}
