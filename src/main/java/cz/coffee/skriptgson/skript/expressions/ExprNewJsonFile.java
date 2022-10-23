package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.event.Event;

import java.io.File;

@Name("New json File")
@Description("Creates a new JSON file")
@Since("1.0")


@SuppressWarnings("unchecked")
public class ExprNewJsonFile extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprNewJsonFile.class,Object.class, ExpressionType.COMBINED,"" +
                "[a] [new] json file %string%",
                "[a] [new] json file %string% with [data] %object%"
        );

    }

    private Expression<String> exprString;
    private Expression<Object> exprObject;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprString = (Expression<String>) exprs[0];
        exprObject = (Expression<Object>) exprs[1];
        pattern = matchedPattern;
        return true;
    }

    public File[] get(Event event) {
        if ( pattern == 0) {
            try {
                JsonElement json = JsonParser.parseString("");
            } finally {
                JsonElement json = JsonParser.parseString("");
            }
            return null;
        }
        return new File[0];
    }

    @Override
    public String toString(Event event,boolean debug) {
        return "json file " + (pattern == 0 ? "" : "with data");
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?> getReturnType() {
        return null;
    }
}
