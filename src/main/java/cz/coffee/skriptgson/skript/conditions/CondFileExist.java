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

@Name("is JsonFile exist")
@Description("Checks if jsonFile exist")
@Examples({"on load:",
        "\tjson file \"test\\test.json\" exists:",
        "\t\tbroadcast true"
})
@Since("1.0")

public class CondFileExist extends Condition {

    static {
        Skript.registerCondition(CondFileExist.class,
                "json file %object% is exists",
                "json file %object% is( not|n't) exists"
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
        File object;
        Object rawobject = check.getSingle(e);
        if ( rawobject instanceof File) {
            object = (File) rawobject;
        } else if ( rawobject instanceof String ){
            object = new File(rawobject.toString());
        } else {
            return false;
        }
        return ( pattern ==  0) == object.exists();
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "json file" + check.toString(e,debug) + (isNegated() ? " is exist" : "isn't exist");

    }
}
