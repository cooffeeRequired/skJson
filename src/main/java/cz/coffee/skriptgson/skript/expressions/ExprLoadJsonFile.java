package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import java.io.File;

@SuppressWarnings({"unused","NullableProblems","unchecked"})


@Name("Load created file")
@Since("1.0")
@Description({
        "load the created file to variable."
})
@Examples({
        "on load:",
        "\tset {_file} to loaded json file \"data/test.json\"}"
})

public class ExprLoadJsonFile extends SimpleExpression<File> {

    static {
        Skript.registerExpression(ExprLoadJsonFile.class, File.class, ExpressionType.COMBINED,
                "(load|open)[ed] json file %string%");
    }

    private Expression<String> rawFile;

    @Override
    protected  File[] get(Event e) {
        String w = rawFile.getSingle(e);
        if ( w == null)
            return null;
        File file = new File(w);
        return file.exists() ? new File[]{file} : null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends File> getReturnType() {
        return File.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        rawFile = (Expression<String>) exprs[0];
        return true;
    }
}
