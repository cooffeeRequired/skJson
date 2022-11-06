package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;

import java.io.File;

@Name("is JsonFile exist")
@Description("Checks if jsonFile exist")
@Examples({
        "json file \"test\\test.json\" exist:",
        "\t broadcast true"
})
@Since("1.0")

@SuppressWarnings({"unchecked","unused","NullableProblems"})

public class CondJsonFileExist extends Condition {

    static {
        Skript.registerCondition(CondJsonFileExist.class,
                "json-file %object% is exist",
                "json-file %object% is( not|n't) exist[s]"
        );
    }

    private Expression<Object> check;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        check = (Expression<Object>) exprs[0];
        SkriptGson.debug("IN");
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }

    @Override
    public boolean check(Event e) {
        File object;
        Object RawObject = check.getSingle(e);
        if ( RawObject instanceof File) {
            object = (File) RawObject;
        } else if ( RawObject instanceof String ){
            object = new File(RawObject.toString());
        } else {
            return false;
        }
        return ( pattern ==  0) == object.exists();
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "json file" + check.toString(e,debug) + (isNegated() ? " is exist" : "isn't exist");

    }
}
