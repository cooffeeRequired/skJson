package cz.coffee.skriptgson.utils;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.adapters.SimpleAdapter;
import org.bukkit.event.Event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

public class GsonUtils {

    public static boolean canCreate(Object stringFile) {
        File f = new File(stringFile.toString());
        return f.getParentFile().exists();
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
        JsonElement next;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(json);
        while ((next = elements.pollFirst()) != null) {
            if (next instanceof JsonArray array) {
                for (JsonElement element : array) {
                    if (Objects.equals(element.toString(), search)) {
                        return true;
                    }
                    elements.offerLast(element);
                }
            } else if (next instanceof JsonObject map) {
                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                    JsonElement value = entry.getValue();
                    if (type == Type.KEY) {
                        if (entry.getKey().equals(search)) return true;
                        if (!value.isJsonPrimitive()) elements.offerLast(value);
                    } else if (type == Type.VALUE) {
                        if (value.equals(JsonParser.parseString(search))) return true;
                        elements.offerLast(value);
                    }
                }
            }
        }
        return false;
    }

    public static JsonElement change(JsonElement json, String from, Object to) {
        JsonElement next;
        ArrayDeque<JsonElement> elements = new ArrayDeque<>();
        boolean isMultiple = false;
        elements.add(json);
        String[] fromList = new String[0];

        if (from.contains(":")) {
            isMultiple = true;
            fromList = from.split(":");
        }

        while ((next = elements.pollFirst()) != null) {
            if (next instanceof JsonObject map) {
                for (Map.Entry<String, JsonElement> m : map.entrySet()) {
                    if (!m.getKey().equals(isMultiple ? fromList[fromList.length - 1] : from)) {
                        elements.offerLast(m.getValue());
                    } else {
                        if (m.getValue() instanceof JsonPrimitive) {
                            if (to instanceof Long l)
                                map.addProperty(m.getKey(), l);
                            else if (to instanceof Integer i)
                                map.addProperty(m.getKey(), i);
                            else if (to instanceof String str)
                                map.addProperty(m.getKey(), str);
                            else if (to instanceof Boolean bool)
                                map.addProperty(m.getKey(), bool);
                            else
                                map.add(m.getKey(), hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(to)));
                        } else {
                            map.add(m.getKey(), hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(to)));
                        }
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


    @SuppressWarnings("unused")
    public static Object fromPrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        } else if (primitive.isNumber()) {
            return primitive.getAsNumber();
        } else if (primitive.isString()) {
            return primitive.getAsString();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static Object toJsonPrimitive(Object Object) {
        if (Object instanceof Integer i)
            return i;
        else if (Object instanceof String str)
            return str;
        else if (Object instanceof Boolean bool)
            return bool;

        return null;
    }

    public static void setVariable(String name, Object element, Event event, boolean isLocal) {
        Variables.setVariable(name, element, event, isLocal);
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

    public static Object getVariable(Event e, String name, boolean isLocal) {
        final Object variable = Variables.getVariable(name, e, isLocal);
        if (variable == null) {
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return variable;
    }

    public enum Type {
        KEY, VALUE
    }

    public static class GsonMapping {
        private static final String SEPARATOR = Variable.SEPARATOR;

        private static void extractNestedObjects(String name, Event event, JsonElement element, boolean isLocal) {
            if (element instanceof JsonObject object) {
                object.keySet().forEach(key -> jsonToList(event, name + SEPARATOR + key, object.get(key), isLocal));
            } else if (element instanceof JsonArray array) {
                for (int index = 0; array.size() > index; index++) {
                    jsonToList(event, name + SEPARATOR + (index + 1), array.get(index), isLocal);
                }
            }
        }

        public static void jsonToList(Event event, String name, JsonElement json, boolean isLocal) {
            JsonElement next;
            Deque<JsonElement> elements = new ArrayDeque<>();
            if (json != null) elements.add(json);

            while ((next = elements.pollFirst()) != null) {
                if (next instanceof JsonObject object) {
                    setVariable(name, object, event, isLocal);
                    extractNestedObjects(name, event, object, isLocal);
                } else if (next instanceof JsonArray array) {
                    setVariable(name, array, event, isLocal);
                    extractNestedObjects(name, event, array, isLocal);
                } else if (next instanceof JsonPrimitive primitive) {
                    setPrimitiveType(name, primitive, event, isLocal);
                } else {
                    setVariable(name, next, event, isLocal);
                }
            }
        }


        public static Object getSkriptVariable(Object input, Event e) {
            boolean isLocal;
            JsonElement newJsonElement;
            JsonObject output = new JsonObject();
            HashMap<String, Object> returnMap = new HashMap<>();
            String name = input.toString().replaceAll("[{}$]", "");
            if (name.startsWith("$_")) {
                isLocal = true;
                name = name.replaceAll("_", "").replaceAll("[$]", "Variable.");
            } else if (name.startsWith("$-")) {
                name = name.replaceAll("-", "").replaceAll("[$]", "Variable.");
                isLocal = false;
            } else {
                name = name.replaceAll("[$]", "Variable.");
                isLocal = false;
            }

            Object variable = Variables.getVariable(name.replaceAll("Variable.", ""), e, isLocal);

            newJsonElement = hierarchyAdapter().toJsonTree(variable);
            if (variable == null)
                newJsonElement = new JsonPrimitive(false);

            output.add("variable", newJsonElement);
            returnMap.put(name, output);

            return returnMap;
        }


        public static JsonElement parseVariable(String rawString, Event e) {
            Matcher m = Pattern.compile("\\$\\{.+?}").matcher(rawString);
            rawString = rawString.replaceAll("(?<!^)[_{}*](?!$)", "").replaceAll("[$]", "Variable.");

            for (Iterator<Object> it = m.results().map(MatchResult::group).map(k -> getSkriptVariable(k, e)).iterator(); it.hasNext(); ) {
                String Value;
                JsonObject object = hierarchyAdapter().toJsonTree(it.next()).getAsJsonObject();
                for (Map.Entry<String, JsonElement> map : object.entrySet()) {
                    JsonObject json = map.getValue().getAsJsonObject();

                    if (json.get("variable") instanceof JsonObject varObject) {
                        Stream<String> keys = varObject.keySet().stream().filter(Objects::nonNull);
                        if (varObject.keySet().stream().filter(Objects::nonNull).allMatch(Utils::isNumeric)) {
                            JsonArray array = new JsonArray();
                            keys.forEach(k -> array.add(json.get("variable").getAsJsonObject().get(k)));
                            Value = array.toString();
                        } else {
                            Value = varObject.toString();
                        }
                    } else {
                        Value = json.get("variable").toString();
                    }
                    rawString = rawString.replaceAll(map.getKey(), Value);
                }
            }
            JsonElement raw = null;
            try {
                raw = JsonParser.parseString(rawString);
            } catch (Exception ex) {
                ex.printStackTrace();
                SkriptGson.debug("rawString: " + rawString);
            }
            return raw;
        }


        public static JsonElement listToJson(Event event, String name, boolean isLocal) {
            JsonElement json = null;

            Object next;
            Deque<Object> objects = new ArrayDeque<>();
            Map<String, Object> variable = (Map<String, Object>) getVariable(event, name + "*", isLocal);
            if (variable == null) return new JsonNull();

            objects.add(variable);
            while ((next = objects.pollFirst()) != null) {
                Stream<String> keys = ((Map<String, Object>) next).keySet().stream().filter(Objects::nonNull);
                if (((Map<String, Object>) next).keySet().stream().filter(Objects::nonNull).allMatch(cz.coffee.skriptgson.utils.Utils::isNumeric)) {
                    JsonArray array = new JsonArray();
                    keys.forEach(key -> {
                        Object nestedVar = getVariable(event, name + key, isLocal);
                        array.add(hierarchyAdapter().toJsonTree(nestedVar));
                    });
                    json = array;
                } else {
                    JsonObject object = new JsonObject();
                    keys.forEach(key -> {
                        Object nestedVar = getVariable(event, name + key, isLocal);
                        object.add(key, hierarchyAdapter().toJsonTree(nestedVar));
                    });
                    json = object;
                }
            }
            return json;
        }
    }

    public static class GsonFileHandler {
        public static void newFile(String fileString, boolean forcing, Object expressionData) {
            GsonErrorLogger err = new GsonErrorLogger();
            File file = new File(fileString);

            if (file.exists()) {
                SkriptGson.warning(err.JSON_FILE_EXISTS);
                return;
            }

            if (forcing) {
                Path fileParents = Paths.get(fileString);
                if (Files.exists(fileParents.getParent())) {
                    SkriptGson.warning(err.PARENT_DIRECTORY_EXISTS);
                    return;
                }

                try {
                    Files.createDirectories(fileParents.getParent());
                } catch (IOException exception) {
                    SkriptGson.warning(err.PARENT_DIRECTORY_EXISTS);
                    return;
                }
            }

            try (var protectedWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                if (expressionData == null) {
                    protectedWriter.nullValue();
                } else if (expressionData instanceof JsonElement element) {
                    protectedWriter.jsonValue(element.toString());
                } else {
                    protectedWriter.jsonValue(expressionData.toString());
                }
                protectedWriter.flush();
            } catch (IOException exception) {
                if (!canCreate(file)) {
                    SkriptGson.warning(err.PARENT_DIRECTORY_NOT_EXIST);
                } else {
                    SkriptGson.warning(exception.getMessage());
                }
            }

        }

        public static JsonElement fromFile(String fileString) {
            GsonErrorLogger err = new GsonErrorLogger();
            JsonElement element = null;
            File file = new File(fileString);
            try (var protectedReader = new JsonReader(new FileReader(file))) {
                element = JsonParser.parseReader(protectedReader);
            } catch (IOException | JsonSyntaxException exception) {
                if (exception instanceof IOException) {
                    if (!file.exists())
                        SkriptGson.warning(err.FILE_NOT_EXIST + fileString);
                } else {
                    SkriptGson.warning(exception.getMessage());
                }
            }
            return element;
        }

        public static void saveToFile(Object expressionData, String fileString) {
            GsonErrorLogger err = new GsonErrorLogger();
            try (var protectedWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(fileString), StandardCharsets.UTF_8))) {
                protectedWriter.setIndent("    ");
                if (expressionData == null) {
                    protectedWriter.nullValue();
                } else if (expressionData instanceof JsonElement element) {
                    String jsonString = hierarchyAdapter().toJson(element);
                    protectedWriter.jsonValue(jsonString);
                } else {
                    protectedWriter.jsonValue(expressionData.toString());
                }
                protectedWriter.flush();
            } catch (IOException exception) {
                if (!(new File(fileString).exists())) {
                    SkriptGson.warning(err.FILE_NOT_EXIST);
                } else {
                    SkriptGson.warning(exception.getMessage());
                }
            }
        }
    }
}
