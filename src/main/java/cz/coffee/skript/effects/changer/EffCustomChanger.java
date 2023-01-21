package cz.coffee.skript.effects.changer;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.*;
import cz.coffee.adapters.JsonAdapter;
import cz.coffee.utils.json.JsonUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;

import static cz.coffee.SkJson.FILE_JSON_MAP;
import static cz.coffee.SkJson.JSON_STORAGE;
import static cz.coffee.utils.json.JsonUtils.parseNestedPattern;
import static cz.coffee.utils.json.JsonVariables.getVariable;
import static cz.coffee.utils.json.JsonVariables.setVariable;


@Name("Custom changer of Json. Supported SET/ADD/REMOVE/RESET")
@Description({"Custom changer of Json.",
        "You can set/remove/reset or add data to/from any json what you want"
})
@Examples({
        "set {-json} to json from string \"{'list': [1,2,3,4]}\"",
        "set {-data::*} to 1,false,2,true",
        "set {-single} to location(0,10,10)",
        "",
        "set json values \"list\" of {_json} to {-data::*}",
        "set json value \"list\" of {_json} to {-single}",
        "set json value \"list[0]\" of {_json} to cached json \"your.input\"",
        "",
        "add json value {-single} to {-json}",
        "add json values {-data::*} to {-json}",
        "add json value {-single} to to cached json \"your.input\"",
        "",
        "remove json value \"list[1]\" from {-json}",
        "remove json value \"list\" from {-json}",
        "remove json value \"list[1]\" from cached json \"your.input\"",
})
@Since("2.5.1")


public class EffCustomChanger extends Effect {

    static {
        Skript.registerEffect(EffCustomChanger.class,
                "set json value[s] %string% of [(:cached json)] %string/json% to %objects%",
                "add json value[s] %objects% to [(:cached json)] %string/json%",
                "remove json('s| )[value] %string% from [(:cached json)] %string/json%"
        );
    }

    private int mode;
    private Expression<Object> expressionWhat; // inputted Json.
    private Expression<?> expressionDelta; // object to set instead of current object.
    private Expression<String> expressionKeyValue; // key navigate to correct nested object
    private boolean isCached, isLocal;
    private VariableString variableString;

    private boolean isClassicType(Object o) {
        return o instanceof String || o instanceof Number || o instanceof Boolean;
    }


