package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.adapters.JsonAdapter;
import cz.coffee.utils.json.JsonFilesHandler;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


@Since("2.5.0")

public class EffNewJsonFile extends Effect {

    private Expression<?> inputToFile;
    private Expression<String> pathToFileExpr;
    private int pattern;
    private boolean hasArray, hasObject, async;

    static {
        Skript.registerEffect(EffNewJsonFile.class,
                "[:async] new json file %string% [with %-object%]"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
        JsonFilesHandler jfh = new JsonFilesHandler();
        String strPathToFile = (pathToFileExpr.getSingle(event));
        Object assignedValue = null;
        assert strPathToFile != null;
        if (inputToFile != null) {
            assignedValue = inputToFile.getSingle(event);
        }
        if (hasObject) {
            jfh.newFile(strPathToFile, new JsonObject(), true, false);
        } else if (hasArray) {
            jfh.newFile(strPathToFile, new JsonArray(), true, false);
        }
        if (assignedValue instanceof JsonElement) {
            jfh.newFile(strPathToFile, assignedValue, true, async);
        } else if (assignedValue instanceof String) {
            jfh.newFile(strPathToFile, assignedValue, true, async);
        } else {
            jfh.newFile(strPathToFile, JsonAdapter.toJson(assignedValue), true, true);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (async ? "async" : "") + " new json file" + pathToFileExpr.toString(e, debug) + " with " + (pattern == 1 ? inputToFile.toString(e, debug) : "item " + inputToFile.toString(e, debug));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        async = parseResult.hasTag(("async"));
        hasObject = parseResult.hasTag(("object"));
        hasArray = parseResult.hasTag(("array"));
        pattern = matchedPattern;

        pathToFileExpr = (Expression<String>) expressions[0];
        if (expressions[1] != null) {
            inputToFile = LiteralUtils.defendExpression(expressions[1]);
            Expression<?> isItem = inputToFile.getConvertedExpression(ItemStack.class);
            inputToFile = Objects.requireNonNullElseGet(isItem, () -> LiteralUtils.defendExpression(expressions[1]));
            return LiteralUtils.canInitSafely(inputToFile);
        }
        return true;
    }
}
