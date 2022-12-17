package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.stream.JsonWriter;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static cz.coffee.skriptgson.SkriptGson.FILE_JSON_HASHMAP;
import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.GsonUtils.canCreate;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("Save Json content to cached Json")
@Description({"Save changed content to cached json, and rewrite those values"})
@Examples({"on script load:",
        "\tsave [cached] json-id \"your\""
})
@Since("2.0.0")

public class EffSaveCachedJson extends Effect {

    static {
        Skript.registerEffect(EffSaveCachedJson.class, "save [cached] json[(-| )id] %string%");
    }

    private Expression<String> stringIdExpression;

    @Override
    protected void execute(@NotNull Event e) {
        GsonErrorLogger err = new GsonErrorLogger();
        String stringIdExpression = this.stringIdExpression.getSingle(e);

        if (JSON_HASHMAP.containsKey(stringIdExpression)) {
            if (FILE_JSON_HASHMAP.containsKey(stringIdExpression)) {
                File file = FILE_JSON_HASHMAP.get(stringIdExpression);
                try (var protectedWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                    protectedWriter.jsonValue(hierarchyAdapter().toJson(JSON_HASHMAP.get(stringIdExpression)));
                    protectedWriter.flush();
                } catch (IOException exception) {
                    if (!canCreate(file)) {
                        SkriptGson.warning(err.PARENT_DIRECTORY_NOT_EXIST);
                    } else {
                        SkriptGson.warning(exception.getMessage());
                    }
                }
            }

        } else {
            SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "save cached json-id " + stringIdExpression.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        stringIdExpression = (Expression<String>) exprs[0];
        return true;
    }
}