    private void deleteNested(JsonElement outputJson, String from) {
        JsonElement element;
        String lastKey = null;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(outputJson);

        // prepare variable for nested keys.
        boolean isNested = false;
        String[] fromAsList;

        if (from.contains(":")) {
            isNested = true;
            fromAsList = from.split(":");
            lastKey = fromAsList[fromAsList.length - 1];
        }
        while ((element = elements.pollFirst()) != null) {
            if (element instanceof JsonObject) {
                JsonObject elementObject = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : elementObject.entrySet()) {
                    if (!entry.getKey().equals(isNested ? lastKey : from)) {
                        if (!(entry.getValue() instanceof JsonPrimitive || entry.getValue() == null))
                            elements.offerLast(entry.getValue());
                    } else {
                        elementObject.remove(entry.getKey());
                    }
                }
            } else if (element instanceof JsonArray) {
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement value = array.get(i);
                    if (!String.valueOf(i).equals(lastKey)) {
                        if (!(value instanceof JsonPrimitive || value == null || value instanceof JsonNull))
                            elements.offerLast(value);
                    } else {
                        array.remove(i);
                    }
                }
            }
        }
    }

    private void dataProcess(boolean isCached, Object key0, JsonElement inputJson, Event event, Gson gson, int mode, String keyValue) {
        String key = key0.toString();
        JsonElement outputJson = null;
        if (isCached) {
            if (JSON_STORAGE.containsKey(key) && FILE_JSON_MAP.containsKey(key)) {
                outputJson = JSON_STORAGE.get(key);
                if (outputJson != null || inputJson != null) {
                    JSON_STORAGE.remove(key);
                    if (mode == 2) {
                        if (outputJson instanceof JsonObject) {
                            outputJson.getAsJsonObject().add(String.valueOf(outputJson.getAsJsonObject().size()), inputJson);
                        } else if (outputJson instanceof JsonArray) {
                            outputJson.getAsJsonArray().add(inputJson);
                        }
                    } else if (mode == 1) {
                        assert outputJson != null;
                        outputJson = new JsonUtils().changeJson(outputJson, keyValue, inputJson);
                    } else if (mode == 0) {
                        deleteNested(outputJson, keyValue);
                    }
                    JSON_STORAGE.put(key, outputJson);
                }
            }
        } else {
            String variableName = variableString.getDefaultVariableName().replaceFirst("_", "");
            Object w = getVariable(event, variableName, isLocal);
            if (w instanceof JsonElement) {
                outputJson = (JsonElement) w;
            } else if (isClassicType(w)) {
                outputJson = gson.toJsonTree(w);
            }
            if (outputJson != null || inputJson != null) {
                if (mode == 2) {
                    if (outputJson instanceof JsonObject) {
                        outputJson.getAsJsonObject().add(String.valueOf(outputJson.getAsJsonObject().size()), inputJson);
                    } else if (outputJson instanceof JsonArray) {
                        outputJson.getAsJsonArray().add(inputJson);
                    }
                } else if (mode == 1) {
                    assert outputJson != null;
                    outputJson = new JsonUtils().changeJson(outputJson, keyValue, inputJson);
                } else if (mode == 0) {
                    deleteNested(outputJson, keyValue);
                }
                setVariable(variableName, outputJson, event, isLocal);
            }
        }
    }


    private void process(Object what, Object[] delta, boolean isCached, String keyValue, int mode, Event event) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().enableComplexMapKeySerialization().create();
        JsonElement inputJson;

        if (mode == 1 || mode == 0) {
            if (keyValue != null) {
                String[] changed = parseNestedPattern(keyValue, false);
                for (int i = 0; i < changed.length; i++) {
                    if (changed[i].endsWith(".list")) changed[i] = changed[i].replaceAll(".list", "");
                }
                keyValue = String.join(":", changed);
            }
        }

        if (mode == 0) {
            dataProcess(isCached, what, null, event, gson, 0, keyValue);
        } else {
            if (delta != null) {
                for (Object k : delta) {
                    if (k instanceof JsonElement) {
                        inputJson = (JsonElement) k;
                    } else if (isClassicType(k)) {
                        inputJson = gson.toJsonTree(k);
                    } else {
                        inputJson = JsonAdapter.toJson(k);
                    }
                    dataProcess(isCached, what, inputJson, event, gson, mode, keyValue);
                }
            }
        }
    }


    @Override
    protected void execute(@NotNull Event event) {
        String keyValue = null;
        Object[] delta = null;
        if (mode != 2) {
            keyValue = expressionKeyValue.getSingle(event);
        }
        if (mode != 0) delta = expressionDelta.getAll(event);
        Object what = expressionWhat.getSingle(event);
        process(what, delta, isCached, keyValue, mode, event);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        if (mode == 0) {
            return "remove json value " + expressionKeyValue.toString(event, b) + " from " + (isCached ? "cached json" + expressionWhat.toString(event, b) : variableString.toString(event, b));
        } else if (mode == 2) {
            return "add json value(s) " + expressionDelta.toString(event, b) + " to " + (isCached ? "cached json" + expressionWhat.toString(event, b) : variableString.toString(event, b));
        } else {
            return "set json value(s) " + expressionKeyValue.toString(event, b) + " of " + (isCached ? "cached json" + expressionWhat.toString(event, b) : variableString.toString(event, b)) + " to " + expressionDelta.toString(event, b);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, ParseResult parseResult) {

        String PATTERN_CACHED = "cached json";
        isCached = parseResult.hasTag(PATTERN_CACHED);

        if (i == 0) {
            // Case SET
            mode = 1;
            expressionWhat = LiteralUtils.defendExpression(expressions[1]);
            expressionKeyValue = (Expression<String>) expressions[0];
            expressionDelta = LiteralUtils.defendExpression(expressions[2]);
            Expression<?> isItem = expressionDelta.getConvertedExpression(ItemStack.class);
            expressionDelta = Objects.requireNonNullElseGet(isItem, () -> LiteralUtils.defendExpression(expressions[2]));
        } else if (i == 1) {
            // Case ADD
            mode = 2;
            expressionWhat = LiteralUtils.defendExpression(expressions[1]);
            expressionDelta = LiteralUtils.defendExpression(expressions[0]);
            Expression<?> isItem = expressionDelta.getConvertedExpression(ItemStack.class);
            expressionDelta = Objects.requireNonNullElseGet(isItem, () -> LiteralUtils.defendExpression(expressions[0]));
        } else if (i == 2) {
            // Case REMOVE
            mode = 0;
            expressionKeyValue = (Expression<String>) expressions[0];
            expressionWhat = LiteralUtils.defendExpression(expressions[1]);
        }

        if (!isCached) {
            if (expressionWhat instanceof Variable<?>) {
                Variable<?> var = (Variable<?>) expressionWhat;
                if (var.isSingle()) isLocal = var.isLocal();
                variableString = var.getName();
            } else {
                return false;
            }
        }

        if (mode == 1 || mode == 2) {
            if (LiteralUtils.canInitSafely(expressionWhat)) {
                return LiteralUtils.canInitSafely(expressionDelta);
            }
        } else if (mode == 0) {
            return LiteralUtils.canInitSafely(expressionWhat);
        }
        return false;
    }
}