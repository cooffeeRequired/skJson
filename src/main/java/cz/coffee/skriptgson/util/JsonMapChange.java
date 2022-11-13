package cz.coffee.skriptgson.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Objects;

public class JsonMapChange {

    public JsonElement changeValue(JsonElement json, Object from, Object to, boolean ...T) {
        JsonElement jel;
        boolean isJson = T[0];
        if(json.isJsonArray()) {
            jel = json.getAsJsonArray();
            for (int index = 0; index < jel.getAsJsonArray().size(); index++) {
                JsonElement j = jel.getAsJsonArray().get(index);
                if(j.isJsonArray()) {
                    changeValue(j.getAsJsonArray(), from, to, isJson);
                } else if(j.isJsonObject()) {
                    changeValue(j.getAsJsonObject(), from, to, isJson);
                }
                if(Objects.equals(String.valueOf(index), from)) {
                    json.getAsJsonArray().remove(index);
                    if(to instanceof String) {
                        json.getAsJsonArray().add(to.toString());
                    }else if(to instanceof Integer) {
                        json.getAsJsonArray().add((int) to);
                    }else if(to instanceof Boolean){
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
                    changeValue(map.getValue().getAsJsonObject(), from, to, isJson);
                } else if(map.getValue().isJsonArray()) {
                    changeValue(map.getValue().getAsJsonArray(), from, to, isJson);
                }
                if(Objects.equals(map.getKey(), from)) {
                    if(map.getValue().isJsonPrimitive()) {
                        json.getAsJsonObject().addProperty(map.getKey(), to.toString());
                    } else {
                        if(isJson) {
                            json.getAsJsonObject().add(map.getKey(), JsonParser.parseString(to.toString()));
                        } else {
                            json.getAsJsonObject().addProperty(map.getKey(), to.toString());
                        }
                    }
                }
            }
        }
        return JsonParser.parseString(json.toString());
    }

    public JsonElement changeKey(JsonElement json, Object from, Object to, boolean ...T) {
        boolean isJson = T[0];
        JsonElement element;
        JsonElement n;

        if(json.toString().contains(from.toString())) {
            if(json.isJsonObject()) {
                n = json.getAsJsonObject().get(from.toString());
                json.getAsJsonObject().remove(from.toString());
                json.getAsJsonObject().add(to.toString(), n);
            } else if(json.isJsonArray()){
                for (int index = 0; index < json.getAsJsonArray().size(); index++) {{
                    JsonElement e = json.getAsJsonArray().get(index);
                    if(e.isJsonArray()) {
                        changeKey(e.getAsJsonArray(), from, to, isJson);
                    } else if(e.isJsonObject()) {
                        changeKey(e.getAsJsonObject(), from, to, isJson);
                    }
                }}
            }
        } else {
            if(json.isJsonObject()) {
                for(Map.Entry<String, JsonElement> map : json.getAsJsonObject().entrySet()) {
                    element = map.getValue();
                    n = element.getAsJsonObject().get(from.toString());
                    if(element.toString().contains(from.toString())) {
                        if (element.isJsonObject()) {
                            element.getAsJsonObject().remove(from.toString());
                            element.getAsJsonObject().add(to.toString(), n);
                        }
                    }
                }
            } else if(json.isJsonArray()) {
                for (int index = 0; index < json.getAsJsonArray().size(); index++) {
                    JsonElement e = json.getAsJsonArray().get(index);
                    if (e.isJsonArray()) {
                        changeKey(e.getAsJsonArray(), from, to, isJson);
                    } else if (e.isJsonObject()) {
                        changeKey(e.getAsJsonObject(), from, to, isJson);
                    }
                }
            }
        }
        return JsonParser.parseString(json.toString());
    }
}
