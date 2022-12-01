package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Name("JSON File exist")
@Description("You can simply check whether the JSON file exists or not.")
@Examples({"on load:",
        "   json file path \"test\\test.json\" exists:",
        "       broadcast true"
})
@Since("1.3.1")

public class CondFileExist extends Condition {

    static {
        Skript.registerCondition(CondFileExist.class,
                "json file [path] %object% exists",
                "json file [path] %object% does(n't| not) exists"
        );
    }

    private Expression<Object> exprFilePath;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        exprFilePath = LiteralUtils.defendExpression(exprs[0]);
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return LiteralUtils.canInitSafely(exprFilePath);
    }

    @Override
    public boolean check(@NotNull Event e) {
        Object filePath = exprFilePath.getSingle(e);
        if (filePath == null) return false;

        String stringifyFilePath = filePath.toString();

        return (pattern == 0) == new File(stringifyFilePath).exists();
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "json file " + exprFilePath.toString(e, debug) + (isNegated() ? " exist" : " doesn't exist");

    }
}
