package cz.coffee.core.mapping;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.core.utils.NumberUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static ch.njol.skript.variables.Variables.getVariable;
import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.Util.jsonToObject;

public abstract class JsonMap {
    private static final Gson GSON = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final JsonObject JSON_OBJECT = new JsonObject();
    private static final JsonArray JSON_ARRAY = new JsonArray();
    private static final String SEPARATOR = Variable.SEPARATOR;
    public static class Skript {
        public static void toList(@NotNull String name, JsonElement input, boolean isLocal, Event event) {
            JsonElement current;
            Queue<JsonElement> elements = new ConcurrentLinkedQueue<>();
            if (input != null) elements.add(input);

            while ((current = elements.poll()) != null) {
                if (current instanceof JsonPrimitive primitive) {
                    primitive(name, primitive, isLocal, event);
                } else if (current instanceof JsonObject object){
                    nested(name, object, isLocal, event);
                } else if (current instanceof JsonArray array) {
                    nested(name, array, isLocal, event);
                }
            }
        }

        private static void primitive(String name, JsonPrimitive input, boolean isLocal, Event event) {
            if (input.isBoolean()) Variables.setVariable(name, input.getAsBoolean(), event, isLocal);
            else if (input.isNumber()) Variables.setVariable(name, input.getAsNumber(), event, isLocal);
            else if  (input.isString()) Variables.setVariable(name, input.getAsString(), event, isLocal);
        }

        private static void nested(@NotNull String name, @NotNull JsonElement input, boolean isLocal, Event event) {
            if (input instanceof JsonObject object) {
                //main(name + "*", object, isLocal, event);
                object.keySet().forEach(key -> {
                    if (key != null)
                        toList(name + key, object.get(key), isLocal, event);
                });
            } else if (input instanceof JsonArray array) {
                //main(name  + "*", array, isLocal, event);
                for (int index = 0; array.size() > index; index++)
                    toList(name + (index+1), array.get(index), isLocal, event);
            }
        }

        private static void main(@NotNull String name, @NotNull JsonElement input, boolean isLocal, Event e) {
            final Object o = GSON.fromJson(input.toString(), Object.class);
            Variables.setVariable(name, o, e, isLocal);
        }

    }
    public static class Json {

        @SuppressWarnings("unchecked")
        public static JsonElement convert(@NotNull String name, boolean isLocal, boolean nullable, Event event) {
            final Object varObject = getVariable(name + "*", event, isLocal);
            Map<String, Object> variable = (Map<String, Object>) varObject;
            if (variable == null) return nullable ? null : JSON_OBJECT;
            Stream<String> keys = variable.keySet().stream().filter(Objects::nonNull);

            if (variable.keySet().stream().filter(Objects::nonNull).allMatch(NumberUtils::isNumber)) {
                final List<String> checkKeys = new ArrayList<>();
                variable.keySet().stream().filter(Objects::nonNull).forEach(checkKeys::add);
                if (NumberUtils.isIncrement(checkKeys.toArray())) {
                    final JsonArray jsonStructure = JSON_ARRAY;
                    keys.forEach(key -> {
                        Object rawValue = subList(name + key, isLocal, event);
                        JsonElement valueData = GSON.toJsonTree(rawValue);
                        if (valueData instanceof JsonPrimitive primitive) {
                            if (NumberUtils.isNumber(jsonToObject(primitive))) {
                                JsonElement jsonPrimitive = JsonParser.parseString(jsonToObject(primitive).toString());
                                jsonStructure.add(jsonPrimitive);
                            } else {
                                jsonStructure.add(primitive);
                            }
                        } else {
                            jsonStructure.add(valueData);
                        }
                    });
                    return jsonStructure;
                }
            } else {
                final JsonObject jsonStructure = JSON_OBJECT;
                keys.forEach(key -> {
                    JsonElement data = GSON.toJsonTree(subList(name + key, isLocal, event));
                    if (data instanceof JsonPrimitive primitive) {
                        jsonStructure.add(key, primitive);
                    } else {
                        jsonStructure.add(key, data);
                    }
                });
                return jsonStructure;
            }
            return null;
        }

        private static Object subList(String name, boolean isLocal, Event event) {
            Object variable = getVariable(name, event, isLocal);
            if (variable == null) convert(name + SEPARATOR, isLocal, false, event);
            else if (variable == Boolean.TRUE) {
                Object subVar = convert(name + SEPARATOR, isLocal, true, event);
                if (subVar != null) variable = subVar;
            }
            if (!(variable instanceof String || variable instanceof Number || variable instanceof Boolean || variable instanceof JsonElement || variable instanceof Map || variable instanceof List)) {
                if (variable != null) variable = parseItem(variable, variable.getClass());
            }
            return variable;
        }
    }
}
