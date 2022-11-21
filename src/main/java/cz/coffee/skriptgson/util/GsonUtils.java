/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.util;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static cz.coffee.skriptgson.util.Utils.newGson;

public class GsonUtils {

    private int i;
    private int type = 1;
    private Object input;

    private String sep;
    private boolean isLocal;



    public GsonUtils getKey(Object k) {
        this.type = 1;
        this.input = k;
        return this;
    }
    public GsonUtils getValue(Object k) {
        this.type = 2;
        this.input = k;
        return this;
    }


    public boolean check(JsonElement json) {
        if(json.isJsonArray()) {
            return(checkArray(json.getAsJsonArray(), this.type, this.input));
        } else if(json.isJsonObject()) {
            return(checkObject(json.getAsJsonObject(), this.type, this.input));
        }
        return false;
    }
    public JsonElement change(JsonElement json, Object to) {
       if(this.type == 1) {
           return(changeKey(json, this.input, to));
       } else if(this.type == 2) {
           return(changeValue(json, this.input, to));
       }
        return json;
    }

    public JsonElement append(JsonElement fromFile, Object Key, Object Nested, Object new_data) {
        if(fromFile.isJsonObject()) {
            return(append_(fromFile.getAsJsonObject(), Key.toString(), Nested.toString(), JsonParser.parseString(new_data.toString())));
        } else if(fromFile.isJsonArray()) {
            return(append_(fromFile.getAsJsonArray(), Key.toString(), Nested.toString(), JsonParser.parseString(new_data.toString())));
        }
        return(fromFile);
    }


    public JsonElement mapList(Event e, String name, boolean nullable, boolean isLocal) {
        this.isLocal = isLocal;
        JsonElement json;
        json = _listMainTree(e, name, nullable);
        return JsonParser.parseString(newGson().toJson(json));
    }

    private JsonElement _listSubTree(Event e, String name) {
        Object variable = getVariable(e, name);
        if(variable == null) {
            variable = _listMainTree(e, name+Variable.SEPARATOR, false);
        } else if( variable == Boolean.TRUE) {
            Object subtree = _listMainTree(e, name+Variable.SEPARATOR, true);
            if(subtree != null) variable = subtree;
        }

        if(!(variable instanceof String || variable instanceof Number || variable instanceof Map || variable instanceof List || variable instanceof JsonPrimitive)) {
            variable = newGson().toJsonTree(variable);
        }
        return newGson().toJsonTree(variable);
    }

    @SuppressWarnings("unchecked")
    private JsonElement _listMainTree(Event e, String name, boolean nullable) {
        Map<String, Object> variable = (Map<String, Object>) getVariable(e, name+"*");
        if(variable == null) {
            return nullable ? null : new JsonPrimitive("");
        }

        Stream<String> keys = variable.keySet().stream().filter(Objects::nonNull);
        if(variable.keySet().stream().filter(Objects::nonNull).allMatch(GsonUtils::isInteger)) {
            JsonArray array = new JsonArray();
            keys.forEach(k->array.getAsJsonArray().add((_listSubTree(e, name+k))));
            return array;
        } else {
            JsonObject object = new JsonObject();
            keys.forEach(k->object.getAsJsonObject().add(k, _listSubTree(e, name+k)));
            return object;
        }
    }


    public void mapJson(Event e, JsonElement json, String name, boolean isLocal) {
        sep = Variable.SEPARATOR;
        this.isLocal = isLocal;
        _mapJsonFirst(e, name, json);
    }


    private void mapJsonObject(Event e, String name, JsonObject element) {
        element.keySet().forEach(k-> _mapJson(e, name+sep+k, element.get(k)));
    }

    private void mapJsonArray(Event e, String name, JsonArray element) {
        for(int index = 0; element.size() > index; index++) {_mapJson(e, name+sep+(index+1), element.get(index));}
    }


