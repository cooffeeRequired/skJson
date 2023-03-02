package cz.coffee.skript.changer;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.cache.Cache;
import cz.coffee.core.cache.CachePackage;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static cz.coffee.core.utils.JsonUtils.*;
import static cz.coffee.core.utils.JsonVariables.getVariable;
import static cz.coffee.core.utils.JsonVariables.setVariable;

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
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */

public class CustomChanger extends Effect {
    static {
        Skript.registerEffect(CustomChanger.class,
                "set json value[s] %string% of [(:cached json)] %string/json% to %objects%",
                "add json value[s] %objects% to [(:cached json)] %string/json%",
                "remove json [[list]( |-)[:value]] %string% from [(:cached json)] %string/json%",
                "remove [json] (:index|:object) %object% [from nested object %-string%] of [(:cached json)] %string/json%"
        );
    }

    private int mode;
    private Expression<Object> expressionWhat; // inputted Json.
    private Expression<?> expressionDelta; // object to set instead of current object.
    private Expression<String> expressionKeyValue; // key navigate to correct nested object
    private VariableString variableString;

    private boolean isCached, isLocal, value, isIndex, isObject, parsedCase;

    @Override
    protected void execute(@NotNull Event event) {
        String keyValue = null;
        Object[] delta = null;
        Object what = expressionWhat.getSingle(event);
        if (mode == 1 || mode == 0) {
            keyValue = expressionKeyValue.getSingle(event);
            if (parsedCase) {
                delta = expressionDelta.getAll(event);
            }
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
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull SkriptParser.ParseResult parseResult) {

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
        } else if (i == 2 || i == 3) {
            // Case REMOVE
            value = parseResult.hasTag("value");
            isIndex = parseResult.hasTag("index");
            isObject = parseResult.hasTag("object");
            mode = 0;
            if (i == 3) {
                parsedCase = true;
                expressionDelta = LiteralUtils.defendExpression(expressions[0]);
                if (LiteralUtils.canInitSafely(expressionDelta)) {
                    expressionKeyValue = (Expression<String>) expressions[1];
                    expressionWhat = LiteralUtils.defendExpression(expressions[2]);
                }
            } else {
                expressionKeyValue = (Expression<String>) expressions[0];
                expressionWhat = LiteralUtils.defendExpression(expressions[1]);
            }
        }

        // Cached think
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
            if (parsedCase) {
                if (LiteralUtils.canInitSafely(expressionDelta)) {
                    return LiteralUtils.canInitSafely(expressionWhat);
                }
            } else {
                return LiteralUtils.canInitSafely(expressionWhat);
            }
        }
        return false;
    }


    private void process(Object what, Object[] delta, boolean isCached, String keyString, int mode, Event event, Expression<?> expression) {
        final String LOCAL_ = '_' + "";
        final String[] extractedKeys = (keyString == null ? new String[0] : extractKeys(keyString));
        JsonElement STORAGE;
        String key = what + "";

        if (mode == 0) {
            // DELETE
            if (isCached) {
                if (Cache.contains(key)) {
                    CachePackage<JsonElement, File> cachePackage = Cache.getPackage(key);
                    if (cachePackage == null) return;
                    STORAGE = cachePackage.getJson();
                    if (parsedCase) {
                        if (isObject) {
                            deleteNested(extractedKeys, STORAGE, false, event, true, delta);
                        } else if (isIndex) {
                            deleteNested(extractedKeys, STORAGE, false, event, false, delta);
                        }
                    } else {
                        deleteNested(extractedKeys, STORAGE, false, event, false);
                    }
                    Cache.remove(key);
                    Cache.addTo(key, STORAGE, cachePackage.getFile());
                }
            } else {
                String variableName = variableString.getDefaultVariableName().replaceFirst(LOCAL_, "");
                Object fromVar = getVariable(event, variableName, isLocal);
                if (fromVar instanceof JsonElement) {
                    STORAGE = (JsonElement) fromVar;
                    if (parsedCase) {
                        if (isObject) {
                            deleteNested(extractedKeys, STORAGE, false, event, true, delta);
                        } else if (isIndex) {
                            deleteNested(extractedKeys, STORAGE, false, event, false, delta);
                        }
                    } else {
                        deleteNested(extractedKeys, STORAGE, false, event, false);
                    }
                    setVariable(variableName, STORAGE, event, isLocal);
                }
            }

        } else if (mode == 1 || mode == 2) {
            JsonElement inputJson = JsonNull.INSTANCE;
            if (delta != null) {
                for (Object object : delta) {
                    inputJson = DefaultAdapter.parse(object, expression, event);
                }
            }

            if (mode == 1) {
                // SET
                if (isCached) {
                    if (Cache.contains(key)) {
                        CachePackage<JsonElement, File> cachePackage = Cache.getPackage(key);
                        if (cachePackage == null) return;
                        STORAGE = cachePackage.getJson();
                        changeJson(STORAGE, extractedKeys, inputJson);
                        Cache.remove(key);
                        Cache.addTo(key, STORAGE, cachePackage.getFile());
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
                    if (Cache.contains(key)) {
                        CachePackage<JsonElement, File> cachePackage = Cache.getPackage(key);
                        if (cachePackage == null) return;
                        STORAGE = cachePackage.getJson();
                        if (STORAGE.isJsonObject())
                            STORAGE.getAsJsonObject().add(String.valueOf(STORAGE.getAsJsonObject().size()), inputJson);
                        else if (STORAGE.isJsonArray())
                            STORAGE.getAsJsonArray().add(inputJson);
                        Cache.remove(key);
                        Cache.addTo(key, STORAGE, cachePackage.getFile());
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