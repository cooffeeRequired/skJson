package cz.coffeerequired.api.json;

import ch.njol.skript.Skript;
import ch.njol.skript.log.ErrorQuality;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

import static cz.coffeerequired.api.json.SerializedJsonUtils.handle;

@SuppressWarnings("unused")
public class SerializedJson {
    @Getter private final JsonElement json;

    public changer changer;
    public counter counter;
    public remover remover;
    public searcher searcher;

    public SerializedJson(JsonElement json) {
        if (SerializedJsonUtils.isNull(json)) throw new SerializedJsonException("Json cannot be null");
        this.json = json;
        this.changer = new changer(json);
        this.counter = new counter(json);
        this.remover = new remover(json);
        this.searcher = new searcher(json);
    }

    public record changer(JsonElement json) {
        public void key(ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens, String newKey) {
            var deque = SerializedJsonUtils.listToDeque(tokens);
            var key = deque.removeLast().getKey();
            JsonElement current = json;
            Map.Entry<String,SkriptJsonInputParser. Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, true);
            }

            if (!current.isJsonObject()) {
                Skript.error("Key could be changed only in Json Object but found (" + current.getClass().getSimpleName() + ")", ErrorQuality.SEMANTIC_ERROR);
            } else {

                SkJson.debug("current:? " + current);

                ((JsonObject) current).add(newKey, current.getAsJsonObject().get(key));
                ((JsonObject) current).remove(key);
            }
        }

        public void value(ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens, JsonElement value) {
            var deque = SerializedJsonUtils.listToDeque(tokens);
            var temp = deque.removeLast();
            var key = temp.getKey();

            SkJson.debug("keys: " + tokens);

            JsonElement current = json;
            Map.Entry<String,SkriptJsonInputParser. Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, true);
            }

            Number index;

            if ((index = SerializedJsonUtils.isNumeric(key)) != null) {
                if (current == null) current = new JsonArray();
                if (!current.isJsonArray()) throw new SerializedJsonException("Index could be changed only in Json Arrays");

                if (((JsonArray) current).isEmpty()) {
                    ((JsonArray) current).add(value);
                } else if (((JsonArray) current).size() <= index.intValue()) {
                    ((JsonArray) current).add(value);
                } else {
                    ((JsonArray) current).set(index.intValue(), value);
                }
            } else {
                if (current == null) current = new JsonObject();

                if (current instanceof JsonArray jArray) {
                    jArray.add(value);
                } else if (current instanceof JsonObject jsonObject) {
                    jsonObject.add(key, value);
                } else {
                    throw new SerializedJsonException("Key could be changed only in Json Objects");
                }
            }
        }
    }
    public record counter(JsonElement json) {
        public int keys(String key) {
            int count = 0;
            Deque<JsonElement> deque = new ArrayDeque<>();
            deque.add(json);
            JsonElement current;

            while ((current = deque.pollFirst()) != null) {
               if (current instanceof JsonObject jsonObject) {
                   for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                       if (entry.getKey().equals(key)) count++;
                       if (!entry.getValue().isJsonPrimitive()) deque.offerLast(entry.getValue());
                   }
               } else if (current instanceof JsonArray jsonArray) {
                   for (JsonElement element : jsonArray) if (!element.isJsonPrimitive()) deque.offerLast(element);
               }
            }
            return count;
        }

        public <V> int values(V object) {
            int count = 0;
            Deque<JsonElement> deque = new ArrayDeque<>();
            deque.add(json);
            JsonElement current;

            JsonElement value = GsonParser.toJson(object);

            while ((current = deque.pollFirst()) != null) {
               if (current instanceof JsonObject jsonObject) {
                   for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                       if (entry.getValue().equals(value)) count++;
                       if (!entry.getValue().isJsonPrimitive()) deque.offerLast(entry.getValue());
                   }
               } else if (current instanceof JsonArray jsonArray) {
                    for (JsonElement element : jsonArray)
                        if (!element.isJsonPrimitive()) deque.offerLast(element);
                        else if (element.equals(value)) count++;
               }
            }

            return count;
        }
    }
    public record remover(JsonElement json) {
        public void byKey(ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens) {
            var deque = SerializedJsonUtils.listToDeque(tokens);
            var key = deque.removeLast().getKey();
            JsonElement current = json;
            Map.Entry<String,SkriptJsonInputParser. Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, false);
            }

            if (!current.isJsonObject()) {
                throw new SerializedJsonException("Key could be removed only in Json Objects");
            } else {
                ((JsonObject) current).remove(key);
            }
        }
        public void byIndex(ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens) {
            var deque = SerializedJsonUtils.listToDeque(tokens);
            var key = deque.removeLast().getKey();
            JsonElement current = json;
            Map.Entry<String,SkriptJsonInputParser. Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, false);
            }

            Number index;

            if ((index = SerializedJsonUtils.isNumeric(key)) != null) {
                if (!current.isJsonArray()) throw new SerializedJsonException("Index could be removed only in Json Arrays");
                ((JsonArray) current).remove(index.intValue());
            }
        }
        public void byValue(ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens, Object value) {
            var deque = SerializedJsonUtils.listToDeque(tokens);
            var key = deque.removeLast().getKey();
            JsonElement current = json;
            Map.Entry<String,SkriptJsonInputParser. Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, false);
            }

            JsonElement valueElement = GsonParser.toJson(value);
            Number index;

            if (current instanceof JsonArray jsonArray) {
                if ((index = SerializedJsonUtils.isNumeric(key)) != null) {
                    JsonElement val = jsonArray.get(index.intValue());
                    if (val.equals(valueElement)) jsonArray.remove(index.intValue());
                } else {
                    throw new SerializedJsonException("Given key is instance of String not int\\double!");
                }
            } else if (current instanceof JsonObject jsonObject) {
                JsonElement val = jsonObject.get(key);
                if (val.equals(valueElement)) jsonObject.remove(key);
            } else {
                throw new SerializedJsonException("Value could be removed only in Json Arrays or Json Objects");
            }
        }
    }
    public record searcher(JsonElement json) {
        public Object keyOrIndex(ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens) {
            var deque = SerializedJsonUtils.listToDeque(tokens);
            var key = deque.removeLast().getKey();
            JsonElement current = json;
            Map.Entry<String, SkriptJsonInputParser.Type> currentKey;

            while ((currentKey = deque.pollFirst()) != null) {
                current = handle(current, currentKey, true);
            }

            if (current instanceof JsonArray array) {
                Number index = SerializedJsonUtils.isNumeric(key);
                if (index != null && index.intValue() <= array.size()) {
                    return GsonParser.fromJson(array.get(index.intValue()));
                }
            } else if (current instanceof JsonObject object) {
                var searched = current.getAsJsonObject().get(key);
                return GsonParser.fromJson(searched);
            } else {
                throw new SerializedJsonException("Key could be searched only in Json Objects\\Arrays");
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "SerializedJson{" + "json=" + json + '}';
    }
}