    private void _mapJson(Event e, String name, JsonElement json) {
        if(json.isJsonObject()) {
            setVariable(name, json.getAsJsonObject(), e, isLocal);
            mapJsonObject(e, name, json.getAsJsonObject());
        } else if(json.isJsonArray()) {
            setVariable(name, json.getAsJsonArray(), e, isLocal);
            mapJsonArray(e, name, json.getAsJsonArray());
        } else if(json.isJsonPrimitive()) {
            setPrimitiveType(name, json.getAsJsonPrimitive(), e, isLocal);
        } else {
            setVariable(name, json, e, isLocal);
        }
    }

    private void _mapJsonFirst(Event e, String name, JsonElement json) {
        if(json == null) return;
        if(json.isJsonObject()) {
            mapJsonObject(e, name, json.getAsJsonObject());
        } else if(json.isJsonArray()) {
            mapJsonArray(e, name, json.getAsJsonArray());
        } else if(json.isJsonPrimitive()) {
            setPrimitiveType(name, json.getAsJsonPrimitive(), e, isLocal);
        } else {
            setVariable(name, json, e, isLocal);
        }
    }

    private static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if ( length == 0) {
            return  false;
        }
        int i = 0;
        if(str.charAt(0) == '-'){
            if(length == 1) {
                return false;
            }
            i = 1;
        }
        while (i < length) {
            char c =  str.charAt(i);
            if(c < '0' || c > '9') {
                return false;
            }
            i++;
        }
        return true;
    }


    private void setVariable(String name, Object element, Event e, boolean isLocal) {
        Variables.setVariable(name, element, e, isLocal);
    }

    private Object getVariable(Event e, String name) {
        final Object variable = Variables.getVariable(name, e, isLocal);
        if(variable == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return variable;
    }

    private void setPrimitiveType(String name, JsonPrimitive element, Event e, boolean isLocal) {
        if(element.isBoolean()) {
            setVariable(name, element.getAsBoolean(), e, isLocal);
        } else if(element.isNumber()) {
            setVariable(name, element.getAsDouble(), e, isLocal);
        } else if(element.isString()) {
            setVariable(name, element.getAsString(), e, isLocal);
        }
    }


    private static boolean checkObject(JsonObject jElem, int type, Object input) {
        for (Map.Entry<String, JsonElement> entry : jElem.entrySet()) {
            JsonElement element = entry.getValue();
            JsonElement j;
            if ( type == 1) {
                if (Objects.equals(entry.getKey(), input.toString())) {
                    return true;
                }
            } else if ( type == 2){
                if(input instanceof JsonElement) {
                    j = newGson().toJsonTree(input);
                } else {
                    j = JsonParser.parseString(input.toString());
                }
                if (Objects.equals(element, j)) return true;
            }
            if (element.isJsonArray()) {
                if (checkArray(element.getAsJsonArray(), type, input)) return true;
            } else if (element.isJsonObject()) {
                if (checkObject(element.getAsJsonObject(), type, input)) return true;
            }
        }
        return false;
    }
    private static boolean checkArray(JsonArray jArray, int type, Object input) {
        for (int index = 0; index < jArray.size(); index++) {
            JsonElement element = jArray.get(index);
            JsonElement j;

            if ( type == 1) {
                if (Objects.equals(element, input)) return true;
            } else if ( type == 2) {
                if(input instanceof JsonElement) {
                    j = newGson().toJsonTree(input);
                } else {
                    j = JsonParser.parseString(input.toString());
                }
                if (Objects.equals(element, j)) return true;
            }

            if (element.isJsonArray()) {
                if (checkArray(element.getAsJsonArray(), type, input)) return true;
            } else if (element.isJsonObject()) {
                if (checkObject(element.getAsJsonObject(), type, input)) return true;
            }
        }
        return false;
    }

    private JsonElement changeValue(JsonElement json, Object from, Object to) {
        JsonElement jel;
        if(json.isJsonArray()) {
            jel = json.getAsJsonArray();
            for (int index = 0; index < jel.getAsJsonArray().size(); index++) {
                JsonElement j = jel.getAsJsonArray().get(index);
                if(j.isJsonArray()) {
                    changeValue(j.getAsJsonArray(), from, to);
                } else if(j.isJsonObject()) {
                    changeValue(j.getAsJsonObject(), from, to);
                }
                if(Objects.equals(String.valueOf(index), from)) {
                    json.getAsJsonArray().remove(index);
                    if(to instanceof String) {
                        json.getAsJsonArray().add(to.toString());
                    }else if(to instanceof Integer | to instanceof Long) {
                        json.getAsJsonArray().add((int) to);
                    }else if(to instanceof Boolean) {
                        json.getAsJsonArray().add((boolean) to);
                    }else {
                        json.getAsJsonArray().add(JsonParser.parseString(to.toString()));
                    }
                }
            }
        } else if(json.isJsonObject()) {
            jel = json.getAsJsonObject();
            for(Map.Entry<String, JsonElement> map : jel.getAsJsonObject().entrySet()) {
                if(map.getValue().isJsonObject()){
                    changeValue(map.getValue().getAsJsonObject(), from, to);
                } else if(map.getValue().isJsonArray()) {
                    changeValue(map.getValue().getAsJsonArray(), from, to);
                }
                int n = 0;
                if(Objects.equals(map.getKey(), from)) {
                    if(map.getValue().isJsonPrimitive()) {
                        if(to instanceof Long)  n = ((Long) to).intValue();
                        if(to instanceof String) {
                            json.getAsJsonObject().addProperty(map.getKey(), to.toString());
                        } else if(to instanceof Integer || to instanceof Long) {
                            json.getAsJsonObject().addProperty(map.getKey(), n);
                        } else if(to instanceof Boolean) {
                            json.getAsJsonObject().addProperty(map.getKey(), (boolean) to);
                        }
                    } else {
                        json.getAsJsonObject().add(map.getKey(), JsonParser.parseString(to.toString()));
                    }
                }
            }
        }
        return JsonParser.parseString(json.toString());
    }

    private JsonElement changeKey(@NotNull JsonElement json, Object from, Object to) {
        JsonElement element;
        JsonElement n;

        if(json.isJsonObject()) {
            for(Map.Entry<String, JsonElement> map : json.getAsJsonObject().entrySet()) {
                element = map.getValue();
                if(getKey(from).check(element)) {
                    if(element.isJsonObject()) {
                        n = element.getAsJsonObject().get(from.toString());
                        if(n != null) {
                            element.getAsJsonObject().remove(from.toString());
                            element.getAsJsonObject().add(to.toString(), n);
                        }
                    }
                }
                if(element.isJsonObject()) {
                    changeKey(map.getValue().getAsJsonObject(), from, to);
                } else if(element.isJsonArray()) {
                    changeKey(map.getValue().getAsJsonArray(), from, to);
                }
            }
        } else if(json.isJsonArray()) {
            for (int index = 0; index < json.getAsJsonArray().size(); index++) {
                JsonElement e = json.getAsJsonArray().get(index);
                if (e.isJsonArray()) {
                    changeKey(e.getAsJsonArray(), from, to);
                } else if (e.isJsonObject()) {
                    changeKey(e.getAsJsonObject(), from, to);
                }
            }
        }
        return JsonParser.parseString(json.toString());
    }

    private JsonElement append_(JsonElement json, String Key, String Nested, JsonElement data) {
        String[] nesteds = Nested.split(":");
        JsonElement element;
        element = json;

        if(json.isJsonObject()) {
            for(String  k : nesteds) {
                if(!parseableInt(k)) {
                    if(!getKey(k).check(json)){
                        SkriptGson.severe("&cone of nested object not exists in the jsonFile.");
                        return null;
                    }
                }
            }

            for(String k : nesteds) {
                try {
                    element = element.getAsJsonObject().get(k);
                } catch (Exception ex) {
                    SkriptGson.severe("&cYour json file has wrong configuration");
                    return null;
                }

            }
            if(element.isJsonObject())
                element.getAsJsonObject().add(Key == null ? String.valueOf(element.getAsJsonObject().entrySet().size()) : Key,
                        data
                );
            if(element.isJsonArray())
                element.getAsJsonArray().add(data);

        } else if(json.isJsonArray()) {
            for(String  n : nesteds) {
                if(!parseableInt(n)) {
                    if(!getKey(n).check(json)){
                        SkriptGson.severe("&cone of nested object not exists in the jsonFile.");
                        return null;
                    }
                }
                int i = 0;
                if(parseableInt(n)) {
                    i = Integer.parseInt(n);
                }
                try {
                    element = parseableInt(n) ? element.getAsJsonArray().get(i) : element.getAsJsonObject().get(n);
                } catch (Exception ex) {
                    SkriptGson.severe("&cYour json file has wrong configuration");
                    return null;
                }

                if(element.isJsonObject()) {
                    SkriptGson.info("&aObject");
                    if(Objects.equals(n, nesteds[nesteds.length - 1]))
                        element.getAsJsonObject().add(Key == null ? String.valueOf(element.getAsJsonObject().entrySet().size()) : Key,
                                data);
                }
                if(element.isJsonArray())
                    if(Objects.equals(n, nesteds[nesteds.length - 1]))
                        element.getAsJsonArray().add(data);
            }
        }
        return json;
    }



    private boolean parseableInt(String NumberString) {
        boolean check;
        try {
            check = true;
            Integer.parseInt(NumberString);
        } catch (NumberFormatException ex) {
            check = false;
        }
        return check;
    }












    public static void parseJsonArray(JsonArray jArray) {
        for (int index = 0; index < jArray.size(); index++) {
            JsonElement element = jArray.get(index);
            if (element.isJsonArray()) {
                parseJsonArray(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                getValues(element.getAsJsonObject());
            }
        }
    }

    public static List<String> getValues(JsonObject jElem) {
        List<String> counts = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : jElem.entrySet()) {
            JsonElement element = entry.getValue();
            if (element.isJsonArray()) {
                parseJsonArray(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                getValues(element.getAsJsonObject());
            }
            counts.add(entry.getKey());
        }
        return counts;
    }



    public GsonUtils countOfKey(final JsonElement jel, String key, int ...type) {
        final JsonObject object;
        final JsonArray array;
        JsonElement n;
        if(jel.isJsonArray()) {
            array = jel.getAsJsonArray();
            for (int index = 0; index < array.size(); index++){
                JsonElement j = array.get(index);
                if ( type[0] == 1) {
                    if(Objects.equals(key, j.toString())) {
                        this.i++;
                    }
                } else if ( type[0] == 2) {
                    if(key.startsWith("{") || key.startsWith("[")) {
                        n = JsonParser.parseString(key);
                    } else {
                        n = newGson().toJsonTree(key);
                    }
                    if(Objects.equals(n, j)) {
                        this.i++;
                    }
                }
                if (j.isJsonObject()) {
                    countOfKey(j.getAsJsonObject(), key, type[0]);
                } else if (j.isJsonArray()) {
                    countOfKey(j.getAsJsonArray(), key, type[0]);
                }
            }
        } else if (jel.isJsonObject()) {
            object = jel.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                JsonElement j = entry.getValue();

                if ( type[0] == 1) {
                    if(Objects.equals(key, entry.getKey())) {
                        this.i++;
                    }
                } else if ( type[0] == 2) {
                    if(key.startsWith("{") || key.startsWith("[")) {
                        n = JsonParser.parseString(key);
                    } else {
                        n = newGson().toJsonTree(key);
                    }
                    if(Objects.equals(n, j)) {
                        this.i++;
                    }
                }
                if(j.isJsonObject()) {
                    countOfKey(j.getAsJsonObject(), key, type[0]);
                } else if(j.isJsonArray()) {
                    countOfKey(j.getAsJsonArray(), key, type[0]);
                }
            }
        }
        return this;
    }

    public int getCount() {
        return this.i;
    }
}
