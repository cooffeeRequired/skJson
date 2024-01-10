package cz.coffee.skjson.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.JsonParserI;
import cz.coffee.skjson.utils.LoggingUtil;
import cz.coffee.skjson.utils.Util;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static cz.coffee.skjson.utils.PatternUtil.keyStruct;
import static cz.coffee.skjson.utils.Util.fstring;
import static cz.coffee.skjson.utils.Util.parseNumber;

public class JsonParser {
    private static void isNull(final JsonElement json) throws JsonParserException {
        if (json == null) throw new JsonParserException(fstring("Input cannot be null, required: (JsonObject, JsonArray, JsonPrimitive), given: %s", "NULL"));
    }

    public static Changer change(final JsonElement json) {
        return new Changer(json);
    }

    public static Remover remove(final JsonElement json) {
        return new Remover(json);
    }

    public static Searcher search(final JsonElement json) {
        return new Searcher(json);
    }

    public static Counter count(final JsonElement json) {
        return new Counter(json);
    }

    public static boolean isExpression(final JsonElement json) {
        AtomicBoolean r = new AtomicBoolean(true);
        if (json.isJsonObject()) {
            json.getAsJsonObject().entrySet().forEach(entry -> {
                if (!(entry.getKey().equals("element-index") || entry.getKey().equals("element-path"))) {
                    r.set(false);
                }
            });
        }
        return r.get();
    }

    static Deque<JsonElement> subDequeJsons(final JsonElement json, List<keyStruct> keys) {
        Deque<JsonElement> currents = new ConcurrentLinkedDeque<>();
        try {
            isNull(json);
            currents.offerLast(json);
            for (keyStruct struct : keys.subList(0, keys.size() - 1)) {
                JsonElement current = currents.pollLast();
                if (current == null || current.isJsonNull()) {
                    return currents;
                }
                if (current.isJsonObject()) {
                    currents.offerLast(current.getAsJsonObject().get(struct.key()));
                } else if (current.isJsonArray()) {
                    currents.offerLast(current.getAsJsonArray().get(Integer.parseInt(struct.key())));
                }
            }
            return currents;
        } catch (Exception ex) {
            LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
        }
        return null;
    }

    static void current(JsonElement value, Deque<JsonElement> currentElements) {
        JsonElement current;
        current = currentElements.pollLast();
        if (current == null || current.isJsonNull()) return;

        if (current.isJsonObject()) {
            JsonObject jsonObject = current.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
            entries.removeIf(entry -> entry.getValue().equals(value));
        } else if (current.isJsonArray()) {
            JsonArray jsonArray = current.getAsJsonArray();
            for (int i = jsonArray.size() - 1; i >= 0; i--) {
                JsonElement element = jsonArray.get(i);
                if (element.equals(value)) {
                    jsonArray.remove(i);
                }
            }
        }
    }

    public record Searcher(JsonElement json) implements JsonParserI.Searcher {

            public Searcher(final JsonElement json) {
                this.json = json;
                try {
                    isNull(json);
                } catch (JsonParserException ex) {
                    LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
                }
            }

            @Override
            public JsonElement key(Queue<keyStruct> keys) {
                Deque<JsonElement> currents = new ConcurrentLinkedDeque<>();
                currents.offerLast(this.json);
                for (keyStruct struct : keys) {
                    JsonElement current = currents.pollLast();
                    if (current == null || current.isJsonNull()) return null;

                    if (current instanceof JsonObject jsonobject) {
                        current = jsonobject.get(struct.key());
                    } else if (current instanceof JsonArray jsonarray) {
                        current = jsonarray.get(parseNumber(struct.key()));
                    }
                    if (current != null) currents.offerLast(current);
                }
                return currents.pollLast();
            }
        }

    public record Remover(JsonElement json) implements JsonParserI.Remover {
            public Remover(final JsonElement json) {
                this.json = json;
                try {
                    isNull(json);
                } catch (JsonParserException ex) {
                    LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
                }
            }

            @Override
            public void byValue(LinkedList<keyStruct> keys, JsonElement value) {
                Deque<JsonElement> currents = new ConcurrentLinkedDeque<>();
                JsonElement current;
                currents.offerLast(this.json);

                for (keyStruct struct : keys) {
                    current = currents.pollLast();
                    if (current == null || current.isJsonNull()) return;
                    if (current instanceof JsonObject jsonobject) {
                        currents.offerLast(jsonobject.get(struct.key()));
                    } else if (current instanceof JsonArray jsonarray) {
                        currents.offerLast(jsonarray.get(parseNumber(struct.key())));
                    }
                }

                current = currents.pollLast();
                if (current == null || current.isJsonNull()) return;
                if (current instanceof JsonObject jsonobject) {
                    String found = null;
                    for (Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                        if (entry.getValue().equals(value)) {
                            found = entry.getKey();
                            break;
                        }
                    }
                    jsonobject.remove(found);
                } else if (current instanceof JsonArray jsonarray) {
                    jsonarray.remove(value);
                }
            }

            @Override
            public void byIndex(LinkedList<keyStruct> keys) {
                Deque<JsonElement> currents = subDequeJsons(this.json, keys);
                assert currents != null;
                JsonElement current = currents.pollLast();
                if (current == null || current.isJsonNull()) return;

                if (current instanceof JsonArray jsonarray) {
                    int index = parseNumber(keys.get(keys.size() - 1).key());
                    if (index < 0 || index >= jsonarray.size()) return;
                    jsonarray.remove(index);
                }

            }

