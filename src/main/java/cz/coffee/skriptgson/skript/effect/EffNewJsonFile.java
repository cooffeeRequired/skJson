package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


@SuppressWarnings({"unchecked","unused"})
public class EffNewJsonFile extends Effect {
    static {
        Skript.registerEffect(EffNewJsonFile.class,
                "[a] [new] json file %string% [(:with data %-string%)]");

    }
    private Expression<String> exprString;
    private Expression<String> exprData;

    @Override
    protected void execute(Event event) {
        if (exprString == null)
            return;
        String inputFile = exprString.getSingle(event);
        if (inputFile == null)
            return;

        FileOutputStream OutputStream;
        JsonWriter writer;
        JsonElement parsedString;
        String inputData;

        inputData = (exprData != null ? exprData.getSingle(event) : null);

        if (new File(inputFile).exists())
            return;

        try {
            OutputStream = new FileOutputStream(inputFile);
            writer = new JsonWriter(new OutputStreamWriter(OutputStream, StandardCharsets.UTF_8));
            writer.setIndent("    ");
            writer.jsonValue(new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(
                            JsonParser.parseString(inputData == null ? "{}" : inputData)
                    )
            );
            writer.flush();
            writer.close();
        } catch (IOException | JsonSyntaxException ignored) {}
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprString = (Expression<String>) exprs[0];
        if (!parseResult.tags.isEmpty()) {
            exprData = (Expression<String>) exprs[1];
        }
        return true;
    }
}
