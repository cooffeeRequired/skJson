package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.api.json.SerializedJsonUtils;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Simple json literal")
@Description({
        "This syntax is strictly limited to its intended use; it does allow other expressions or variables to be used.",
        "**RECOMMENDATION**: Use this syntax exclusively for paths that are a maximum of 2 keys deep.",
        "Can handle strict (get/set/remove)",
        "* at the end means you want to return a skript list.",
        "If the data is a JSON array, it is straightforward.",
        "If it is a JSON object, all the values are extracted and converted into Java objects, making them directly usable within the skript."
})
@Examples("""
        set {_json} to json from "{array: [{A: 1, B: 2, C: 3, location: {}}]}"
        
        send {_json}.array[0]* # will print 1,2,3,{}
        
        #send {_json}.array[0] # will print {A: 1, B: 2, C: 3, location: {}}
        
        set {_json}.array[0].location.loc to location(1, 2, 3) # will set location to key
        # OUTPUT
        {
          "array": [
            {
              "A": 1,
              "B": 2,
              "C": 3,
              "location": {
                "loc": {
                  "class": "org.bukkit.Location",
                  "world": "world",
                  "x": 1.0,
                  "y": 2.0,
                  "z": 3.0,
                  "pitch": 0.0,
                  "yaw": 0.0
                }
              }
            }
          ]
        }
        
        send {_json} as uncolored pretty printed
        """)
@Since("4.5")
public class ExprStrictLiteralJson extends SimpleExpression<Object> {

    private ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens;

    private Expression<JsonElement> jsonElementExpression;
    private Expression<?> v;

    @Override
    protected @Nullable Object[] get(Event event) {
        if (v != null) {
            String parsed = v.getSingle(event) + "";
            tokens = SkriptJsonInputParser.tokenizeFromPattern(parsed);
        }
        JsonElement jsonElement = jsonElementExpression.getSingle(event);
        if (jsonElement == null) return new Object[0];

        SerializedJson serializedJson = new SerializedJson(jsonElement);
        Object searcherResult = serializedJson.searcher.keyOrIndex(tokens);
        if (searcherResult == null) return new Object[0];

        if (tokens.getLast().getValue().equals(SkriptJsonInputParser.Type.ListAll)) {
            return SerializedJsonUtils.getAsParsedArray(searcherResult);
        } else {
            return new Object[]{searcherResult};
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return Classes.getDebugMessage(jsonElementExpression) + " " + tokens.toString();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        var r = parseResult.regexes.getFirst();
        jsonElementExpression = defendExpression(expressions[0]);

        var group = r.group();

        SkJson.debug("regexes: %s", group);

        tokens = SkriptJsonInputParser.tokenizeFromPattern(group);

        SkJson.debug("tokens: %s", tokens);

        if (group.contains("%")) {
            v = parseExpression(group);
        }
        return !tokens.isEmpty() && canInitSafely(jsonElementExpression);
    }

    private boolean isQuoted(String original) {
        return original.startsWith("\"") && original.endsWith("\"");
    }

    private boolean isExpression(String original) {
        if (isQuoted(original)) {
            original = original.substring(1, original.length() - 1);
        }
        return original.startsWith("%") && original.endsWith("%");
    }

    public @Nullable Expression<?> parseExpression(String expr) {
        Expression<?> result;
        if (isExpression(expr) && !isQuoted(expr)) {
            SkJson.warning("Expression '%s' is quoted but not quoted", expr);
            return null;
        }
        result = VariableString.newInstance(expr, StringMode.VARIABLE_NAME);
        SkJson.debug("expression: %s -> parsed result: %s", expr, result);
        return result;
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(Object.class, Object[].class);
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (mode.equals(Changer.ChangeMode.SET)) {
            if (delta == null) return;

            JsonElement jsonElement = jsonElementExpression.getSingle(event);
            for (Object o : delta) {
                JsonElement parsed = GsonParser.toJson(o);
                SerializedJson serializedJson = new SerializedJson(jsonElement);
                serializedJson.changer.value(tokens, parsed);
            }
        }
    }
}
