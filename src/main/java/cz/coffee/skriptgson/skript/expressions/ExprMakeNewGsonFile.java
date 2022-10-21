package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import cz.coffee.skriptgson.Util.GsonDataApi;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.io.File;

@Name("Return new Gson File.")
@Since("1.0")
@Description({})
@Examples({})

@SuppressWarnings({"unused", "unchecked"})
public class ExprMakeNewGsonFile extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprMakeNewGsonFile.class, Object.class, ExpressionType.COMBINED,"[a] [new] gson file (named|with name) %string%");
    }

    private Expression<String> fileName;

    protected Object[] get(Event e) {
        File api = new GsonDataApi(this.fileName.getSingle(e)).createFile();

        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    /**
     * @param e     The event to get information to. This is always null if debug == false.
     * @param debug If true this should print more information, if false this should print what is shown to the end user
     * @return String representation of this object
     */
    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "a new gson file named " + fileName;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        fileName = (Expression<String>) exprs[0];
        return true;
    }

}
