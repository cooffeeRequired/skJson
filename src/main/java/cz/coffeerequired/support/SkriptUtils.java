package cz.coffeerequired.support;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.skript.SkriptSecLoop;
import cz.coffeerequired.skript.core.expressions.ExprJson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.MatchResult;

import static ch.njol.skript.lang.Variable.SEPARATOR;

@SuppressWarnings("unused")
public abstract class SkriptUtils {

    public static List<SecLoop> currentLoops() {
        return ParserInstance.get().getCurrentSections(SecLoop.class);
    }

    public static SkriptSecLoop getSecLoop(int i, String input) {
        int j = 1;
        SecLoop loop = null;
        for (SecLoop l : currentLoops()) {
            if (l.getLoopedExpression() instanceof ExprJson) {
                if (j < i) {
                    j++;
                    continue;
                }
                if (loop != null) {
                    return null;
                }
                loop = l;
                if (j == i)
                    break;
            }
        }
        return new SkriptSecLoop(loop);
    }

    public static Integer getLoopIndex(MatchResult numberOfLoop) {
        try {
            if (numberOfLoop == null) return null;
            var group = numberOfLoop.group(0);
            SkJson.debug("group -> %s", group);
            return Integer.parseInt(group);
        } catch (Exception e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> clazz, int length) {
        return (T[]) Array.newInstance(clazz, length);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> clazz) {
        return (T[]) Array.newInstance(clazz, 0);
    }

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
                } catch (NumberFormatException ignored) {
                }
            }
        }

        for (int i = 0; i <= maxIndex; i++) {
            list.add(null);
        }

        // Fill that list with values
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                try {
                    int index = Integer.parseInt(entry.getKey().toString());
                    Object value = Parser.toJson(entry.getValue());
                    if (value instanceof Map) {
                        list.set(index, cleanupMap((Map<?, ?>) value));
                    } else {
                        list.set(index, value);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Delete all nulls or empties
        while (!list.isEmpty() && list.getFirst() == null) list.removeFirst();
        return list;
    }

    public static void convertJsonToSkriptVariable(@NotNull String variableName, @NotNull JsonElement json, @NotNull Event event, boolean isLocal) {

        switch (json) {
            case JsonPrimitive primitive ->
                    savePrimitiveToVariable(variableName, Parser.fromJson(primitive), event, isLocal);
            case JsonObject object -> {
                var parsed = Parser.fromJson(object);
                if (!cannotBeParsed(parsed)) {
                    saveParsedToVariable(variableName, parsed, event, isLocal);
                } else {
                    for (String key : object.keySet()) {
                        JsonElement value = object.get(key);
                        String pathKey = variableName + key + SEPARATOR;
                        if (!cannotBeParsed(value)) {
                            saveParsedToVariable(pathKey, parsed, event, isLocal);
                        } else {
                            convertJsonToSkriptVariable(pathKey, value, event, isLocal);
                        }
                    }
                }
            }
            case JsonArray array -> {
                for (int i = 0; i < array.size(); i++) {
                    JsonElement element = array.get(i);
                    String newName = variableName + (i + 1) + SEPARATOR;
                    Object parsed = Parser.fromJson(element);

                    if (cannotBeParsed(element)) {
                        convertJsonToSkriptVariable(newName, element, event, isLocal);
                    } else {
                        saveParsedToVariable(newName, parsed, event, isLocal);
                    }
                }
            }
            default -> {
            }
        }
    }

    private static void savePrimitiveToVariable(String variableName, Object value, Event event, boolean isLocal) {
        if (variableName != null && value != null && event != null) {
            if (variableName.endsWith(SEPARATOR)) {
                variableName = variableName.substring(0, variableName.length() - 2);
            }
            value = value instanceof JsonPrimitive ? Parser.fromJson((JsonPrimitive) value) : value;
            assert value != null;
            Variables.setVariable(variableName, value, event, isLocal);
        }
    }

    private static boolean cannotBeParsed(Object element) {
        return element instanceof JsonElement;
    }

    private static void saveParsedToVariable(String variableName, Object o, Event event, boolean isLocal) {
        if (variableName != null && o != null && event != null) {
            if (variableName.endsWith(SEPARATOR)) {
                variableName = variableName.substring(0, variableName.length() - 2);
            }
            if (JsonAccessorUtils.isJavaType(o)) {
                savePrimitiveToVariable(variableName, o, event, isLocal);
            } else {
                Variables.setVariable(variableName, o, event, isLocal);
            }
        }
    }

    public static <T> boolean isSingleton(T[] collection) {
        return collection != null && collection.length == 1;
    }


    @FunctionalInterface
    public interface ComparatorDelta<T> {
        boolean compare(T o);
    }

    public static <E> boolean anyElementIs(Collection<E> delta, ComparatorDelta<E> o) {
        return delta.stream().anyMatch(o::compare);
    }

    public static <E> boolean anyElementIs(E[] delta, ComparatorDelta<E> o) {
        return Arrays.stream(delta).anyMatch(o::compare);
    }
}
