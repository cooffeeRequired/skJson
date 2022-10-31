/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */

package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
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


@SuppressWarnings({"unchecked","unused","NullableProblems"})

@Since("1.0")
@Name("New json-file with or without json data")
@Description("Create a new json-file with/out data")
@Examples({
        "\n", "on load:",
        "\tset {-e} to json {\"anything\": [1,2,\"false\"]",
        "\tnew json file \"plugins\\YourAwesomeAddon\\config.json\"",
        "\t #Example 2 With data",
        "\tnew json file \"plugins\\YourAwesomeAddon\\config.json\" with data {-e}"
})
public class EffNewJsonFile extends Effect {
    static {
        Skript.registerEffect(EffNewJsonFile.class,
                "[a] [new] json file %string% [(:with data %-string/jsonelement%)]");

    }
    private Expression<String> exprString;
    private Expression<?> exprRawData;

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
        Object inputData;

        inputData = (exprRawData != null ? exprRawData.getSingle(event) : null);

        if (new File(inputFile).exists())
            return;

        try {
            OutputStream = new FileOutputStream(inputFile);
            writer = new JsonWriter(new OutputStreamWriter(OutputStream, StandardCharsets.UTF_8));
            writer.setIndent("    ");
            writer.jsonValue(new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(inputData instanceof JsonElement ? inputData : JsonParser.parseString(inputData == null ? "{}" : (String) inputData))
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
            exprRawData = exprs[1];
        }
        return true;
    }
}
