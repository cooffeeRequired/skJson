package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.FileHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Name("Check if JSON file exists")
@Examples("""
        if json file "..." exists:
            send true
        """)
public class CondJsonFileExist extends Condition {

    private Expression<String> filePathExpression;
    private int line;

    @Override
    public boolean check(Event event) {
        final String filePath = filePathExpression.getSingle(event);
        if (filePath == null || !filePath.endsWith(".json")) return false;
        return (line == 0) ==  FileHandler.exists(filePath);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "json file " + filePathExpression.toString(event, debug) + (line == 1 ? "exists" : "does not exist");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        filePathExpression = (Expression<String>) expressions[0];
        line = matchedPattern;
        setNegated(line == 1);
        return true;
    }
}
