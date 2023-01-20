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
import com.google.gson.JsonElement;
import cz.coffee.adapters.JsonAdapter;
import cz.coffee.utils.json.JsonUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static cz.coffee.SkJson.FILE_JSON_MAP;
import static cz.coffee.SkJson.JSON_STORAGE;
import static cz.coffee.utils.json.JsonUtils.fromString2JsonElement;
import static cz.coffee.utils.json.JsonVariables.getVariable;
import static cz.coffee.utils.json.JsonVariables.setVariable;

@Name("Change value of JsonElements.")
@Description("You can handle a JsonElement or cached Jsons. and change their data as you want.")
@Examples({"on load:",
        "\tset {-json} to new json from text \"{'test': {'some': false}\"",
        "\tchange {-json} value \"test:some\" to item (iron sword named \"The &acolored &fSword\") and save it to json file \"plugins/sk-gson/test.json\"",
        "\tbroadcast {-json}",
        "",
        "\tchange cached json \"json5\" value \"test:some\" to item (iron sword named \"The &acolored &fSword\")",
        "\tbroadcast cached json \"json5\"",
})
@Since("2.0.0")

public class EffChange extends Effect {

    static {
        Skript.registerEffect(EffChange.class,
                "change [:cached json] %json/string%'s value %string% to %object%"
        );
    }

    private boolean isCached;
    private Expression<Object> exprInputSource;
    private Expression<String> keyExpr;
    private Expression<?> exprChangedData;
    private VariableString variableString;
    private boolean isLocal;

    @Override
    protected void execute(@NotNull Event e) {
        JsonUtils ju =  new JsonUtils();
        Object inputSource = exprInputSource.getSingle(e);
        String key = keyExpr.getSingle(e);
        Object changedData = exprChangedData.getSingle(e);
        JsonElement json, jsonInput;
        String variableName;

        if (changedData instanceof JsonElement) {
            json = (JsonElement) changedData;
        } else if (changedData instanceof String || changedData instanceof Boolean || changedData instanceof Number) {
            json = fromString2JsonElement(changedData.toString());
        } else {
            json = JsonAdapter.toJson(changedData);
        }

        assert json != null;
        assert key != null;
        assert inputSource != null;

        if (isCached) {
            if (JSON_STORAGE.containsKey(inputSource.toString()) && FILE_JSON_MAP.containsKey(inputSource.toString())) {
                jsonInput = JSON_STORAGE.get(inputSource.toString());
                JsonElement changedJson = ju.changeJson(jsonInput, key, json);
                JSON_STORAGE.remove(key);
                JSON_STORAGE.put(key, changedJson);
            }
        } else {
            variableName  = variableString.getDefaultVariableName().replaceAll("_", "");
            if (inputSource instanceof JsonElement) {
                Object v = getVariable(e, variableName, isLocal);
                if (v instanceof JsonElement) {
                    jsonInput = new Gson().toJsonTree(v);
                } else {
                    jsonInput = JsonAdapter.toJson(v);
                }
                JsonElement o = ju.changeJson(jsonInput, key, json);
                setVariable(variableName, o, e, isLocal);
            }
        }

    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "change " + (isCached ? "cached json " + exprInputSource.toString(e, debug) : variableString.toString(e, debug)) + "'s value " + keyExpr.toString(e, debug) + " to " + exprChangedData.toString(e, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        isCached = parseResult.hasTag("cached json");
        exprInputSource = LiteralUtils.defendExpression(exprs[0]);
        if (!isCached) {
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
        keyExpr = (Expression<String>) exprs[1];
        exprChangedData = LiteralUtils.defendExpression(exprs[2]);
        Expression<?> isItem = exprChangedData.getConvertedExpression(ItemStack.class);
        exprChangedData = Objects.requireNonNullElseGet(isItem, () -> LiteralUtils.defendExpression(exprs[2]));
        if (LiteralUtils.canInitSafely(exprInputSource)) {
            return LiteralUtils.canInitSafely(exprChangedData);
        }
        return false;
    }
}
