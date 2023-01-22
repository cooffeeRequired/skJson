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

package cz.coffee.utils.json;

import ch.njol.skript.lang.Variable;
import com.google.gson.*;
import cz.coffee.utils.SimpleUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static cz.coffee.utils.SimpleUtil.*;
import static cz.coffee.utils.json.JsonUtils.fromPrimitive2Object;
import static cz.coffee.utils.json.JsonVariables.*;

@SuppressWarnings("unused")
public class JsonMapping {
    private static final String SEPARATOR = Variable.SEPARATOR;

    /**
     * @param name     Variable name {@link NotNull} {@link String}
     * @param isLocal  value contain if variable is local or nah
     * @param nullable can it be nullable?
     * @param event    {@link Event}
     * @return {@link JsonElement}
     */
    @SuppressWarnings("unchecked")
    private static JsonElement jsonMainTree(@NotNull String name, boolean isLocal, boolean nullable, Event event) {
        Gson gson = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization().disableHtmlEscaping().create();
        Map<String, Object> variable = (Map<String, Object>) JsonVariables.getVariable(event, name + "*", isLocal);
        if (variable == null) return nullable ? null : new JsonObject();
        Stream<String> keys = variable.keySet().stream().filter(Objects::nonNull);
        if (variable.keySet().stream().filter(Objects::nonNull).allMatch(SimpleUtil::isNumeric)) {
            List<String> checkKeys = new ArrayList<>();
            variable.keySet().stream().filter(Objects::nonNull).forEach(checkKeys::add);
            if (isIncrementNumber(checkKeys.toArray())) {
                JsonArray jsonStructure = new JsonArray();
                keys.forEach(key -> {
                    JsonElement valueData = gson.toJsonTree(jsonListSubTree(name + key, isLocal, event));
                    if (valueData instanceof JsonPrimitive) {
                        JsonPrimitive primitive = valueData.getAsJsonPrimitive();
                        if (isNumeric(fromPrimitive2Object(primitive))) {
                            JsonElement jsonPrimitive = JsonParser.parseString(fromPrimitive2Object(primitive).toString());
                            jsonStructure.add(jsonPrimitive);
                        } else {
                            jsonStructure.add(primitive);
                        }
                    } else {
                        jsonStructure.add(valueData);
                    }
                });
                return jsonStructure;
            } else {
                JsonObject jsonStructure = new JsonObject();
                keys.forEach(key -> {
                    JsonElement data = gson.toJsonTree(jsonListSubTree(name + key, isLocal, event));
                    if (data instanceof JsonPrimitive) {
                        JsonPrimitive primitive = (JsonPrimitive) data;
                        jsonStructure.add(key, primitive);
                    } else {
                        jsonStructure.add(key, data);
                    }
                });
                return jsonStructure;
            }
        } else {
            JsonObject jsonStructure = new JsonObject();
            keys.forEach(key -> {
                JsonElement data = gson.toJsonTree(jsonListSubTree(name + key, isLocal, event));
                if (data instanceof JsonPrimitive) {
                    JsonPrimitive primitive = data.getAsJsonPrimitive();
                    jsonStructure.add(key, primitive);
                } else {
                    jsonStructure.add(key, data);
                }
            });
            return jsonStructure;
        }
    }

    /**
     * This function is entry point for map {@link JsonElement} to Variable
     *
     * @param name    Variable name {@link NotNull} {@link String}
     * @param isLocal value contain if variable is local or nah
     * @param event   {@link Event}
     * @return {@link JsonElement}
     */
    public static JsonElement jsonToList(@NotNull String name, boolean isLocal, Event event) {
        return jsonMainTree(name, isLocal, false, event);
    }

    /**
     * This function is processing function for mapping
     *
     * @param name    Variable name {@link NotNull} {@link String}
     * @param isLocal value contain if variable is local or nah
     * @param event   {@link Event}
     */
    public static void jsonToList(@NotNull String name, JsonElement json, boolean isLocal, Event event) {
        JsonElement next;
        Deque<JsonElement> elements = new ArrayDeque<>();
        if (json != null) elements.add(json);

        while ((next = elements.pollFirst()) != null) {
            if (next instanceof JsonObject) {
                extractNestedObjects(name, next.getAsJsonObject(), isLocal, event);
            } else if (next instanceof JsonArray) {
                extractNestedObjects(name, next.getAsJsonArray(), isLocal, event);
            } else if (next instanceof JsonPrimitive) {
                setPrimitiveType(name, next.getAsJsonPrimitive(), event, isLocal);
            } else {
                setVariable(name, next, event, isLocal);
            }
        }
    }

    /**
     * This function do extract subTree from MainTree
     *
     * @param name    Variable name {@link NotNull} {@link String}
     * @param isLocal value contain if variable is local or nah
     * @param event   {@link Event}
     * @return {@link JsonElement}
     */
    private static Object jsonListSubTree(String name, boolean isLocal, Event event) {
        Object variable = getVariable(event, name, isLocal);
        if (variable == null) {
            variable = jsonMainTree(name + SEPARATOR, isLocal, false, event);
        } else if (variable == Boolean.TRUE) {
            Object subVariable = jsonMainTree(name + SEPARATOR, isLocal, true, event);
            if (subVariable != null) {
                variable = subVariable;
            }
        }

        if (!(variable instanceof String || variable instanceof Integer || variable instanceof Double || variable instanceof Boolean || variable instanceof JsonElement || variable instanceof Map || variable instanceof List)) {
            variable = gsonAdapter.toJson(variable);
        }
        return variable;
    }

    /**
     * This function extraction data from {@link JsonObject} or {@link JsonArray}
     *
     * @param variableName Variable name {@link NotNull} {@link String}
     * @param isLocal      value contain if variable is local or nah
     * @param event        {@link Event}
     */
    private static void extractNestedObjects(@NotNull String variableName, @NotNull JsonElement input, boolean isLocal, Event event) {
        if (input instanceof JsonObject) {
            input.getAsJsonObject().keySet().forEach(key -> {
                if (!(key == null))
                    jsonToList(variableName + SEPARATOR + key, input.getAsJsonObject().get(key), isLocal, event);
            });
        } else if (input instanceof JsonArray) {
            for (int index = 0; input.getAsJsonArray().size() > index; index++)
                jsonToList(variableName + SEPARATOR + (index + 1), input.getAsJsonArray().get(index), isLocal, event);
        }
    }
}
