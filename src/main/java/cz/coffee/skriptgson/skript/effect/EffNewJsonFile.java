package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.adapters.Adapters;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.GsonUtils.GsonFileHandler.newFile;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("New JSON file.")
@Description("Create a new json file with or without data.")
@Examples({"on load:",
        "\tset {_data} to new json from text \"{'test': true}\"",
        "\tnew json file \"gson/test.json\"",
        "\tnew json file \"gson/test.json\" with location(10,-10,10, world \"World\")",
        "\tnew json file \"gson/test.json\" with {_data} with force",
        "\tnew json file \"gson/test.json\" with item iron sword named \"This is a test\"",
        "\tnew json file \"gson/test.json\" with new JsonObject",
        "\tnew json file \"gson/test.json\" with new JsonArray",
})
@Since("2.0.0")

public class EffNewJsonFile extends Effect {

    static {
        Skript.registerEffect(EffNewJsonFile.class,
                "new json file %string% [(:with force)]",
                "new json file %string% with %jsonelement% [(:with force)]",
                "new json file %string% with %object% [(:with force)]",
                "new json file %string% with new ((:jsonobject)|(:jsonarray)) [(:with force)]",
                "new json file %string% with %itemstack% as item [(:with force)]"
        );
    }

    private Expression<String> fileExpression;
    private Expression<JsonElement> jsonExpression;
    private Expression<Object> genericObjectExpression;
    private Expression<ItemType> itemTypeExpression;
    private int pattern;
    private boolean force;
    private boolean newArray, newObject;

    @Override
    protected void execute(@NotNull Event e) {
        String fileExpression = this.fileExpression.getSingle(e);
        JsonElement jsonExpression = null;
        Object genericObjectExpression = null;
        Object itemTypeExpression = null;

        if (pattern == 1)
            jsonExpression = this.jsonExpression.getSingle(e);
        else if (pattern == 2)
            genericObjectExpression = this.genericObjectExpression.getSingle(e);
        else if (pattern == 4)
            itemTypeExpression = this.itemTypeExpression.getSingle(e);

        if (newObject)
            newFile(fileExpression, force, new JsonObject());
        else if (newArray)
            newFile(fileExpression, force, new JsonArray());
        else if (jsonExpression == null && genericObjectExpression == null && itemTypeExpression == null) {
            newFile(fileExpression, force, null);
            JSON_HASHMAP.put(fileExpression, null);
        } else if (genericObjectExpression != null)
            newFile(fileExpression, force, Adapters.toJson(genericObjectExpression));
        else if (jsonExpression != null)
            newFile(fileExpression, force, jsonExpression);
        else
            newFile(fileExpression, force, hierarchyAdapter().toJsonTree(itemTypeExpression));
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if (pattern == 0)
            return "new json file " + fileExpression.toString(e, debug) + (force ? " with forcing" : "");
        if (pattern == 1)
            return "new json file " + fileExpression.toString(e, debug) + " with " + jsonExpression + (force ? " with forcing" : "");
        if (pattern == 2)
            return "new json file " + fileExpression.toString(e, debug) + " with " + genericObjectExpression + (force ? " with forcing" : "");
        if (pattern == 3)
            return "new json file " + fileExpression.toString(e, debug) + "new JsonEelemt" + (force ? " with forcing" : "");
        if (pattern == 4)
            return "new json file " + fileExpression.toString(e, debug) + " item " + itemTypeExpression.toString(e, debug) + (force ? " with forcing" : "");
        return "";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        force = parseResult.hasTag("with force");
        fileExpression = (Expression<String>) exprs[0];

        if (pattern == 1)
            jsonExpression = (Expression<JsonElement>) exprs[1];
        else if (pattern == 2) {
            genericObjectExpression = LiteralUtils.defendExpression(exprs[1]);
            return LiteralUtils.canInitSafely(genericObjectExpression);
        } else if (pattern == 3) {
            newArray = parseResult.hasTag("jsonarray".toLowerCase());
            newObject = parseResult.hasTag("jsonobject".toLowerCase());
        } else if (pattern == 4)
            itemTypeExpression = (Expression<ItemType>) exprs[1];
        return true;
    }
}
