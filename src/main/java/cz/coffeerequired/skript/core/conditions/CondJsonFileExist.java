package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.FileHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Name("Check if JSON file exists")
@Description("Checks whether a `.json` or `.jsonc` file exists at the given path.")
@Since("2.6.2")
@Examples("""
        if json file "skjson/homes.json" exists:
            send true
        if file "plugins/SkJson/skjson/homes.json" exists as json:
            send true
        """)
public class CondJsonFileExist extends Condition {

    private Expression<String> filePathExpression;
    private int line;

    @Override
    public boolean check(Event event) {
        final String filePath = filePathExpression.getSingle(event);
        if (filePath == null) return false;
        if (!filePath.endsWith(".json") && !filePath.endsWith(".jsonc")) return false;
        boolean expectExists = line % 2 == 0;
        return expectExists == FileHandler.exists(filePath);
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
        setNegated(line % 2 == 1);
        return true;
    }
}
