package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import cz.coffee.skriptgson.SkriptGson;
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


@SuppressWarnings({"unchecked","unused","NullableProblems"})
public class ExprNewJsonFile extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprNewJsonFile.class,Object.class, ExpressionType.COMBINED,
                "[a] new json file %string% [(:with [data] %-string/jsonelement%)]");

    }

    private Expression<String> exprString;
    private Expression<String> exprRawData;
    private boolean withData;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprString = (Expression<String>) exprs[0];
        if (!parseResult.tags.isEmpty()) {
            exprRawData = (Expression<String>) exprs[1];
            withData = true;
        }
        return true;
    }

    public File[] get(@NotNull Event event) {

        if (exprString == null)
            return null;
        String inputFile = exprString.getSingle(event);
        if (inputFile == null)
            return null;

        FileOutputStream OutputStream;
        JsonWriter writer;
        JsonElement parsedString;
        Object inputData;

        inputData = (exprRawData != null ? exprRawData.getSingle(event) : null);

        if (new File(inputFile).exists()){
            if(new File(inputFile).length() >1) {
                SkriptGson.warning("&r&ccan't create the file &e" + inputFile + ",&c because is already exist");
                return null;
            }
        }

        try {
            OutputStream = new FileOutputStream(inputFile);
            writer = new JsonWriter(new OutputStreamWriter(OutputStream, StandardCharsets.UTF_8));
            writer.setIndent("    ");
            writer.jsonValue(newGson()
                    .toJson(inputData instanceof JsonElement ? inputData : JsonParser.parseString(inputData == null ? "{}" : (String) inputData))
            );
            writer.flush();
            writer.close();
        } catch (Exception e) {
            return null;
        }
        return new File[]{new File(inputFile)};
    }
    @Override
    public String toString(Event event,boolean debug) {
        return "json file " + (!withData ? "" : "with data");}

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends File> getReturnType() {
        return File.class;
    }
}
