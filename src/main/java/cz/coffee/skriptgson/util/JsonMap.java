/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cz.coffee.skriptgson.util.PluginUtils.newGson;

public class JsonMap {

    private int i;

    public static boolean checkObject(JsonObject jElem, int type, Object ...input) {
        for (Map.Entry<String, JsonElement> entry : jElem.entrySet()) {
            JsonElement element = entry.getValue();
            JsonElement j;
            if ( type == 1) {
                if (Objects.equals(entry.getKey(), input[0]))
                    return true;
            } else if ( type == 2){
                if(input[0].toString().startsWith("{") || input[0].toString().startsWith("[")) {
                    j = JsonParser.parseString(input[0].toString());
                } else {
                    j = newGson().toJsonTree(input[0]);
                }
                System.out.println(j);
                if (Objects.equals(element, j)){
                    return true;
                }
            }
            if (element.isJsonArray()) {
                if (checkArray(element.getAsJsonArray(), type, input[0])) {
                    return true;
                }
            } else if (element.isJsonObject()) {
                if (checkObject(element.getAsJsonObject(), type, input[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkArray(JsonArray jArray, int type, Object ...input) {
        for (int index = 0; index < jArray.size(); index++) {
            JsonElement element = jArray.get(index);
            if (element.isJsonArray()) {
                if (checkArray(element.getAsJsonArray(), type, input[0])) {
                    return true;
                }
            } else if (element.isJsonObject()) {
                if (checkObject(element.getAsJsonObject(), type, input[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    public JsonMap countOccurrenceOfKey(final JsonElement jel, String key, int ...type) {
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
                    countOccurrenceOfKey(j.getAsJsonObject(), key, type[0]);
                } else if (j.isJsonArray()) {
                    countOccurrenceOfKey(j.getAsJsonArray(), key, type[0]);
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
                    countOccurrenceOfKey(j.getAsJsonObject(), key, type[0]);
                } else if(j.isJsonArray()) {
                    countOccurrenceOfKey(j.getAsJsonArray(), key, type[0]);
                }
            }
        }
        return this;
    }

    public int getCount() {
        return this.i;
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
}
