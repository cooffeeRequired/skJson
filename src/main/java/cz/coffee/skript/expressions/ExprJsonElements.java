package cz.coffee.skript.expressions;

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
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.core.utils.AdapterUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cz.coffee.core.utils.JsonUtils.getByKey;
import static cz.coffee.core.utils.JsonUtils.getNestedElements;
import static cz.coffee.core.utils.Util.extractKeys;
import static cz.coffee.core.utils.Util.jsonToObject;

@SuppressWarnings("unused")
@Name("Elements/Values of the json")
@Description({"You can get the main json values, or you can also get the use values for a given object/array you can also get a single value",
        "<pre>loop-value, loop-element, loop-key</pre>",
        "",
        "Means :  -> entries entries mean the entry for the looped element, for example we have element {\"B\": false}",
                "    its entry will be loop-key = B, loop-element = false, loop-value = {B=false}",
                "!Warnings: loop-key or loop-element you can use only for entries!",

        "LOOP-PATTERNS ->",
        "\t<pre>loop-value, loop-element, loop-key</pre>"
})
@Examples("""
command GetElements:
  trigger:
    set {_json} to json from string "{'A': [{'B': {}}, false, true, 10, 20, 22.22, 'A']}"

    # Get single element of json
    set {_single} to element "A::1" of {_json} # That will return true cause, the first element of the array "A" is true


    # Get multiple elements of json
    set {_array::*} to elements "A" of {_json} # That will return whole array so that mean ({"B": {}}, false, true, 10, 20, 22.22, A)


    # Loop through elements
    loop elements "A" of {_json}:
        send loop-value

    # loop through entries of json
    execute GET request "https://api.json.com/producs"
    loop entries of request's body:
        if loop-key = "data":
            set {_data} to element "data" of loop-element
""")
@Since("2.8.0 performance & clean")


public class ExprJsonElements extends SimpleExpression<Object> {

    private static final List<String> loopEntriesStrings = new ArrayList<>();

    private boolean isValues, isEntries;
    private Expression<JsonElement> jsonElementExpression;
    private Expression<String> stringExpression;
    private static boolean loopEntries = false;

    static {
        Skript.registerExpression(
                ExprJsonElements.class, Object.class, ExpressionType.COMBINED,
                "(value|element) %string% of %object%",
                "(values|elements) [%-string%] of %object%",
                "entr(y|ies) [%-string%] of %object%"
        );

    }


    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        JsonElement json = jsonElementExpression.getSingle(e);
        if (json == null) return new Object[0];
        boolean emptyKeys = stringExpression == null;
        String keys = !emptyKeys ? stringExpression.getSingle(e) : null;
        LinkedList<String> wrappedKeys = extractKeys(keys, null);
        if (!isValues) {
            // Single
            if (wrappedKeys == null) return new Object[0];
            JsonElement jsonResult = getByKey(json, wrappedKeys);
            Object[] result;
            Object assigned = AdapterUtils.assignFrom(jsonResult);
            if (assigned == null) {
                assigned = jsonToObject(jsonResult);
                if (assigned == null) return new Object[0];
            }
            result = new Object[]{assigned};
            return result;
        } else {
            if (emptyKeys) {
                return !isEntries ? getNestedElements(json).toArray(new Object[0]) : new Object[]{json};
            } else {
                if (wrappedKeys == null) return new Object[0];
                JsonElement jsonResult = getByKey(json, wrappedKeys);
                if (jsonResult == null) return new Object[0];
                return !isEntries ? getNestedElements(jsonResult).toArray(new Object[0]) : new Object[]{jsonResult};
            }
        }
    }

    @Override
    public boolean isSingle() {
        return !isValues;
    }

    public static boolean isChangedLoopOf(@NotNull String s) {
        boolean result = false;
        if (loopEntries) {
            for (String loopEntriesString : loopEntriesStrings) {
                if (loopEntriesString.equals(s)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public @Nullable Iterator<?> iterator(@NotNull Event e) {
        if (!isEntries) return super.iterator(e);
        Object object = null;

        Iterator<?> oldIterator = super.iterator(e);
        if (oldIterator == null) return null;
        if (oldIterator.hasNext()) object = oldIterator.next();
        if (!(object instanceof JsonElement finalObject)) return null;

        return new Iterator<>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                if (finalObject.isJsonArray()) {
                    JsonArray array = finalObject.getAsJsonArray();
                    return index < array.size();
                } else if (finalObject.isJsonObject()) {
                    JsonObject json = finalObject.getAsJsonObject();
                    return index < json.keySet().size();
                }
                return false;
            }

            @Override
            public Object next() {
                if (finalObject.isJsonArray()) {
                    final WeakHashMap<String, Object> weak = new WeakHashMap<>();
                    JsonArray array = finalObject.getAsJsonArray();
                    weak.put(String.valueOf(index), jsonToObject(array.get(index)));
                    index++;
                    return weak;
                } else if (finalObject.isJsonObject()) {
                    JsonObject json = finalObject.getAsJsonObject();
                    Set<String> keys = json.keySet();
                    final WeakHashMap<String, Object> weak = new WeakHashMap<>();
                    String declaredKey = keys.toArray(new String[0])[index];
                    weak.put(declaredKey, jsonToObject(json.get(declaredKey)));
                    index++;
                    return weak;
                }
                return null;
            }
        };
    }

    @Override
    public boolean isLoopOf(@NotNull String s) {
        return super.isLoopOf(s);
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (!isEntries ? (isValues ? "values " : "value ") : "entries ")  + (stringExpression != null ? stringExpression.toString(e, debug)+" " : " ") + "of " + jsonElementExpression.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        isEntries = matchedPattern == 2;
        isValues = matchedPattern == 1 || isEntries;
        if (isEntries) {
            loopEntries = true;
            loopEntriesStrings.clear();
            loopEntriesStrings.addAll(List.of("element", "key"));
        }
        jsonElementExpression = LiteralUtils.defendExpression(exprs[1]);
        stringExpression = (Expression<String>) exprs[0];
        return LiteralUtils.canInitSafely(jsonElementExpression);
    }
}
