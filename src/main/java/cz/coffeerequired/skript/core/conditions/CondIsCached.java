package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Description;
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
@Description({
        "Checks whether a JSON cache id is loaded in memory.",
        "Aliases: `json cache … exists` and `json storage with id … is cached`."
})
@Examples("""
        if json cache "homesdb" exists:
            send true
        if the json storage with id "benchdb" is cached:
            send true
        """)
@Since("4.1 - API UPDATE")
public class CondIsCached extends Condition {

    private Expression<String> expressionId;
    private int line;

    @Override
    public boolean check(Event event) {
        String id = expressionId.getSingle(event);
        boolean expectCached = line % 2 == 0;
        return expectCached == Api.getCache().containsKey(id);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "[skjson] the json storage with id " + expressionId.toString(event, debug) + " " +
                (line == 0 ? "is" : "isn't") + " cached";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        line = matchedPattern;
        setNegated(matchedPattern % 2 == 1);
        expressionId = (Expression<String>) expressions[0];
        return true;
    }
}
