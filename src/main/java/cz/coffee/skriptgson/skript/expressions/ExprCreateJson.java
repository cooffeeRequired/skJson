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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.Utils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static cz.coffee.skriptgson.util.Utils.newGson;

@Name("JSON from Text/File/Request")
@Description("Creates a new JSON object from test/file/request")
@Since("1.3.1")
@Examples({"command example:",
        "\tsend new json from string \"{'Hello': 'There'}\"",
        "\tsend new json from player's location",
        "\tsend new json from arg-1",
        "#Change v1.3.1",
        "\tset {_l} to \"Good Test\"",
        "\tset {_num} to 2222.111",
        "\tsend new json from string \"{\"Test\": ${_l}, \"Number\": ${_num}\" with variables"
})

@SuppressWarnings({"unused"})
public class ExprCreateJson extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprCreateJson.class, Object.class, ExpressionType.COMBINED,
                "[a] new json from [(text|string)] %object% [(:with variables)]",
                "[a] new json from file [path] %object%",
                "[a] new json from request %object% [(:with variables)]"
        );
    }

    private Expression<?> toParse;
    private int pattern;
    private boolean with;

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        with = parseResult.hasTag("with variables");
        if(pattern == 2) {
            SkriptGson.warning("&cSorry but at this moment you can't use this syntax.");
            return false;
        }
        toParse = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(toParse);
    }

    @Override
    protected JsonElement @NotNull [] get(@NotNull Event e) {
        Object nonParsedData = toParse.getSingle(e);
        boolean object = nonParsedData instanceof ConfigurationSerializable || nonParsedData instanceof YggdrasilSerializable;
        JsonElement element = null;
        if(nonParsedData == null)
            return new JsonElement[0];

        String rawString = nonParsedData.toString();

        if(pattern == 0 | pattern == 2) {
            if(with) {
                return new JsonElement[]{parsedVariable(rawString,e)};
            }
        }

        if(pattern == 0) {
            try {element = (object) ? newGson().toJsonTree(nonParsedData) : JsonParser.parseString(rawString);
            } catch (JsonSyntaxException jsonSyntaxException){
                if(jsonSyntaxException.getMessage().contains("Unterminated object") || jsonSyntaxException.getMessage().contains("$")) {
                    SkriptGson.severe("It looks like you are using Script-Gson variables &e'${...}'&c, try using &e'with variables'");
                    SkriptGson.severe("Your input string: new json from string &e'"+toParse.toString()+"'"+(with ? "&bwith variables" : "" ));
                }
            }
        } else if(pattern == 1){
            try (var protectedReader = new JsonReader(new FileReader(nonParsedData.toString()))) {
                element = JsonParser.parseReader(protectedReader);
            } catch (IOException ex) {
                SkriptGson.severe(ex.getMessage());
                return new JsonElement[0];
            }
        }
        return new JsonElement[]{element};
    }

    @Override
    public boolean isSingle() {return true;}

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {return JsonElement.class;}


    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if(pattern == 0)
            return "new json from string '"+toParse.toString(e, debug)+"'"+(with ? "with variables" : "" );
        else if(pattern == 1)
            return "new json from file '"+toParse.toString(e, debug)+"'";
        else
            return "new json from request '"+toParse.toString(e, debug)+"'"+(with ? "with variables" : "" );
    }

    private Object getSkriptVariable(Object input, Event e) {
        boolean isLocal = false;
        JsonElement newJsonElement;
        JsonObject output = new JsonObject();
        HashMap<String, Object> returnMap = new HashMap<>();
        String name = input.toString().replaceAll("[{}]", "");
        if (name.startsWith("$_")) {
            isLocal = true;
            name = name.replaceAll("_", "").replaceAll("[$]","Variable.");
        }
        Object variable = Variables.getVariable(name.replaceAll("Variable.",""), e, isLocal);

        newJsonElement = newGson().toJsonTree(variable);
        if (variable == null)
            newJsonElement = new JsonPrimitive(false);

        output.add("variable", newJsonElement);
        returnMap.put(name, output);

        return returnMap;
    }

    private JsonElement parsedVariable(String rawString, Event e) {
        Matcher m = Pattern.compile("\\$\\{.+?}").matcher(rawString);
        rawString = rawString.replaceAll("(?<!^)[_{}*](?!$)", "").replaceAll("[$]","Variable.");

        for (Iterator<Object> it = m.results().map(MatchResult::group).map(k-> getSkriptVariable(k,e)).iterator(); it.hasNext(); ) {
            String Value;
            JsonObject object = newGson().toJsonTree(it.next()).getAsJsonObject();
            for(Map.Entry<String, JsonElement> map : object.entrySet()) {
                JsonObject json = map.getValue().getAsJsonObject();

                if(json.get("variable").isJsonObject()) {
                    Stream<String> keys = json.get("variable").getAsJsonObject().keySet().stream().filter(Objects::nonNull);
                    if (json.get("variable").getAsJsonObject().keySet().stream().filter(Objects::nonNull).allMatch(Utils::isNumeric)) {
                        JsonArray array = new JsonArray();
                        keys.forEach(k -> array.getAsJsonArray().add(json.get("variable").getAsJsonObject().get(k)));
                        Value = array.toString();
                    } else {
                        Value = json.getAsJsonObject().get("variable").toString();
                    }
                }else{
                    Value = json.get("variable").toString();
                }
                rawString = rawString.replaceAll(map.getKey(), Value);
            }
        }
        return JsonParser.parseString(rawString);
    }
}
