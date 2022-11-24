package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Name("is JSON File exist")
@Description("Checks if JSON File exist")
@Examples({"on load:",
        "\tjson file \"test\\test.json\" exists:",
        "\t\tbroadcast true"
})
@Since("1.0")

public class CondFileExist extends Condition {

    static {
        Skript.registerCondition(CondFileExist.class,
                "json file %object% exists",
                "json file %object% does( not|n't) exists"
        );
    }

    private Expression<Object> check;
    private int pattern;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        check = (Expression<Object>) exprs[0];
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }

    @Override
    public boolean check(@NotNull Event e) {
        Object raw_object = check.getSingle(e);
        Object file;
        if ( raw_object instanceof File) {
            file = (File) raw_object;
        } else if ( raw_object instanceof String ){
            file = new File(raw_object.toString());
        } else {
            return false;
        }
        return ( pattern ==  0) == ((File) file).exists();
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "json file " + check.toString(e,debug) + (isNegated() ? " is exist" : "isn't exist");

    }
}
