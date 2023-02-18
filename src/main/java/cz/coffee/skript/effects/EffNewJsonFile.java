package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.adapter.DefaultAdapters;
import cz.coffee.utils.json.JsonFilesHandler;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;



@Name("New json file with given path")
@Description({
        "New json file with given path, with or without user data.",
        "When the data was null, parser will automatically create a jsonobject inside the file",
        "When you will handle big json payload i recommend you use 'async new json file ....' "
})
@Examples({
        "on script load:",
        "\tif json file \"...\" does not exists:",
        "\t\tnew json file \"...\""
})
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
        Object assignedValue;
        if (strPathToFile == null) return;
        File file = new File(strPathToFile);

        JsonElement json = null;
        if (inputToFile != null) {
            assignedValue = inputToFile.getSingle(event);
            json = DefaultAdapters.parse(assignedValue, inputToFile, event);
        }

        if (hasObject) {
            jfh.writeFile(file, new JsonObject(), false);
        } else if (hasArray) {
            jfh.writeFile(file, new JsonArray(), false);
        } else {
            jfh.writeFile(file, json, async);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (async ? "async" : "") + " new json file" + pathToFileExpr.toString(e, debug) + " with " + inputToFile.toString(e, debug);
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
            return LiteralUtils.canInitSafely(inputToFile);
        }
        return true;
    }
}
