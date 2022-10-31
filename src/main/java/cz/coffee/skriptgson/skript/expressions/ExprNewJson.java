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
import com.google.gson.JsonSyntaxException;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.util.regex.MatchResult;

@Name("JSON from Text/File")
@Description("Creates a new JSON object from text or file.")
@Since("1.0")
@Examples({
        "\n", "on load:",
        "\tset {-e} to json {\"anything\": [1,2,\"false\"]",
        "\tset {-e} to json [1,2, {\"A\":\"B\"}]",
        "\tset {-e} to json 2 # used for jsonPrimitive",
        "\tset {-e} to json from string \"{'anything': [1,2,'false']}\"",
        "\tset {-e} to json from file \"plugins\\YourAwesome\\test.json\"",
})

@SuppressWarnings({"unused","NullableProblems","unchecked"})
public class ExprNewJson extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprNewJson.class, Object.class, ExpressionType.COMBINED,
                "[a] [new] json from (string|text) %string%",
                "[a] [new] json from file [path] %string%",
                "json (([<.+>])|({<.+>}))",
                "(<'.+'>)"
        );
    }

    @Nullable
    private Expression<String> exprString;
    private String regexString;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pattern = matchedPattern;
        if ( pattern == 2 || pattern == 3 ){
            for (MatchResult regex : parseResult.regexes) {
                regexString =  regex.group(0);
            }
        } else {
            exprString = (Expression<String>) exprs[0];
        }
        return true;
    }

    @Override
    public JsonElement[] get(Event e) {
        String inputString;

        if (pattern == 2 || pattern == 3) {
            if (regexString == null){
                return null;
            }
            inputString = regexString;
        } else {
            if (exprString == null)
                return null;
            inputString = exprString.getSingle(e);
        }
        if (inputString == null)
            return null;
        JsonElement json;
        if (pattern == 0 || pattern == 2 || pattern == 3) {
            try {
                json = JsonParser.parseString(inputString);
            } catch (JsonSyntaxException ex) {
                return null;
            }
        } else {
            FileReader reader;
            try {
                reader = new FileReader(inputString);
                json = JsonParser.parseReader(reader);
            } catch (Exception ex) {
                return null;
            }
        }
        if (json.isJsonNull())
            return null;
        return new JsonElement[]{json};
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
