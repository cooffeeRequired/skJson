package cz.coffeerequired.support;

import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.SerializedJsonUtils;
import org.bukkit.event.Event;

import javax.validation.constraints.NotNull;
import java.util.*;

import static ch.njol.skript.lang.Variable.SEPARATOR;

public abstract class SkriptUtils {
    @SuppressWarnings("unchecked")
    public static TreeMap<String, Object> getListVariable(String name, Event event, boolean isLocal) {
        return (TreeMap<String, Object>) Variables.getVariable(name, event, isLocal);
    }


    public static JsonElement convertSkriptVariableToJson(Map<?, ?> inputMap) {
        Map<String, Object> cleanMap = cleanupMap(inputMap);
        return new Gson().toJsonTree(cleanMap);
    }
    private static Map<String, Object> cleanupMap(Map<?, ?> map) {
        Map<String, Object> cleanMap = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().toString() : null;
            Object value = entry.getValue();

            if (key == null) continue;

            if (value instanceof Map<?, ?> valueMap) {
                if (isNumericMap(valueMap)) {
                    cleanMap.put(key, convertMapToList(valueMap));
                } else {
                    cleanMap.put(key, cleanupMap(valueMap));
                }
            } else {
                cleanMap.put(key, value);
            }
        }

        return cleanMap;
    }

    private static boolean isNumericMap(Map<?, ?> map) {
        for (Object key : map.keySet()) {
            if (key == null) continue;
            try {
                Integer.parseInt(key.toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static ArrayList<Object> convertMapToList(Map<?, ?> map) {
        ArrayList<Object> list = new ArrayList<>();
        int maxIndex = -1;

        // Search the bigger index
        for (Object key : map.keySet()) {
            if (key != null) {
                try {
                    int index = Integer.parseInt(key.toString());
                    maxIndex = Math.max(maxIndex, index);
                } catch (NumberFormatException ignored) {}
            }
        }

        // list Initialize new size of a list
        for (int i = 0; i <= maxIndex; i++) {
            list.add(null);
        }

        // Fill that list with values
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                try {
                    int index = Integer.parseInt(entry.getKey().toString());
                    Object value = GsonParser.toJson(entry.getValue());
                    if (value instanceof Map) {
                        list.set(index, cleanupMap((Map<?, ?>) value));
                    } else {
                        list.set(index, value);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // Delete all nulls or empties
        while (!list.isEmpty() && list.getFirst() == null) list.removeFirst();
        return list;
    }

    public static void convertJsonToSkriptVariable(@NotNull String variableName, @NotNull JsonElement json, @NotNull Event event, boolean isLocal) {
        if (json instanceof JsonPrimitive primitive) {
            savePrimitiveToVariable(variableName, primitive, event, isLocal);
        } else if (json instanceof JsonObject object) {
            var parsed = GsonParser.fromJson(object);

            SkJson.logger().info(String.format("IN OBJECT &a: %s", parsed));

            for (String key : object.keySet()) {
                JsonElement value = object.get(key);
                String pathKey = variableName + key + SEPARATOR;
                if (cannotBeParsed(value)) {
                    convertJsonToSkriptVariable(pathKey, value, event, isLocal);
                } else {
                    saveParsedToVariable(pathKey, parsed, event, isLocal);
                }
            }
        } else if (json instanceof JsonArray array) {
            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                String newName = variableName + (i + 1) + SEPARATOR;
                Object parsed = GsonParser.fromJson(element);
                if (cannotBeParsed(element)) {
                    convertJsonToSkriptVariable(newName, element, event, isLocal);
                } else {
                    saveParsedToVariable(newName, parsed, event, isLocal);
                }
            }
        }
    }

    private static void savePrimitiveToVariable(String variableName, Object value, Event event, boolean isLocal) {
        if (variableName != null && value != null && event != null) {
            SkJson.logger().info(String.format("PRIMITIVE -> (%s) %s => &a%s", value.getClass().getName(), variableName, value));
            Variables.setVariable(variableName, value, event, isLocal);
        }
    }

    private static boolean cannotBeParsed(JsonElement element) {
        return GsonParser.fromJson(element) instanceof JsonElement;
    }


    private static void saveParsedToVariable(String variableName, Object o, Event event, boolean isLocal) {
        if (variableName != null && o != null && event != null) {
            variableName = variableName.substring(0, variableName.length() - 2);

            if (SerializedJsonUtils.isJavaType(o)) {
                savePrimitiveToVariable(variableName, o, event, isLocal);
            } else {
                SkJson.logger().info(String.format("PARSED -> (%s) %s => &a%s", o.getClass().getName(), variableName, o));
                Variables.setVariable(variableName, o, event, isLocal);
            }
        }
    }


}
