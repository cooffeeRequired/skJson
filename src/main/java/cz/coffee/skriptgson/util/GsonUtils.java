package cz.coffee.skriptgson.util;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;

import java.util.*;
import java.util.stream.Stream;

import static cz.coffee.skriptgson.util.Utils.newGson;

public class GsonUtils {

    private String sep;
    private boolean isLocal;

    public enum Type {
        KEY, VALUE
    }

    private static boolean instancesOf(Object input) {
        return input instanceof String || input instanceof Number || input instanceof Map || input instanceof List || input instanceof JsonPrimitive || input instanceof Boolean;
    }


        /*
    Deprecated methods for mapping json
     */
    // Mapping


    public static class GsonMapping {

        private static String fieldName;

        public static void jsonToList(Event event, String name, JsonElement json, boolean isLocal) {
            String SEPARATOR = Variable.SEPARATOR;
            fieldName = name;
            JsonElement next;
            Deque<JsonElement> elements = new ArrayDeque<>();
            elements.add(json);

            //setVariable(name+SEPARATOR+"*", json, event, isLocal);

            while ((next = elements.pollFirst()) != null) {
                if (next instanceof JsonObject object) {
                    object.keySet().forEach(key -> {
                        JsonElement element = object.get(key);

                        String v = fieldName + SEPARATOR + key;
                        System.out.println(v);

                        if (element instanceof JsonObject elementObject) {
                            setVariable(v, elementObject, event, isLocal);
                            fieldName = v + SEPARATOR + key;
                            elements.offerLast(elementObject);
                        } else if (element instanceof JsonArray elementArray) {
                            setVariable(v, elementArray, event, isLocal);
                            elements.offerLast(elementArray);
                        } else if (element instanceof JsonPrimitive elementPrimitive) {
                            setPrimitiveType(v, elementPrimitive, event, isLocal);
                        } else {
                            setVariable(v, element, event, isLocal);
                        }
                    });
                } else if (next instanceof JsonArray array) {

                    System.out.println("Array: " + fieldName);

                    for (int index = 0; array.size() > index; index++) {
                        fieldName = fieldName + SEPARATOR + (index + 1);

                        System.out.println(fieldName);

                        JsonElement element = array.get(index);
                        if (element instanceof JsonObject elementObject) {
                            setVariable(fieldName, elementObject, event, isLocal);
                            elements.offerLast(elementObject);
                        } else if (element instanceof JsonArray elementArray) {
                            setVariable(fieldName, elementArray, event, isLocal);
                            elements.offerLast(elementArray);
                        } else if (element instanceof JsonPrimitive elementPrimitive) {
                            setPrimitiveType(fieldName, elementPrimitive, event, isLocal);
                        } else {
                            setVariable(fieldName, element, event, isLocal);
                        }
                    }
                } else if (next instanceof JsonPrimitive primitive) {
                    setPrimitiveType(name, primitive, event, isLocal);
                } else {
                    setVariable(name, next, event, isLocal);
                }
            }

        }


        public static JsonElement listToJson(Event event, String name) {
            JsonElement json = null;
            boolean isLocal = name.startsWith(Variable.LOCAL_VARIABLE_TOKEN);
            Object next;
            Deque<Object> objects = new ArrayDeque<>();
            Map<String, Object> variable = (Map<String, Object>) getVariable(event, name + "*", isLocal);
            if (variable == null) return new JsonNull();

            objects.add(variable);
            while ((next = objects.pollFirst()) != null) {
                Stream<String> keys = ((Map<String, Object>) next).keySet().stream().filter(Objects::nonNull);

                if (((Map<String, Object>) next).keySet().stream().filter(Objects::nonNull).allMatch(Utils::isNumeric)) {
                    JsonArray array = new JsonArray();
                    keys.forEach(key -> {
                        Object nestedVar = getVariable(event, name + key, isLocal);
                        array.add(newGson().toJsonTree(nestedVar));
                    });
                    json = array;
                } else {
                    JsonObject object = new JsonObject();
                    keys.forEach(key -> {
                        Object nestedVar = getVariable(event, name + key, isLocal);
                        object.add(key, newGson().toJsonTree(nestedVar));
                    });
                    json = object;
                }
            }
            return json;
        }
    }

    public static boolean isInt(String NumberString) {
        boolean check;
        try {
            check = true;
            Integer.parseInt(NumberString);
        } catch (NumberFormatException ex) {
            check = false;
        }
        return check;
    }

