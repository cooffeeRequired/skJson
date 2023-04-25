package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static cz.coffee.core.utils.AdapterUtils.parseItem;


@Name("New json file")
@Description({"You can create a new json file."})
@Since("2.8.0 - performance & clean")

public class EffNewJsonFile extends AsyncEffect {

    static {
        Skript.registerEffect(EffNewJsonFile.class, "[:async] new json file %string% [(:with) (object|content)[s] %-object%]");
    }

    private boolean async, with;
    private Expression<String> expressionPath;
    private Expression<?> expressionObject;

    @Override
    protected void execute(@NotNull Event e) {
        String path = expressionPath.getSingle(e);
        JsonElement content;
        if (path == null) return;
        final File file = new File(path);
        if (with) {
            Object o = expressionObject.getSingle(e);
            if (o == null) {
                content = new JsonObject();
            } else {
                content = parseItem(o, o.getClass());
            }
        } else {
            content = new JsonObject();
        }
        FileUtils.write(file, content, async);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return String.format("%s new json file %s %s", (async ? "async" : ""), expressionPath.toString(e, debug), (with ? "with content " + expressionObject.toString(e, debug) : ""));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        with = parseResult.hasTag("with");
        getParser().setHasDelayBefore(Kleenean.TRUE);
        async = parseResult.hasTag("async");
        expressionPath = (Expression<String>) exprs[0];
        expressionObject = LiteralUtils.defendExpression(exprs[1]);
        if (with) return LiteralUtils.canInitSafely(expressionObject);
        return true;
    }
}
