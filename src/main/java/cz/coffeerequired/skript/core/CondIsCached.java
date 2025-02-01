package cz.coffeerequired.skript.core;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.Api;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Name("Is JSON file cached")
@Examples("""
            json storage of id "my-json-storage" is cached:
                send true
        """)
@Since("4.1 - API UPDATE")
public class CondIsCached extends Condition {

    private Expression<String> expressionId;
    private int line;

    @Override
    public boolean check(Event event) {
        String id = expressionId.getSingle(event);
        return (line == 0) == Api.getCache().containsKey(id);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "json storage of id " + expressionId.toString(event, debug) + " " + (line == 0 ? "is" : "isn't") + " cached";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        line = matchedPattern;
        setNegated(matchedPattern == 1);
        expressionId = (Expression<String>) expressions[0];
        return true;
    }
}
