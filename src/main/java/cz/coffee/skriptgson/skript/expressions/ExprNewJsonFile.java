package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static cz.coffee.skriptgson.util.Utils.newGson;

@Name("New json File")
@Description("Creates a new JSON file")
@Since("1.0")
@Examples({
        "set {-e} to new json file \"YouAreAwesome\\test.json\"",
        "set {-e} to new json file \"YouAreAwesome\\test.json\" with data (json {\"A\":\"B\"})",
        "set {-e} to new json file \"YouAreAwesome\\test.json\" with data (json from string \"{'Im': 'God'}\")",
})


public class ExprNewJsonFile extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprNewJsonFile.class, Object.class, ExpressionType.COMBINED,
                "[a] new json file %string% [(:with [data] %-string/jsonelement%)]");

    }

    private Expression<String> exprString;
    private Expression<String> exprRawData;
    private boolean withData;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
        exprString = (Expression<String>) exprs[0];
        if (!parseResult.tags.isEmpty()) {
            exprRawData = (Expression<String>) exprs[1];
            withData = true;
        }
        return true;
    }

    public File @NotNull [] get(@NotNull Event event) {
        if (exprString == null)
            return new File[0];
        String inputFile = exprString.getSingle(event);
        if (inputFile == null)
            return new File[0];

        FileOutputStream OutputStream;
        JsonWriter writer;
        Object inputData;

        inputData = (exprRawData != null ? exprRawData.getSingle(event) : null);

        if (new File(inputFile).exists()) {
            if (new File(inputFile).length() > 1) {
                return new File[]{new File(inputFile)};
            }
        }

        try {
            OutputStream = new FileOutputStream(inputFile);
            writer = new JsonWriter(new OutputStreamWriter(OutputStream, StandardCharsets.UTF_8));
            writer.setIndent("    ");
            writer.jsonValue(newGson()
                    .toJson(JsonParser.parseString(inputData == null ? "{}" : (String) inputData))
            );
            writer.flush();
            writer.close();
        } catch (Exception e) {
            return new File[0];
        }
        return new File[]{new File(inputFile)};
    }

    @Override
    public @NotNull String toString(Event event, boolean debug) {
        return "json file " + (!withData ? "" : "with data");
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends File> getReturnType() {
        return File.class;
    }
}
