package cz.coffee.core.utils;

import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.Utils;
import cz.coffee.core.annotation.Used;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static cz.coffee.core.Utils.gsonAdapter;
import static cz.coffee.core.Utils.isNumeric;
import static cz.coffee.core.utils.JsonUtils.Type.KEY;
import static cz.coffee.core.utils.JsonUtils.Type.VALUE;

@Used
abstract public class JsonUtils {
    /**
     * @param input        any {@link JsonElement}
     * @param searchedTerm The expression we are looking for in the json object.
     * @param type         The type we are looking for, either the key or the value
     * @return {@link Boolean}
     */

    public static boolean check(@NotNull JsonElement input, String searchedTerm, @NotNull Type type) {
        JsonElement element;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(input);

        while ((element = elements.pollFirst()) != null) {
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement term : array) {
                    JsonElement parsedElement;
                    try {
                        parsedElement =  new Gson().fromJson(searchedTerm, JsonElement.class);
                    } catch (JsonSyntaxException exception) {
                        parsedElement = new JsonPrimitive(searchedTerm);
                    }
                    if (Objects.equals(term, parsedElement)) return true;
                    elements.offerLast(term);
                }
            } else if (element.isJsonObject()) {
                JsonObject map = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                    if (type == KEY) {
                        if (entry.getKey().equals(searchedTerm)) return true;
                        if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                    } else if (type == VALUE) {
                        JsonElement parsedElement;
                        try {
                            parsedElement =  new Gson().fromJson(searchedTerm, JsonElement.class);
                        } catch (JsonSyntaxException exception) {
                            parsedElement = new JsonPrimitive(searchedTerm);
                        }
                        if (entry.getValue().equals(parsedElement)) return true;
                        elements.offerLast(entry.getValue());
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param primitive any {@link JsonPrimitive} value
     * @return Object.
     */
    public static Object fromPrimitive2Object(JsonElement primitive) {
        return new Gson().fromJson(primitive, Object.class);
    }

    public static Object getSkriptVariable(Object input, Event e) {
        HashMap<String, Object> returnMap = new HashMap<>();
        String stringifyInput = input.toString().replaceAll("[{}$]", "");
        String variableName;
        boolean isLocal = stringifyInput.startsWith("_");
        boolean isTemp = false;
        if (!isLocal) {
            isTemp = stringifyInput.startsWith("-");
        }
        variableName = stringifyInput.replaceAll("[-_]", "");
        if (isTemp) {
            variableName = stringifyInput.replaceAll("_", "");
        }
        Object variable = Variables.getVariable(variableName, e, isLocal);
        returnMap.put(stringifyInput, variable);
        return returnMap;
    }

    public static JsonElement parseVariable(String rawString, Event e) {
        String value;
        Matcher m = Pattern.compile("\\$\\{.+?}").matcher(rawString);
        rawString = rawString.replaceAll("(?<!^)[{}*](?!$)", "").replaceAll("[$]", "Variable.");
        for (Iterator<Object> it = m.results().map(MatchResult::group).map(k -> getSkriptVariable(k, e)).iterator(); it.hasNext(); ) {
            JsonObject unParsedVar = gsonAdapter.toJsonTree(it.next()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> map : unParsedVar.entrySet()) {
                JsonElement json = map.getValue();
                if (json.isJsonObject()) {
                    Stream<String> keys = json.getAsJsonObject().keySet().stream().filter(Objects::nonNull);
                    if (json.getAsJsonObject().keySet().stream().filter(Objects::nonNull).allMatch(Utils::isNumeric)) {
                        JsonArray array = new JsonArray();
                        keys.forEach(k -> array.getAsJsonArray().add(json.getAsJsonObject().get(k)));
                        value = array.toString();
                    } else
                        value = json.getAsString();
                } else {
                    value = json.getAsString();
                }
                assert value != null;
                rawString = rawString.replaceAll("Variable." + map.getKey(), value);
            }
        }
        return JsonParser.parseString(rawString);
    }

    private static JsonObject createMissing(JsonObject input, int number, String nKey) {
        String sanitizeKey = removeLast(nKey, ".list");
        if (!check(input, sanitizeKey, KEY)) {
            if (nKey.endsWith(".list")) {
                input.add(sanitizeKey, new JsonArray());
            } else {
                input.add(sanitizeKey, new JsonObject());
            }
        }
        return input;
    }

    private static JsonArray createMissing(JsonArray input, String nKey, int parsedNumber) {
        boolean sizeOverflow = parsedNumber > (input.size() - 1);
        if (input.isEmpty() || sizeOverflow) {
            if (nKey.endsWith(".list")) {
                input.add(new JsonArray());
            } else {
                input.add(new JsonObject());
            }
        }
        return input;
    }

    /**
     * @param input any {@link JsonElement}
     * @param from  searched expression for its fundamental value.
     * @param to    the final data that will be changed for the given key.
     * @return will return the changed {@link JsonElement}
     */
    public static JsonElement changeJson(@NotNull JsonElement input, @NotNull String[] from, Object to) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().enableComplexMapKeySerialization().create();
        JsonElement element;
        String lastKey = from[from.length - 1];
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(input);

        while ((element = elements.pollFirst()) != null) {
            if (element instanceof JsonObject) {
                JsonObject elementObject = element.getAsJsonObject();
                for (String mapKey : from) {
                    JsonElement value = elementObject.get(mapKey);
                    if (!(value == null || value instanceof JsonNull)) {
                        if (mapKey.equals(lastKey)) {
                            elementObject.remove(mapKey);
                            elementObject.add(lastKey, to instanceof JsonElement ? (JsonElement) to : gson.toJsonTree(to));
                        } else {
                            if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                        }
                    }
                }
            } else if (element instanceof JsonArray) {
                int index;
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    JsonElement value = array.get(i);
                    if (i == (index = Integer.parseInt(lastKey))) {
                        array.set(index, gson.toJsonTree(to));
                        break;
                    } else {
                        if (!(value instanceof JsonPrimitive || value == null || value instanceof JsonNull))
                            elements.offerLast(value);
                    }
                }
            }
        }
        return input;
    }

