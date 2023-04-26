package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.SkJson.JSON_STORAGE;

@SuppressWarnings("ALL")
@Name("Json file is cached")
@Description("Check if the file for given id is cached")
@Examples({
        "on load:",
        "\tif cached json \"test\" if linked:",
        "\t\tsend true"
})
@Since("2.8.0 - performance & clean")

public class CondJsonIsLoaded extends Condition {

    static {
        Skript.registerCondition(CondJsonIsLoaded.class,
                "[cached] json %string% is (load|linked)",
                "[cached] json %string% is(n't| not) (load|linked)"
        );
    }

    private Expression<String> exprId;
    private int line;

    @Override
    public boolean check(@NotNull Event event) {
        final String id = exprId.getSingle(event);
        return (line == 0) == JSON_STORAGE.containsKey(id);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "cached json " + exprId.toString(event, b) + (line == 0 ? " is " : " is not") + "loaded";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        line = i;
        setNegated(i == 1);
        exprId = (Expression<String>) expressions[0];
        return true;
    }
}
