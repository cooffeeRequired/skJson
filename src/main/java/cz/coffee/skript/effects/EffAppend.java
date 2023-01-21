/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.adapters.JsonAdapter;
import cz.coffee.utils.json.JsonFilesHandler;
import cz.coffee.utils.json.JsonUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import static cz.coffee.SkJson.*;
import static cz.coffee.utils.json.JsonUtils.fromString2JsonElement;
import static cz.coffee.utils.json.JsonVariables.getVariable;
import static cz.coffee.utils.json.JsonVariables.setVariable;

@Name("Append jsonelement/cached Json/Json file")
@Description({"You can append the jsonelement or the cached json or the json file"})
@Examples({"command sk-example:",
        "\ttrigger:",
        "\t\tappend player's location with key \"location\" to cached json \"your\"",
        "\t\tsend cached json \"your\" with pretty print",
        "",
        "\t\tset {_json} to json from player's world",
        "\t\tappend player's location with key \"location\" as nested object \"player:data[0]\" to {_json}",
        "\t\tsend {_json} with pretty print",
        "",
        "\t\tset {_fileJson} to json from file \"sk-gson\\test.json\"",
        "\t\tappend player's location to file \"sk-gson\\test.json\"",
        "\t\tset {_fileJson} to json from file \"sk-gson\\test.json\"",
        "\t\tsend {_fileJson} with pretty print",
})
@Since("2.5.0")

public class EffAppend extends Effect {

    static {
        Skript.registerEffect(EffAppend.class,
                "append %object% [:with key %-string%] [:as nested object %-string%] to json file %string%",
                "append %object% [:with key %-string%] [:as nested object %-string%] to (:cached json) %string%",
                "append %object% [:with key %-string%] [:as nested object %-string%] to %json%"
        );
    }

    private boolean isCached, isFile, hasKey, isNested;
    private Expression<Object> exprInputSource;
    private Expression<String> keyExpr, nestedExpr;
    private Expression<?> dataToAppendExpr;
    private VariableString variableString;
    private boolean isLocal;

    @Override
    protected void execute(@NotNull Event e) {
        Object inputSource = exprInputSource.getSingle(e); // last expr.
        Object dataToAppend = dataToAppendExpr.getSingle(e); // first
        String key = null; // key expr (with key)
        String nested = null; //nested expr (as nested)

        if (hasKey)
            key = keyExpr.getSingle(e);
        if (isNested)
            nested = nestedExpr.getSingle(e);

        JsonElement json, jsonInput;
        String variableName;

        if (dataToAppend instanceof JsonElement) {
            json = (JsonElement) dataToAppend;
        } else if (dataToAppend instanceof String || dataToAppend instanceof Boolean || dataToAppend instanceof Number) {
            json = fromString2JsonElement(dataToAppend.toString());
        } else {
            json = JsonAdapter.toJson(dataToAppend);
        }

        assert json != null;
        assert inputSource != null;

        if (isCached) {
            if (JSON_STORAGE.containsKey(inputSource.toString()) && FILE_JSON_MAP.containsKey(inputSource.toString())) {
                jsonInput = JSON_STORAGE.get(inputSource.toString());
                JSON_STORAGE.remove(inputSource.toString());
                JSON_STORAGE.put(inputSource.toString(), parseNotes(jsonInput, key, nested, json));
            }
        } else if (isFile) {
            JsonFilesHandler jfh = new JsonFilesHandler(false);
            File file = new File(inputSource.toString());
            jsonInput = jfh.readFile(file.toString());
            jfh.writeFile(file, parseNotes(jsonInput, key, nested, json), false);

        } else {
            variableName  = variableString.getDefaultVariableName().replaceAll("_", "");
            Object v = getVariable(e, variableName, isLocal);
            if (v instanceof JsonElement) {
                jsonInput = new Gson().toJsonTree(v);
            } else {
                jsonInput = JsonAdapter.toJson(v);
            }
            JsonElement o = parseNotes(jsonInput, key, nested, json);
            setVariable(variableName, o, e, isLocal);
        }
    }

    // processing the nested and keys.

    private JsonElement parseNotes(JsonElement inputJson, String Key, String Nested, JsonElement json) {
        JsonUtils ju = new JsonUtils();
        String key;
        if (inputJson != null) {
            boolean debug = PROJECT_DEBUG;
            if (inputJson instanceof JsonObject) {
                JsonObject object = (JsonObject) inputJson;
                key = Key != null ? Key : String.valueOf(object.size());
                if (Nested == null) {
                    object.add(key, json);
                    return object;
                } else {
                    inputJson = ju.appendJson(inputJson, json, key, Nested, debug);
                    return inputJson;
                }
            } else if (inputJson instanceof JsonArray){
                JsonArray array = (JsonArray) inputJson;
                key = Key != null ? Key : String.valueOf(array.size());
                if (Nested == null) {
                    array.add(json);
                    return array;
                } else {
                    inputJson = ju.appendJson(inputJson, json, key, Nested, debug);
                    return inputJson;
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "append " + dataToAppendExpr.toString(e, debug) + (hasKey ? " with key " + keyExpr.toString(e, debug) : "") + (isNested ? " as nested object " + nestedExpr.toString(e, debug) : " ") + "to" + (isFile ? " json file " + exprInputSource.toString(e, debug) : (isCached ? " cached json " + exprInputSource.toString(e, debug) : " "+variableString.toString(e, debug)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        isCached = parseResult.hasTag("cached json");
        isFile = matchedPattern == 0;
        dataToAppendExpr = LiteralUtils.defendExpression(exprs[0]);
        keyExpr = (Expression<String>) exprs[1];
        nestedExpr = (Expression<String>) exprs[2];
        exprInputSource = LiteralUtils.defendExpression(exprs[3]);
        isNested = parseResult.hasTag("as nested object");
        hasKey = parseResult.hasTag("with key");
        dataToAppendExpr = LiteralUtils.defendExpression(exprs[0]);
        Expression<?> isItem = dataToAppendExpr.getConvertedExpression(ItemStack.class);
        dataToAppendExpr = Objects.requireNonNullElseGet(isItem, () -> LiteralUtils.defendExpression(exprs[0]));
        keyExpr = (Expression<String>) exprs[1];
        nestedExpr = (Expression<String>) exprs[2];
        exprInputSource = LiteralUtils.defendExpression(exprs[3]);


        if (!isCached || !isFile) {
            if (exprInputSource instanceof Variable<?>) {
                Variable<?> var = (Variable<?>) exprInputSource;
                if (var.isSingle()) {
                    isLocal = var.isLocal();
                    variableString = var.getName();
                } else {
                    return false;
                }
            }
        }
        if (LiteralUtils.canInitSafely(dataToAppendExpr)) {
            return LiteralUtils.canInitSafely(exprInputSource);
        }
        return false;
    }
}
