package cz.coffee.skript.changer;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.adapter.DefaultAdapters;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.SkJson.FILE_JSON_MAP;
import static cz.coffee.SkJson.JSON_STORAGE;
import static cz.coffee.utils.json.JsonUtils.*;
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
    private int mode;
    private Expression<Object> expressionWhat; // inputted Json.
    private Expression<?> expressionDelta; // object to set instead of current object.
    private Expression<String> expressionKeyValue; // key navigate to correct nested object
    private boolean isCached, isLocal;
    private VariableString variableString;

    static {
        Skript.registerEffect(EffCustomChanger.class,
                "set json value[s] %string% of [(:cached json)] %string/json% to %objects%",
                "add json value[s] %objects% to [(:cached json)] %string/json%",
                "remove json('s| )[value] %string% from [(:cached json)] %string/json%"
        );
    }

    @Override
    protected void execute(@NotNull Event event) {
        String keyValue = null;
        Object[] delta = null;
        Object what = expressionWhat.getSingle(event);
        if (mode == 1 || mode == 0) {
            keyValue = expressionKeyValue.getSingle(event);
        }
        if (mode != 0) delta = expressionDelta.getAll(event);
        process(what, delta, isCached, keyValue, mode, event, expressionDelta);
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
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {

        String PATTERN_CACHED = "cached json";
        isCached = parseResult.hasTag(PATTERN_CACHED);
        if (i == 0) {
            // Case SET
            mode = 1;
            expressionWhat = LiteralUtils.defendExpression(expressions[1]);
            expressionKeyValue = (Expression<String>) expressions[0];
            expressionDelta = LiteralUtils.defendExpression(expressions[2]);
        } else if (i == 1) {
            // Case ADD
            mode = 2;
            expressionWhat = LiteralUtils.defendExpression(expressions[1]);
            expressionDelta = LiteralUtils.defendExpression(expressions[0]);
        } else if (i == 2) {
            // Case REMOVE
            mode = 0;
            expressionKeyValue = (Expression<String>) expressions[0];
            expressionWhat = LiteralUtils.defendExpression(expressions[1]);
        }

        if (!isCached) {
            if (expressionWhat instanceof Variable<?> var) {
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


    private void process(Object what, Object[] delta, boolean isCached, String keyString, int mode, Event event, Expression<?> expression) {
        final String LOCAL_ = '_'+"";
        final String[] extractedKeys = (keyString == null ? new String[0] : extractKeys(keyString));
        JsonElement STORAGE;
        String key = what+"";

        if (mode == 0) {
            // DELETE
            if (isCached) {
                if (JSON_STORAGE.containsKey(key) && FILE_JSON_MAP.containsKey(key)) {
                    STORAGE = JSON_STORAGE.get(key);
                    deleteNested(extractedKeys,STORAGE);
                    JSON_STORAGE.remove(key);
                    JSON_STORAGE.put(key, STORAGE);
                }
            } else {
                String variableName = variableString.getDefaultVariableName().replaceFirst(LOCAL_, "");
                Object fromVar = getVariable(event, variableName, isLocal);
                if (fromVar instanceof JsonElement) {
                    STORAGE = (JsonElement) fromVar;
                    deleteNested(extractedKeys, STORAGE);
                    setVariable(variableName, STORAGE, event, isLocal);
                }
            }

        } else if (mode == 1 || mode == 2) {
            JsonElement inputJson = JsonNull.INSTANCE;
            if (delta != null) {
                for (Object object : delta) {
                    inputJson = DefaultAdapters.parse(object, expression, event);
                }
            }

            if (mode == 1) {
                // SET
                if (isCached) {
                    if (JSON_STORAGE.containsKey(key) && FILE_JSON_MAP.containsKey(key)) {
                        STORAGE = JSON_STORAGE.get(key);
                        changeJson(STORAGE, extractedKeys, inputJson);
                        JSON_STORAGE.remove(key);
                        JSON_STORAGE.put(key, STORAGE);
                    }
                } else {
                    String variableName = variableString.getDefaultVariableName().replaceFirst(LOCAL_, "");
                    Object fromVar = getVariable(event, variableName, isLocal);
                    if (fromVar instanceof JsonElement) {
                        STORAGE = (JsonElement) fromVar;
                        changeJson(STORAGE, extractedKeys, inputJson);
                        setVariable(variableName, STORAGE, event, isLocal);
                    }
                }
            } else {
                // ADD
                if (isCached) {
                    if (JSON_STORAGE.containsKey(key) && FILE_JSON_MAP.containsKey(key)) {
                        STORAGE = JSON_STORAGE.get(key);
                        if (STORAGE.isJsonObject())
                            STORAGE.getAsJsonObject().add(String.valueOf(STORAGE.getAsJsonObject().size()), inputJson);
                        else if (STORAGE.isJsonArray())
                            STORAGE.getAsJsonArray().add(inputJson);
                        JSON_STORAGE.remove(key);
                        JSON_STORAGE.put(key, STORAGE);
                    }
                } else {
                    String variableName = variableString.getDefaultVariableName().replaceFirst(LOCAL_, "");
                    Object fromVar = getVariable(event, variableName, isLocal);
                    if (fromVar instanceof JsonElement) {
                        STORAGE = (JsonElement) fromVar;
                        if (STORAGE.isJsonObject())
                            STORAGE.getAsJsonObject().add(String.valueOf(STORAGE.getAsJsonObject().size()), inputJson);
                        else if (STORAGE.isJsonArray())
                            STORAGE.getAsJsonArray().add(inputJson);

                        setVariable(variableName, STORAGE, event, isLocal);
                    }
                }
            }
        }
    }
}