    /**
     * @param search          equivalent parameter of the search term
     * @param fromSourceInput {@link JsonElement} input
     * @param type            {@link Type}
     * @return count of {@link Integer}
     */
    public static int count(@NotNull String search, @NotNull JsonElement fromSourceInput, Type type) {
        int count = 0;
        JsonElement value;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(fromSourceInput);

        while ((value = elements.pollFirst()) != null) {
            if (value instanceof JsonArray) {
                for (JsonElement l : value.getAsJsonArray()) elements.offerLast(l);
            } else if (value instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                    if (type == KEY) {
                        if (entry.getKey().equals(search)) count++;
                        if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                    } else if (type == VALUE) {
                        JsonElement parsedValue = JsonParser.parseString(search);
                        if (entry.getValue().equals(parsedValue)) count++;
                        elements.offerLast(entry.getValue());
                    }
                }
            }
        }
        return count;
    }

    public static String removeLast(String s, String search) {
        int pos = s.lastIndexOf(search);
        if (pos > -1) return s.substring(0, pos) + s.substring(pos + search.length());
        return s;
    }

    /**
     * @param fromInput  {@link NotNull} {@link JsonElement} loaded directly from json file / variable / json map
     * @param appendData {@link NotNull} {@link JsonElement} customer input's json
     * @param key        searched expression from value:key pair..
     * @param nested     expression what contains a nested path
     * @return changed {@link JsonElement}
     */

    public static JsonElement appendJson(@NotNull JsonElement fromInput, @NotNull JsonElement appendData, String key, String nested) {
        String[] nests = extractKeys(nested, true);
        JsonElement next;
        double lValue = 0;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(fromInput);

        while ((next = elements.pollFirst()) != null) {
            int parsedNumber;
            for (String nestedKey : nests) {
                if (nestedKey.isEmpty()) continue;
                if (isNumeric(nestedKey)) lValue = Double.parseDouble(nestedKey);
                parsedNumber = (int) lValue;

                // creating section
                if (next.isJsonArray()) {
                    JsonArray array = next.getAsJsonArray();
                    next = createMissing(array, nestedKey, parsedNumber);
                } else if (next.isJsonObject()) {
                    JsonObject object = next.getAsJsonObject();
                    next = createMissing(object, parsedNumber, nestedKey);
                }

                // Modding/looping section
                String sanitizeKey = removeLast(nestedKey, ".list");
                if (next.isJsonArray()) {
                    JsonArray array = next.getAsJsonArray();
                    if (array.isEmpty())
                        next = array;
                    else
                        next = array.get(parsedNumber);
                } else if (next.isJsonObject()) {
                    JsonObject object = next.getAsJsonObject();
                    JsonElement value = object.get(sanitizeKey);
                    if (!(value instanceof JsonPrimitive || value instanceof JsonNull)) next = value;
                }

            }
            if (next.isJsonObject()) {
                next.getAsJsonObject().add(key == null ? String.valueOf(next.getAsJsonObject().size()) : key, appendData);
            } else if (next.isJsonArray()) {
                next.getAsJsonArray().add(appendData);
            }
        }

        return fromInput;
    }

    /**
     * <p>
     * json -> "{'A': false, 'B': [1,false,true]}"
     * </p>
     *
     * @param StringInput any string input for example "B[0]"
     * @return new array extract from the given String [B, 0]
     */
    public static String[] extractKeys(String StringInput, boolean... append) {

        if (StringInput == null) {
            return new String[]{};
        }


        boolean isAppend = (append != null && append.length > 0 && append[0]);
        ArrayList<String> arrayList = new ArrayList<>();
        String nestedKey = null, nestedIndex = null;

        final Pattern isSquared = Pattern.compile(".*\\[((\\d+|)])");
        final Pattern subPattern = Pattern.compile("^([^\\[.*]+)");
        final Pattern internalPattern = Pattern.compile("\\[(.*?)]");


        for (String string : StringInput.split(":(?![{}\\[\\]])")) {
            Matcher squares = subPattern.matcher(string);
            Matcher number = internalPattern.matcher(string);
            if (isSquared.matcher(string).find()) {
                while (squares.find()) if (squares.group(1) != null) nestedKey = squares.group(1);
                while (number.find()) if (number.group() != null) nestedIndex = number.group(1);
                else nestedIndex = "0";
                arrayList.add((isAppend ? nestedKey + ".list" : nestedKey));
                arrayList.add(nestedIndex);
            } else {
                arrayList.add(string);
            }
        }
        return arrayList.toArray(new String[0]);
    }

    public static JsonElement deleteNested(String[] nest, JsonElement input, boolean edited, Event event, boolean isObject, Object... data) {
        Object unparsedData = (data != null && data.length > 0 && data[0] != null) ? data[0] : null;
        boolean unparsedJson = data != null && data.length > 0 && data[0] != null;


        JsonElement element;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(input);

        while ((element = elements.pollFirst()) != null) {

            if (element instanceof JsonObject) {
                JsonObject map = element.getAsJsonObject();
                for (String mapKey : nest) {
                    JsonElement value = map.get(mapKey);
                    if (!(value == null || value instanceof JsonNull)) {
                        if (unparsedJson) {
                            if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                        } else {
                            if (mapKey.equals(nest[nest.length - 1])) {
                                map.remove(mapKey);
                            } else {
                                if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                            }
                        }
                    }
                }
            } else if (element instanceof JsonArray) {
                JsonArray list = element.getAsJsonArray();
                for (int j = 0; j < list.size(); j++) {
                    JsonElement value = list.get(j);
                    if (!(value == null || value instanceof JsonNull)) {
                        if (unparsedJson) {
                            if (!isObject) {
                                if (unparsedData instanceof Number w) {
                                    if (w.intValue() <= list.size()) {
                                        list.remove(w.intValue());
                                    }
                                }
                            } else {
                                if (DefaultAdapter.parse(unparsedData, event).equals(value))
                                    list.remove(j);
                                else if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                            }
                        } else if (edited) {
                            if (value.toString().equals(nest[nest.length - 1])) {
                                list.remove(j);
                            } else {
                                if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                            }
                        } else {
                            int lastIndex = -1;
                            if (isNumeric(nest[nest.length - 1]))
                                lastIndex = Integer.parseInt(nest[nest.length - 1]);


                            if (j == lastIndex) {
                                list.remove(j);
                            } else {
                                if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                            }
                        }
                    }
                }
            }
        }
        return input;
    }


    @Used
    public enum Type {
        KEY, VALUE
    }
}