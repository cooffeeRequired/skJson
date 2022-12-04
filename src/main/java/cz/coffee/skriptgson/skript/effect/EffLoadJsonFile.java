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
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.util.Utils.color;

@Name("Load JSON file as ID")
@Description({"You can load the json file into memory and then continue working with it."})
@Examples({"on script load:",
        "   load json file \"plugins/example.json\" as \"yourID\"",
})
@Since("1.4.0")


public class EffLoadJsonFile extends Effect {


    private Expression<Object> jsonElement;
    private Expression<Object> rawID;


    static {
        Skript.registerEffect(EffLoadJsonFile.class, "load json file %object% as %object%");
    }

    private JsonElement inputReader(File file) {
        JsonElement element;
        try (var reader = new JsonReader(new FileReader(file))) {
            element = JsonParser.parseReader(reader);
            return element;
        } catch (JsonSyntaxException | IOException ex) {
            if (ex instanceof JsonSyntaxException) {
                Skript.error(color("&cThe json syntax isn't correct, error message " + ex.getMessage()));
            }
        }
        return null;
    }

    @Override
    protected void execute(@NotNull Event e) {
        Object rawObject = jsonElement.getSingle(e);
        if(rawObject == null) return;
        JsonElement json = inputReader(new File(rawObject.toString()));
        String ID = String.valueOf(rawID.getSingle(e));
        if(ID == null || json == null) return;
        JSON_HASHMAP.put(ID, json);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "load json file " + jsonElement.toString(e, debug) + "as " + rawID.toString(e,debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        jsonElement = LiteralUtils.defendExpression(exprs[0]);
        if(LiteralUtils.canInitSafely(jsonElement)) {
            rawID = LiteralUtils.defendExpression(exprs[1]);
            return LiteralUtils.canInitSafely(rawID);
        }
        return false;
    }
}