            @Override
            public void byKey(LinkedList<keyStruct> keys) {
                Deque<JsonElement> currents = subDequeJsons(this.json, keys);
                assert currents != null;
                String lastKey = keys.getLast().key();
                JsonElement current = currents.pollLast();

                if (current == null || current.isJsonNull()) return;

                if (current instanceof JsonObject jsonobject) {
                    jsonobject.remove(lastKey);
                } else if (current instanceof JsonArray jsonarray) {
                    for (JsonElement e : jsonarray) {
                        if (e instanceof JsonObject eo && eo.has(lastKey)) {
                            jsonarray.remove(e);
                            break;
                        }
                    }
                }
            }

            @Override
            public void allByValue(LinkedList<keyStruct> keys, JsonElement value) {
                Deque<JsonElement> currents = new ConcurrentLinkedDeque<>();
                JsonElement current;
                currents.offerLast(this.json);

                if (keys == null || keys.isEmpty()) {
                    current(value, currents);
                    return;
                }

                while (!keys.isEmpty()) {
                    current = currents.pollLast();
                    if (current == null || current.isJsonNull()) return;

                    if (current.isJsonObject()) {
                        JsonObject jsonObject = current.getAsJsonObject();
                        String key = Objects.requireNonNull(keys.pollFirst()).key();
                        currents.offerLast(jsonObject.get(key));
                    } else if (current.isJsonArray()) {
                        JsonArray jsonArray = current.getAsJsonArray();
                        int index = Integer.parseInt(Objects.requireNonNull(keys.pollFirst()).key());
                        currents.offerLast(jsonArray.get(index));
                    }
                }
                current(value, currents);
            }
        }

    public record Changer(JsonElement json) implements JsonParserI.Changer {
            public Changer(final JsonElement json) {
                this.json = json;
                try {
                    isNull(json);
                } catch (JsonParserException ex) {
                    LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
                }
            }

        @Override
        public void key(LinkedList<keyStruct> keys, String key) {
                Deque<JsonElement> currents = subDequeJsons(this.json, keys);
                assert currents != null;
                String lastKey = keys.getLast().key();

                JsonElement current = currents.pollLast();
                if (current == null || current.isJsonNull()) current = this.json;

                if (current instanceof JsonObject jsonobject) {
                    JsonElement value = jsonobject.remove(lastKey);
                    if (value != null) jsonobject.add(key, value);
                } else if (current instanceof JsonArray jsonarray) {
                    try {
                        int index = Integer.parseInt(lastKey);
                        if (index >= 0 && index < jsonarray.size()) {
                            jsonarray.remove(index);
                            jsonarray.set(index, ParserUtil.parse(key));
                        }
                    } catch (Exception ex) {
                        LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
                    }
                }
            }

        @Override
            public void value(LinkedList<keyStruct> keys, JsonElement value) {
            Deque<JsonElement> currents = new ConcurrentLinkedDeque<>();
            currents.offerLast(this.json);
            JsonElement current;
            keyStruct lastKey = keys.removeLast();
            while ((current = currents.pollLast()) != null) {
                    for (keyStruct struct : keys) {
                        if (struct.key().isEmpty() || struct.key().isBlank()) continue;
                        try {
                            int index = Util.isNumber(struct.key()) ? parseNumber(struct.key()) : -1;
                            if (current instanceof JsonObject jsonobject) {
                                if (!jsonobject.has(struct.key())) {
                                    if (struct.isList()) {
                                        jsonobject.add(struct.key(), new JsonArray());
                                    } else {
                                        jsonobject.add(struct.key(), new JsonObject());
                                    }
                                }
                                current = jsonobject.get(struct.key());
                            } else if (current instanceof JsonArray jsonArray) {
                                if (index >= jsonArray.size()) {
                                    if (struct.isList()) {
                                        jsonArray.add(new JsonArray());
                                    } else {
                                        jsonArray.add(new JsonObject());
                                    }
                                }
                                current = jsonArray.get(index);
                            }
                        } catch (Exception ex) {
                            LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
                        }
                    }
                    if (current instanceof JsonObject jsonobject) {
                        String last = lastKey == null ? String.valueOf(jsonobject.size()) : lastKey.key();
                        jsonobject.add(last, value);
                    } else if (current instanceof JsonArray jsonarray) {
                        int index = -1;
                        for (int i = 0; i < jsonarray.size(); i++) {
                            assert lastKey != null;
                            if (i == parseNumber(lastKey.key())) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            jsonarray.set(index, value);
                        } else {
                            jsonarray.remove(value);
                            jsonarray.add(value);
                        }
                    }
                }
            }
        }

    public record Counter(JsonElement json) implements JsonParserI.Counter {
            public Counter(final JsonElement json) {
                this.json = json;
                try {
                    isNull(json);
                } catch (JsonParserException ex) {
                    LoggingUtil.enchantedError(ex, ex.getStackTrace(), ex.getLocalizedMessage());
                }
            }

            @Override
            public int keys(@NonNull String key) {
                int count = 0;
                JsonElement value;
                Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
                elements.add(this.json);

                while ((value = elements.pollFirst()) != null) {
                    if (value instanceof JsonArray) {
                        for (JsonElement l : value.getAsJsonArray()) elements.offerLast(l);
                    } else if (value instanceof JsonObject) {
                        for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                            if (entry.getKey().equals(key)) count++;
                            if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                        }
                    }
                }
                return count;
            }

            @Override
            public int values(@NonNull JsonElement value) {
                int count = 0;
                JsonElement jsonElement;
                Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
                elements.add(this.json);

                while ((jsonElement = elements.pollFirst()) != null) {
                    if (jsonElement instanceof JsonArray) {
                        for (JsonElement l : jsonElement.getAsJsonArray()) elements.offerLast(l);
                    } else if (jsonElement instanceof JsonObject) {
                        for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                            if (entry.getValue().equals(value)) count++;
                            if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                        }
                    }
                }
                return count;
            }
        }
}
