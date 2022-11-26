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

    /*
    Deprecated methods for mapping json
     */
    // Mapping
    private JsonElement _listSubTree(Event e, String name) {
        Object variable = getVariable(e, name, isLocal);
        if (variable == null) {
            variable = _listMainTree(e, name + Variable.SEPARATOR, false);
        } else if (variable == Boolean.TRUE) {
            Object subtree = _listMainTree(e, name + Variable.SEPARATOR, true);
            if (subtree != null) variable = subtree;
        }

        if (!(variable instanceof String || variable instanceof Number || variable instanceof Map || variable instanceof List || variable instanceof JsonPrimitive)) {
            variable = newGson().toJsonTree(variable);
        }
        return newGson().toJsonTree(variable);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private JsonElement _listMainTree(Event e, String name, boolean nullable) {
        Map<String, Object> variable = (Map<String, Object>) getVariable(e, name + "*", isLocal);
        if (variable == null) {
            return nullable ? null : new JsonNull();
        }

        Stream<String> keys = variable.keySet().stream().filter(Objects::nonNull);

        if (variable.keySet().stream().filter(Objects::nonNull).allMatch(Utils::isNumeric)) {
            JsonArray array = new JsonArray();
            keys.forEach(k -> array.getAsJsonArray().add((_listSubTree(e, name + k))));
            return array;
        } else {
            JsonObject object = new JsonObject();
            keys.forEach(k -> object.getAsJsonObject().add(k, _listSubTree(e, name + k)));
            return object;
        }
    }

    private void _mapJson(Event e, String name, JsonElement json) {
        if (json.isJsonObject()) {
            setVariable(name, json.getAsJsonObject(), e, isLocal);
            mapJsonObject(e, name, json.getAsJsonObject());
        } else if (json.isJsonArray()) {
            setVariable(name, json.getAsJsonArray(), e, isLocal);
            mapJsonArray(e, name, json.getAsJsonArray());
        } else if (json.isJsonPrimitive()) {
            setPrimitiveType(name, json.getAsJsonPrimitive(), e, isLocal);
        } else {
            setVariable(name, json, e, isLocal);
        }
    }

    private void _mapJsonFirst(Event e, String name, JsonElement json) {
        if (json == null) return;
        if (json.isJsonObject()) {
            mapJsonObject(e, name, json.getAsJsonObject());
        } else if (json.isJsonArray()) {
            mapJsonArray(e, name, json.getAsJsonArray());
        } else if (json.isJsonPrimitive()) {
            setPrimitiveType(name, json.getAsJsonPrimitive(), e, isLocal);
        } else {
            setVariable(name, json, e, isLocal);
        }
    }

    private void mapJsonObject(Event e, String name, JsonObject element) {
        element.keySet().forEach(k -> _mapJson(e, name + sep + k, element.get(k)));
    }

    private void mapJsonArray(Event e, String name, JsonArray element) {
        for (int index = 0; element.size() > index; index++) {
            _mapJson(e, name + sep + (index + 1), element.get(index));
        }
    }

    public JsonElement mapList(Event e, String name, boolean nullable, boolean isLocal) {
        this.isLocal = isLocal;
        JsonElement json;
        json = _listMainTree(e, name, nullable);
        return JsonParser.parseString(newGson().toJson(json));
    }

    public void mapJson(Event e, JsonElement json, String name, boolean isLocal) {
        sep = Variable.SEPARATOR;
        this.isLocal = isLocal;
        _mapJsonFirst(e, name, json);
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
                for(Map.Entry<String, JsonElement> entry : map.entrySet()) {
                    JsonElement value = entry.getValue();
                    if(type == Type.KEY) {
                        if(entry.getKey().equals(search)) match = true;
                        if(!value.isJsonPrimitive()) elements.offerLast(value);
                    } else if (type == Type.VALUE) {
                        if(value.equals(JsonParser.parseString(search))) match = true;
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
            if(next instanceof JsonObject map) {
                if(type.equals(Type.KEY)){
                    n = map.get(from);
                    if(n != null) {
                        map.remove(from);
                        map.add(to.toString(), n);
                    } else {
                        for(Map.Entry<String, JsonElement> entry : map.entrySet()) {
                            if(entry.getKey().equals(from)) {
                                n = map.get(from);
                                if(n != null) {
                                    map.remove(from);
                                    map.add(to.toString(), n);
                                }
                            }
                            elements.offerLast(entry.getValue());
                        }
                    }
                } else if (type.equals(Type.VALUE)) {
                    for(Map.Entry<String, JsonElement> entry : map.entrySet()) {
                        JsonElement value = entry.getValue();
                        if(entry.getKey().equals(from)) {
                            changed = true;
                            if(value instanceof JsonPrimitive) {
                                if(to instanceof Integer i)
                                    map.addProperty(entry.getKey(), i);
                                else if(to instanceof String str)
                                    map.addProperty(entry.getKey(), str);
                                else if(to instanceof Boolean bool)
                                    map.addProperty(entry.getKey(), bool);
                                else
                                    map.add(entry.getKey(), JsonParser.parseString(to.toString()));
                            } else {
                                map.add(entry.getKey(), JsonParser.parseString(to.toString()));
                            }
                        }
                        if(!changed) elements.offerLast(value);
                    }
                }
            } else if(next instanceof JsonArray array) {
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
    private void setPrimitiveType(String name, JsonPrimitive element, Event event, boolean isLocal) {
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
    private Object getVariable(Event e, String name, boolean isLocal) {
        final Object variable = Variables.getVariable(name, e, isLocal);
        if (variable == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return variable;
    }

}
