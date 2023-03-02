package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.cache.Cache;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;


@Since("2.7.0 - define new cached json")
@Name("Pre-define new cached json")
@Description("You can define new cached json without a creating cached from file")
@Examples({"on load:",
        "define new cached json \"test1\" for given path \"items/try2.json\" with data diamond sword",
        "send cached json \"test1\""
})

public class EffDefineCachedJson extends Effect {

    static {
        Skript.registerEffect(EffDefineCachedJson.class,
                "[define] new cached json %string% for [given] path %string% [(:with) data %-object%]"
        );
    }

    private Expression<String> exprName, exprPath;
    private Expression<?> exprData;
    private boolean withData;

    @Override
    protected void execute(@NotNull Event e) {
        String fileString = exprPath.getSingle(e);
        String name = exprName.getSingle(e);
        if (fileString == null) return;
        File file = new File(fileString);
        JsonElement element;
        if (withData) {
            Object o = exprData.getSingle(e);
            element = DefaultAdapter.parse(o, exprData, e);
        } else {
            element = new JsonObject();
        }

        if (!Cache.contains(name)) {
            Cache.addTo(name, element, file);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "define new cached json " + exprName.toString(e, debug) + " for given path " + exprPath.toString(e, debug) + " " + (withData ? "with data " + exprData.toString(e, debug) : "");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        withData = parseResult.hasTag("with");
        exprName = (Expression<String>) exprs[0];
        exprPath = (Expression<String>) exprs[1];
        exprData = LiteralUtils.defendExpression(exprs[2]);
        return LiteralUtils.canInitSafely(exprData);
    }
}