    public static boolean check(JsonElement json, String search, Type type) {
        boolean match = false;
        JsonElement next;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(json);
        while ((next = elements.pollFirst()) != null) {
            if (next instanceof JsonArray array) {
                for (JsonElement element : array) elements.offerLast(element);
            } else if (next instanceof JsonObject map) {
                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                    JsonElement value = entry.getValue();
                    if (type == Type.KEY) {
                        if (entry.getKey().equals(search)) match = true;
                        if (!value.isJsonPrimitive()) elements.offerLast(value);
                    } else if (type == Type.VALUE) {
                        if (value.equals(JsonParser.parseString(search))) match = true;
                        elements.offerLast(value);
                    }
                }
            }
        }
        return match;
    }

    public static JsonElement change(JsonElement json, String from, Object to, Type type) {
        JsonElement next, n;
        ArrayDeque<JsonElement> elements = new ArrayDeque<>();
        boolean changed = false;
        elements.add(json);

        while ((next = elements.pollFirst()) != null) {
            if (next instanceof JsonObject map) {
                if (type.equals(Type.KEY)) {
                    n = map.get(from);
                    if (n != null) {
                        map.remove(from);
                        map.add(to.toString(), n);
                    } else {
                        for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                            if (entry.getKey().equals(from)) {
                                n = map.get(from);
                                if (n != null) {
                                    map.remove(from);
                                    map.add(to.toString(), n);
                                }
                            }
                            elements.offerLast(entry.getValue());
                        }
                    }
                } else if (type.equals(Type.VALUE)) {
                    for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                        JsonElement value = entry.getValue();
                        if (entry.getKey().equals(from)) {
                            changed = true;
                            if (value instanceof JsonPrimitive) {
                                if (to instanceof Integer i)
                                    map.addProperty(entry.getKey(), i);
                                else if (to instanceof String str)
                                    map.addProperty(entry.getKey(), str);
                                else if (to instanceof Boolean bool)
                                    map.addProperty(entry.getKey(), bool);
                                else
                                    map.add(entry.getKey(), JsonParser.parseString(to.toString()));
                            } else {
                                map.add(entry.getKey(), JsonParser.parseString(to.toString()));
                            }
                        }
                        if (!changed) elements.offerLast(value);
                    }
                }
            } else if (next instanceof JsonArray array) {
                for (JsonElement element : array) {
                    elements.offerLast(element);
                }
            }

        }
        return json;
    }

    public static JsonElement append(JsonElement jsonFromFile, JsonElement jsonToAppend, String key, String Nested) {
        String[] nest = Nested.split(":");
        JsonElement next;
        boolean isArrayKey = false;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(jsonFromFile);
        while ((next = elements.pollFirst()) != null) {
            for (String c : nest) {
                if (isInt(c)) isArrayKey = true;
                if (!check(jsonFromFile, c, Type.KEY)) {
                    SkriptGson.severe("One of nested object doesn't exist in the JSON file");
                    return null;
                }
                if (next instanceof JsonArray array) next = array.get(isArrayKey ? Integer.parseInt(c) : 0);
                else if (next instanceof JsonObject map) next = map.get(c);
            }
            if (next instanceof JsonObject map) {
                map.add(key == null ? String.valueOf(map.size()) : key, jsonToAppend);
            } else if (next instanceof JsonArray array) {
                array.add(jsonToAppend);
            }
        }
        return jsonFromFile;
    }

    // Counting designed by Kenzie#0001
    public static int count(String search, JsonElement start, Type type) {
        int count = 0;
        JsonElement next;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(start);
        while ((next = elements.pollFirst()) != null) {
            if (next instanceof JsonArray array) {
                for (JsonElement element : array) elements.offerLast(element);
            } else if (next instanceof JsonObject map) {
                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                    JsonElement value = entry.getValue();
                    if (type == Type.KEY) {
                        if (entry.getKey().equals(search)) count++;
                        if (!value.isJsonPrimitive()) elements.offerLast(value);
                    } else if (type == Type.VALUE) {
                        if (value.equals(JsonParser.parseString(search))) count++;
                        elements.offerLast(value);
                    }
                }
            }
        }
        return count;
    }


    /*
     * Private Functions
     */
    private static void setPrimitiveType(String name, JsonPrimitive element, Event event, boolean isLocal) {
        if (element.isBoolean()) {
            setVariable(name, element.getAsBoolean(), event, isLocal);
        } else if (element.isNumber()) {
            setVariable(name, element.getAsDouble(), event, isLocal);
        } else if (element.isString()) {
            setVariable(name, element.getAsString(), event, isLocal);
        }
    }

    public static void setVariable(String name, Object element, Event event, boolean isLocal) {
        Variables.setVariable(name, element, event, isLocal);
    }

    private static Object getVariable(Event e, String name, boolean isLocal) {
        final Object variable = Variables.getVariable(name, e, isLocal);
        if (variable == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return variable;
    }

}
