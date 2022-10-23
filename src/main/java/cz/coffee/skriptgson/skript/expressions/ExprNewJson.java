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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;

@Name("JSON from Text/File")
@Description("Creates a new JSON object from text or file.")
@Since("1.0")

public class ExprNewJson extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprNewJson.class, Object.class, ExpressionType.COMBINED,
                "[a] [new] json from (string|text) %string%",
                "[a] [new] json from file [path] %string%"
        );
    }

    @Nullable
    private Expression<String> exprString;
    private int pattern;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprString = (Expression<String>) exprs[0];
        pattern = matchedPattern;
        return true;
    }

    @Override
    public JsonElement[] get(Event event) {
        if (exprString == null)
            return null;
        String inputString = exprString.getSingle(event);
        if (inputString == null)
            return null;

        JsonElement json;
        if (pattern == 0) {
            try {
                json = JsonParser.parseString(inputString);
            } catch (JsonSyntaxException ex) {
                return null;
            }
            return new JsonElement[]{json};
        } else {
            FileReader reader;
            try {
                reader = new FileReader(inputString);
                json = JsonParser.parseReader(reader);
            } catch (Exception ex) {
                return null;
            }
            return new JsonElement[]{json};
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "json from " + (pattern == 0 ? "text" : "file");
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

}
