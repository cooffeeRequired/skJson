package cz.coffee.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import cz.coffee.core.cache.Cache;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Cached json exist")
@Description("You can check if the cached json exist")
@Examples({"on load:",
        "\tif cached json doesn't exists:",
        "\t\tdefine new cached json \"test1\" for given path \"test.json\""
})
@Since("2.7.0 - define new cached json")


public class CondCachedJsonExist extends Condition {

    static {
        Skript.registerCondition(CondCachedJsonExist.class,
                "cached json %string% exists",
                "cached json %string% does(n't| not) exists"
        );
    }

    private Expression<String> exprName;
    private boolean negated;

    @Override
    public boolean check(@NotNull Event e) {
        String name = exprName.getSingle(e);
        return (!negated) == Cache.contains(name);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "cached json " + exprName.toString(e, debug) + (negated ? " does not exists" : " exists");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        negated = matchedPattern == 1;
        exprName = (Expression<String>) exprs[0];
        setNegated(negated);
        return true;
    }
}
