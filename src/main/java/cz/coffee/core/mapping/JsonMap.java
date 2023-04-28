package cz.coffee.core.mapping;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.core.utils.AdapterUtils;
import cz.coffee.core.utils.JsonUtils;
import cz.coffee.core.utils.NumberUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ch.njol.skript.variables.Variables.getVariable;
import static cz.coffee.core.utils.NumberUtils.isIncrement;
import static cz.coffee.core.utils.NumberUtils.isNumber;

public abstract class JsonMap {
    private static final Gson GSON = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final String SEPARATOR = Variable.SEPARATOR;


    public static void toList(@NotNull String name, JsonElement input, boolean isLocal, Event event) {
        if (input.isJsonPrimitive()) {
            primitive(name, input.getAsJsonPrimitive(), isLocal, event);
        } else if (input.isJsonObject() || input.isJsonArray()) {
            if (input instanceof JsonArray list) {
                for (int index = 0; index < list.size(); index++) {
                    JsonElement element = list.get(index);
                    if (element.isJsonPrimitive()) {
                        primitive(name + (index + 1), element.getAsJsonPrimitive(), isLocal, event);
                    } else {
                        if (element.isJsonObject()) star(name + (index + 1), element, isLocal, event);
                        toList(name + (index + 1) + SEPARATOR, element, isLocal, event);
                    }
                }
            } else if (input instanceof JsonObject map) {
                map.keySet().stream().filter(Objects::nonNull).forEach(key -> {
                    JsonElement element = map.get(key);
                    if (element.isJsonPrimitive()) {
                        primitive(name + key, element.getAsJsonPrimitive(), isLocal, event);
                    } else {
                        if (element.isJsonObject()) star(name + key, element, isLocal, event);
                        toList(name + key + SEPARATOR, element, isLocal, event);
                    }
                });
            }
        }
    }
    static void primitive(String name, JsonPrimitive input, boolean isLocal, Event event) {
        if (input.isBoolean())
            Variables.setVariable(name, input.getAsBoolean(), event, isLocal);
        else if (input.isNumber())
            Variables.setVariable(name, input.getAsNumber(), event, isLocal);
        else if (input.isString())
            Variables.setVariable(name, input.getAsString(), event, isLocal);
    }

    static void star(String name, JsonElement input, boolean isLocal, Event event) {
        name = name + SEPARATOR + "*";
        Variables.setVariable(name, input, event, isLocal);
    }

    /**
     * @param name     Variable name {@link NotNull} {@link String}
     * @param isLocal  value contain if variable is local or nah
     * @param nullable can it be nullable?
     * @param event    {@link Event}
     * @return {@link JsonElement}
     */
    @SuppressWarnings("unchecked")
    public static JsonElement convert(@NotNull String name, boolean isLocal, boolean nullable, Event event) {

        Map<String, Object> variable = (Map<String, Object>) getVariable(name + "*", event, isLocal);
        if (variable == null) return nullable ? null : new JsonObject();
        List<String> checkKeys = variable.keySet().stream().filter(Objects::nonNull).filter(f -> !f.equals("*")).toList();

        if (checkKeys.stream().allMatch(NumberUtils::isNumber)) {
            if (isIncrement(checkKeys.toArray())) {
                JsonArray jsonStructure = new JsonArray();
                checkKeys.forEach(key -> {
                    Object rawValue = subNode(name + key, isLocal, event);
                    JsonElement valueData = GSON.toJsonTree(rawValue);
                    if (valueData instanceof JsonPrimitive) {
                        JsonElement primitive = JsonUtils.convert(valueData);
                        if (isNumber(primitive)) {
                            jsonStructure.add(JsonUtils.convert(valueData));
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
                checkKeys.forEach(key -> {
                    JsonElement data = GSON.toJsonTree(subNode(name + key, isLocal, event));
                    if (data instanceof JsonPrimitive primitive) {
                        jsonStructure.add(key, primitive);
                    } else {
                        jsonStructure.add(key, data);
                    }
                });
                return jsonStructure;
            }
        } else {
            JsonObject jsonStructure = new JsonObject();
            checkKeys.forEach(key -> {
                JsonElement data = GSON.toJsonTree(subNode(name + key, isLocal, event));
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

     static Object subNode(String name, boolean isLocal, Event event) {
        Object variable = getVariable(name, event, isLocal);
        if (variable == null) {
            variable = convert(name + SEPARATOR, isLocal, false, event);
        } else if (variable == Boolean.TRUE) {
            Object subVariable = convert(name + SEPARATOR, isLocal, true, event);
            if (subVariable != null) {
                variable = subVariable;
            }
        }

        if (!(variable instanceof String || variable instanceof Number || variable instanceof Boolean || variable instanceof JsonElement || variable instanceof Map || variable instanceof List)) {
            if (variable != null) variable = AdapterUtils.parseItem(variable, variable.getClass());
        }
        return variable;
    }


}
