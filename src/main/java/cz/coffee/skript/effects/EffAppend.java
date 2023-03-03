/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
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
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.cache.Cache;
import cz.coffee.core.cache.CachePackage;
import cz.coffee.core.utils.JsonFilesHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

import static cz.coffee.adapter.DefaultAdapter.assignTo;
import static cz.coffee.adapter.DefaultAdapter.isClassicType;
import static cz.coffee.core.utils.JsonUtils.appendJson;
import static cz.coffee.core.utils.JsonVariables.getVariable;
import static cz.coffee.core.utils.JsonVariables.setVariable;


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
        "\t\tset {_fileJson} to json from file \"skJson\\test.json\"",
        "\t\tappend player's location to file \"skJson\\test.json\"",
        "\t\tset {_fileJson} to json from file \"skJson\\test.json\"",
        "\t\tsend {_fileJson} with pretty print",
})
@Since("2.5.0")

public class EffAppend extends Effect {

    static {
        Skript.registerEffect(EffAppend.class,
                "append %object/json% [:with key %-string%] [:as nested object %-string%] to json file %string%",
                "append %object% [:with key %-string%] [:as nested object %-string%] to [:hot] (:cached json) %string% [(1:of) %-object%]",
                "append %object% [:with key %-string%] [:as nested object %-string%] to %json%"
        );
    }

    private boolean isCached, isFile, hasKey, isNested, forUUID;
    private Expression<Object> exprInputSource, exprUUID;
    private Expression<String> keyExpr, nestedExpr;
    private Expression<?> dataToAppendExpr;
    private VariableString variableString;
    private boolean isLocal, hot;

    @Override
    protected void execute(@NotNull Event e) {
        Object inputSource = exprInputSource.getSingle(e); // last expr.
        Object dataToAppend = dataToAppendExpr.getSingle(e);

        String key = null; // key expr (with key)
        String nested = null; //nested expr (as nested)


        if (hasKey)
            key = keyExpr.getSingle(e);
        if (isNested)
            nested = nestedExpr.getSingle(e);

        JsonElement json, jsonInput;
        String variableName;

        json = DefaultAdapter.parse(dataToAppend, dataToAppendExpr, e);
        assert json != null;
        assert inputSource != null;

        if (isCached) {
            if (hot) {
                UUID uuid0;

                if (Cache.hotContains(inputSource.toString())) {
                    CachePackage.HotLink cachePackage = Cache.getHotPackage(inputSource.toString());
                    if (cachePackage == null) return;
                    if (forUUID)
                        uuid0 = UUID.fromString(exprUUID.getSingle(e).toString());
                    else
                        uuid0 = cachePackage.getUuid();
                    jsonInput = cachePackage.getJson();
                    Cache.addToHot(inputSource.toString(),appendJson(jsonInput, json, key, nested), uuid0);
                }

            } else {
                if (Cache.contains(inputSource.toString())) {
                    CachePackage<JsonElement, File> cachePackage = Cache.getPackage(inputSource.toString());
                    jsonInput = cachePackage.getJson();
                    Cache.remove(inputSource.toString());
                    Cache.addTo(inputSource.toString(),appendJson(jsonInput, json, key, nested), cachePackage.getFile());
                }
            }
        } else if (isFile) {
            JsonFilesHandler jfh = new JsonFilesHandler(false);
            File file = new File(inputSource.toString());
            jsonInput = jfh.readFile(file.toString());
            jfh.writeFile(file, appendJson(jsonInput, json, key, nested), false);

        } else {
            variableName = variableString.getDefaultVariableName().replaceAll("_", "");
            Object v = getVariable(e, variableName, isLocal);
            if (v instanceof JsonElement) {
                jsonInput = (JsonElement) v;
            } else if (isClassicType(v)) {
                jsonInput = new Gson().toJsonTree(v);
            } else {
                jsonInput = assignTo(v);
            }

            if (jsonInput == null) return;
            setVariable(variableName, appendJson(jsonInput, json, key, nested), e, isLocal);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "append " + dataToAppendExpr.toString(e, debug) + (hasKey ? " with key " + keyExpr.toString(e, debug) : "") + (isNested ? " as nested object " + nestedExpr.toString(e, debug) : " ") + "to" + (isFile ? " json file " + exprInputSource.toString(e, debug) : (isCached ? " cached json " + exprInputSource.toString(e, debug) : " " + variableString.toString(e, debug)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        isFile = matchedPattern == 0;
        hot = parseResult.hasTag("hot");

        isCached = parseResult.hasTag("cached json");
        isNested = parseResult.hasTag(("as nested object"));
        hasKey = parseResult.hasTag(("with key"));
        dataToAppendExpr = LiteralUtils.defendExpression(exprs[0]);

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
        if (hot) {
            forUUID = parseResult.mark == 1;
            if (forUUID) {
                exprUUID = LiteralUtils.defendExpression(exprs[4]);
                if (LiteralUtils.canInitSafely(dataToAppendExpr)) {
                    if (LiteralUtils.canInitSafely(exprUUID)){
                        return LiteralUtils.canInitSafely(exprInputSource);
                    }
                }
            }
        }
        if (LiteralUtils.canInitSafely(dataToAppendExpr)) {
            return LiteralUtils.canInitSafely(exprInputSource);
        }
        return false;
    }
}
