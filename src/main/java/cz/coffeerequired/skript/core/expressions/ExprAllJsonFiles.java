package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import cz.coffeerequired.api.FileHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Name("Get all files paths in given directory")
@Examples("all json files from dir \".\"")
@Description("Returns all json files from the given directory. The directory must be a valid path.")
@Since("5.0")
public class ExprAllJsonFiles extends SimpleExpression<String> {

    private Expression<String> directoryPathExpression;

    @Override
    protected @Nullable String[] get(Event event) {
        var directoryPath = directoryPathExpression.getSingle(event);
        return FileHandler.walk(directoryPath).join();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "all json files from folder " + directoryPathExpression.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        directoryPathExpression = (Expression<String>) expressions[0];
        return directoryPathExpression != null;
    }

    @Override
    public boolean isLoopOf(String input) {
        return input.equalsIgnoreCase("file");
    }
}
