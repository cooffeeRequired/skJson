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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Simple json literal")
@Description({
        "<b>Explanatory notes</b>",
        "* - This expression is used to get a values (list) from a json object|array.",
        "  - Need to be at the end of the path.",
        "","",
        "This syntax is a simplification for json path, and shortening the notation and getting closer to the form (jq or other popular tools)",
        "**RECOMMENDATION**: Use this syntax exclusively for paths that are a maximum of 2 keys deep. - for Creating",
        "- Use rather value/key path expression",
        "Can handle strict (get/set/remove)",
        "all variables or expressions must be in “% ... %”",

        "This syntax is strictly limited to its intended use; it does allow other expressions or variables to be used.",
        "**RECOMMENDATION**: Use this syntax exclusively for paths that are a maximum of 2 keys deep.",
        "Can handle strict (get/set/remove)",
        "* at the end means you want to return a skript list.",
})
@Examples("""
        set {_json} to json from "{}"
    
        # SET
        set {_json}.list to "[]"
        set {_json}.object to "{}"
        set {_json}.object.key to "value"
        set {_json}.object.val to "value 2"
    
        # ADD
        add 40 and "K" to {_json}.list
        add location(0, 0, 0) and location(0, 0, 1) to {_json}.list
        add 10 to {_json}.object
    
        # REMOVE
        remove 1 and 40 from {_json}.list
        remove "good" from {_json}.list
        remove "value" and "value 2" from {_json}.object
    
        # DELETE -> keys
        delete {_json}.object
        delete {_json}.list
        delete {_json}.list[0]
    
        # GET
        send {_json}.list.0 #* -> 1
        send {_json}.list.1 #* -> 2
        send {_json}.list.2 #* -> "good"
        send {_json}.list #* -> 1, 2, "good"
        send {_json}.object.key #* -> "value"
    
        #REMOVE ALL
        remove all 1 from {_json}.list
        remove all "value" from {_json}.object
    
        # RESET
        reset {_json}.object
        """)
@Since({"4.5", "5.1.2"})
public class ExprStrictLiteralJson extends SimpleExpression<Object> {

    private ArrayList<Map.Entry<String, PathParser.Type>> tokens;

    private Expression<JsonElement> jsonElementExpression;
    private Expression<?> v;

    @Override
    protected @Nullable Object[] get(Event event) {
        if (v != null) {
            String parsed = v.getSingle(event) + "";
            SkJson.debug("expression: %s -> parsed result: %s", v, parsed);
            tokens = PathParser.tokenizeFromPattern(parsed);
        }
        JsonElement jsonElement = jsonElementExpression.getSingle(event);
        if (jsonElement == null) return new Object[0];

        JsonAccessor serializedJson = new JsonAccessor(jsonElement);
        Object searcherResult = serializedJson.searcher.keyOrIndex(tokens);
        if (searcherResult == null) return new Object[0];

        if (tokens.getLast().getValue().equals(PathParser.Type.ListAll)) {
            return JsonAccessorUtils.getAsParsedArray(searcherResult);
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
        SkJson.debug("&egroup: %s", group);
        if (group.contains("%")) {
            v = parseExpression(group);
            tokens = new ArrayList<>();
        } else {
            tokens = PathParser.tokenizeFromPattern(group);
        }
        return canInitSafely(jsonElementExpression);
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
        return result;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, REMOVE_ALL -> CollectionUtils.array(Object[].class);
            case DELETE, RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        JsonElement jsonElement = jsonElementExpression.getSingle(event);
        JsonAccessor serializedJson = new JsonAccessor(jsonElement);


        if (v != null) {
            String parsed = v.getSingle(event) + "";
            SkJson.debug("expression: %s -> parsed result: %s", v, parsed);
            tokens = PathParser.tokenizeFromPattern(parsed);
        }

        if (mode.equals(Changer.ChangeMode.DELETE)) {
            serializedJson.remover.byKey(tokens);
        } else if (mode.equals(Changer.ChangeMode.RESET)) {
            serializedJson.remover.reset(tokens);
        }

        if (delta == null) return;

        if (mode.equals(Changer.ChangeMode.SET)) {
            if (delta.length > 1) {
                JsonArray array = new JsonArray();

                for (Object o : delta) {
                    JsonElement parsed = Parser.toJson(o);
                    array.add(parsed);
                }

                serializedJson.changer.value(tokens, array);
            } else {
                for (Object o : delta) {
                    JsonElement parsed = Parser.toJson(o);
                    serializedJson.changer.value(tokens, parsed);
                }
            }
        } else if (mode.equals(Changer.ChangeMode.ADD)) {
            for (Object o : delta) {
                JsonElement parsed = Parser.toJson(o);
                serializedJson.changer.add(tokens, parsed);
            }
        } else if (mode.equals(Changer.ChangeMode.REMOVE)) {
            for (var o : delta) {
                serializedJson.remover.byValue(tokens, o);
            }
        } else if (mode.equals(Changer.ChangeMode.REMOVE_ALL)) {
            for (var o : delta) {
                serializedJson.remover.allByValue(tokens, o);
            }
        }
    }
}
