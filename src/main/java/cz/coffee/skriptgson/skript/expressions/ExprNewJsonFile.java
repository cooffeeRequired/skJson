package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Name("New json File")
@Description("Creates a new JSON file")
@Since("1.0")


@SuppressWarnings({"unchecked","unused"})
public class ExprNewJsonFile extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprNewJsonFile.class,Object.class, ExpressionType.COMBINED,"" +
                "[a] [new] json file %string%",
                "[a] [new] json file %string% with [data] %string%");

    }

    private Expression<String> exprString;
    private Expression<String> exprData;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprString = (Expression<String>) exprs[0];
        pattern = matchedPattern;
        if ( pattern == 1) {
            exprData = (Expression<String>) exprs[1];
        }
        return true;
    }

    public Object[] get(@NotNull Event event) {
        if (exprString == null)
            return null;
        String inputFile = exprString.getSingle(event);
        if (inputFile == null)
            return null;

        FileOutputStream OutputStream;
        JsonWriter writer;
        JsonElement parsedString;
        String inputData;

        if (exprData != null) {
            inputData = exprData.getSingle(event);
        } else {
            inputData = null;
        }

        if (new File(inputFile).exists()) {
            return null;
        }

        try {
            OutputStream = new FileOutputStream(inputFile);
            writer = new JsonWriter(new OutputStreamWriter(OutputStream, StandardCharsets.UTF_8));
            writer.setIndent("    ");
            writer.jsonValue(new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(
                            JsonParser.parseString(pattern == 0 ? "{}" : inputData)
                    )
            );
            writer.flush();
            writer.close();
        } catch (IOException | JsonSyntaxException e) {
            return null;
        }
        return new Object[]{inputFile};
    }

    @Override
    public String toString(Event event,boolean debug) {return "json file " + (pattern == 0 ? "" : "with data");}

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }
}
