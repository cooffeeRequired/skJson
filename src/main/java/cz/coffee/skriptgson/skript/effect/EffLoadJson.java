package cz.coffee.skriptgson.skript.effect;


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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static cz.coffee.skriptgson.SkriptGson.FILE_JSON_HASHMAP;
import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.GsonUtils.GsonFileHandler.fromFile;

@Name("Load json file as String ID")
@Description("You can load the JsonFile to cache with custom ID")
@Examples({"on script load:",
        "\tload json file \"plugins/skript-gson/test.json\" as \"gson-test\""
})
@Since("2.0.0")

public class EffLoadJson extends Effect {

    static {
        Skript.registerEffect(EffLoadJson.class,
                "load json file [path] %object% as %string%",
                "load json file [path] %object%"
        );
    }

    private Expression<Object> jsonFileExpression;
    private Expression<String> stringIdExpression;
    private int pattern;

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        jsonFileExpression = LiteralUtils.defendExpression(exprs[0]);
        if (LiteralUtils.canInitSafely(jsonFileExpression)) {
            if (pattern == 0) {
                stringIdExpression = LiteralUtils.defendExpression(exprs[1]);
                return LiteralUtils.canInitSafely(stringIdExpression);
            }
            return LiteralUtils.canInitSafely(jsonFileExpression);
        }
        return false;
    }

    @Override
    protected void execute(@NotNull Event e) {
        String jsonFileExpression = String.valueOf(this.jsonFileExpression.getSingle(e));
        String stringIdExpression = jsonFileExpression;
        if (pattern == 0) {
            stringIdExpression = this.stringIdExpression.getSingle(e);
        }
        if (jsonFileExpression == null) return;
        JsonElement jsonFromFile = fromFile(jsonFileExpression);
        if (jsonFromFile == null) return;
        JSON_HASHMAP.put(stringIdExpression, jsonFromFile);
        FILE_JSON_HASHMAP.put(stringIdExpression, new File(jsonFileExpression));
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "load json file path " + jsonFileExpression.toString(e, debug) + " as " + stringIdExpression.toString(e, debug);
    }
}
