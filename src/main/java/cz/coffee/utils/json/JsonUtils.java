/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils.json;

import ch.njol.skript.variables.Variables;
import com.google.gson.*;
import cz.coffee.utils.SimpleUtil;
import cz.coffee.utils.Type;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.SimpleUtil.isNumeric;
import static cz.coffee.utils.Type.KEY;
import static cz.coffee.utils.Type.VALUE;


/**
 * Class {@link JsonUtils}
 * Since 2.0
 */

@SuppressWarnings("unused")
public class JsonUtils {

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
                JsonArray elementArray = element.getAsJsonArray();
                for (JsonElement term : elementArray) {
                    if (Objects.equals(term.toString(), searchedTerm)) return true;
                    elements.offerLast(term);
                }
            } else if (element.isJsonObject()) {
                JsonObject elementObject = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : elementObject.entrySet()) {
                    if (type == KEY) {
                        if (entry.getKey().equals(searchedTerm)) return true;
                        if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                    } else if (type == Type.VALUE) {
                        JsonElement parsedData = JsonParser.parseString(searchedTerm);
                        if (entry.getValue().equals(parsedData)) return true;
                        elements.offerLast(entry.getValue());
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param Object {@link String} Any expression on which .toString() can be performed
     * @return {@link JsonPrimitive}
     */
    public static JsonElement fromString2JsonElement(Object Object) {
        if (Object instanceof Integer) {
            return new JsonPrimitive((Integer) Object);
        } else if (Object instanceof String) {
            try {
                return JsonParser.parseString((String) Object);
            } catch (JsonSyntaxException exception) {
                return JsonParser.parseString(gsonAdapter.toJson(Object));
            }
        } else if (Object instanceof Boolean)
            return new JsonPrimitive(((Boolean) Object));

        return null;
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
                    if (json.getAsJsonObject().keySet().stream().filter(Objects::nonNull).allMatch(SimpleUtil::isNumeric)) {
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

    private static JsonObject createMissing(JsonObject input, int number, String nKey, boolean debug) {
        String sanitizeKey = nKey.replaceAll(".list", "");
        if (!check(input, sanitizeKey, KEY)) {
            if (nKey.endsWith(".list")) {
                input.add(sanitizeKey, new JsonArray());
            } else {
                input.add(sanitizeKey, new JsonObject());
            }
        }
        return input;
    }

    private static JsonArray createMissing(JsonArray input, String nKey, int parsedNumber, boolean debug) {
        JsonElement el = input.deepCopy();
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

    public static String[] parseNestedPattern(String string, boolean append) {
        final String arrayRegex = ".*\\[((\\d+|)])";
        final Pattern subPattern = Pattern.compile("^([^\\[.*]+)");
        final Pattern internalPattern = Pattern.compile("\\[(.*?)]");
        final Pattern multiSquares = Pattern.compile(".\\[\\d+]");

        ArrayList<String> parsed = new ArrayList<>();
        String[] nests = string.split(":");
        String nKey = "";
        String nInt = "";
        int index = 0;
        for (String n : nests) {
            index++;
            if (n.matches(arrayRegex)) {
                Matcher m = subPattern.matcher(n);
                if (m.find()) {
                    nKey = m.group(1);
                }
                Matcher in = internalPattern.matcher(n);
                if (in.find()) {
                    nInt = Objects.equals(in.group(1), "") ? "0" : in.group(1);
                }
                if (index == 1) {
                    boolean assigned = false;
                    if (nKey != null) {
                        if (!nKey.startsWith("[")) {
                            assigned = true;
                            parsed.add(nKey + ".list");
                            parsed.add(nInt);
                        } else {
                            if (nInt.equals("0")) {
                                parsed.add(nInt + ".list");
                            }
                        }
                    }
                    if (nInt.equals("0") && (!assigned)) {
                        parsed.add("0.list");
                    }
                } else {
                    if (nKey == null) {
                        parsed.add("0.list");
                    } else {
                        if (append) parsed.add(nKey + ".list");
                    }
                    parsed.add(nInt);
                }

            } else {
                parsed.add(n);
            }
        }
        return parsed.toArray(new String[0]);
    }

    /**
     * @param input any {@link JsonElement}
     * @param from  searched expression for its fundamental value.
     * @param to    the final data that will be changed for the given key.
     * @return will return the changed {@link JsonElement}
     */
    public static JsonElement changeJson(@NotNull JsonElement input, @NotNull String from[], Object to) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().enableComplexMapKeySerialization().create();
        JsonElement element;
        String lastKey = from[from.length-1];
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
                        } else {
                            if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                        }
                    }
                }
            } else if (element instanceof JsonArray) {
                int index = 0;
                if (isNumeric(lastKey)) index = Integer.parseInt(lastKey);
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
    public int count(@NotNull String search, @NotNull JsonElement fromSourceInput, Type type) {
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

    /**
     * @param fromInput  {@link NotNull} {@link JsonElement} loaded directly from json file / variable / json map
     * @param appendData {@link NotNull} {@link JsonElement} customer input's json
     * @param key        searched expression from value:key pair..
     * @param nested     expression what contains a nested path
     * @return changed {@link JsonElement}
     */

    public JsonElement appendJson(@NotNull JsonElement fromInput, @NotNull JsonElement appendData, String key, String nested, boolean debug) {
        String[] nests = parseNestedPattern(nested, true);
        JsonElement next;
        boolean isArrayKey, isExist = false;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(fromInput);
        while ((next = elements.pollFirst()) != null) {
            int parsedNumber = 0;
            int n = 0;
            for (String nKey : nests) {
                n++;
                System.out.println(nKey);
                if (isNumeric(nKey)) {
                    double lValue = Double.parseDouble(nKey);
                    parsedNumber = (int) lValue;
                }
                // creating section
                if (next.isJsonArray()) {
                    JsonArray array = next.getAsJsonArray();
                    next = createMissing(array, nKey, parsedNumber, debug);
                } else if (next.isJsonObject()) {
                    JsonObject object = next.getAsJsonObject();
                    next = createMissing(object, parsedNumber, nKey, debug);
                }

                // Modding/looping section
                String sanitizeKey = nKey.replaceAll(".list", "");
                if (next.isJsonArray()) {
                    JsonArray array = next.getAsJsonArray();
                    if (array.isEmpty()) {
                        next = array;
                    } else {
                        next = array.get(parsedNumber);
                    }
                } else if (next.isJsonObject()) {
                    JsonObject object = next.getAsJsonObject();
                    next = object.get(sanitizeKey);
                }

            }

            if (next.isJsonObject()) {
                next.getAsJsonObject().add(key, appendData);
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
    public static String[] extractKeys(String StringInput) {
        ArrayList<String> arrayList = new ArrayList<>();
        String nestedKey = null, nestedIndex = null;

        final Pattern isSquared = Pattern.compile(".*\\[((\\d+|)])");
        final Pattern subPattern = Pattern.compile("^([^\\[.*]+)");
        final Pattern internalPattern = Pattern.compile("\\[(.*?)]");


        for (String string : StringInput.split(":")) {
            Matcher squares = subPattern.matcher(string);
            Matcher number = internalPattern.matcher(string);
            if (isSquared.matcher(string).find()) {
                while (squares.find()) if (squares.group(1) != null) nestedKey = squares.group(1);
                while (number.find()) if (number.group() != null) nestedIndex = number.group(1);
                else nestedIndex = "0";
                arrayList.add(nestedKey);
                arrayList.add(nestedIndex);
            } else {
                arrayList.add(string);
            }
        }
        return arrayList.toArray(new String[0]);
    }

    public static boolean isClassicType(Object o) {
        return o instanceof String || o instanceof Number || o instanceof Boolean;
    }

    public static JsonElement deleteNested(String[] nest, JsonElement input) {
        JsonElement element;
        Deque<JsonElement> elements = new ArrayDeque<>();
        elements.add(input);

        while ((element = elements.pollFirst()) != null) {
            if (element instanceof JsonObject) {
                JsonObject map = element.getAsJsonObject();
                String[] mapKeys = map.keySet().toArray(new String[0]);
                for (String mapKey : nest) {
                    JsonElement value = map.get(mapKey);
                    if (!(value == null || value instanceof JsonNull)) {
                        if (mapKey.equals(nest[nest.length - 1])) {
                            map.remove(mapKey);
                        } else {
                            if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                        }
                    }
                }
            } else if (element instanceof JsonArray) {
                JsonArray list = element.getAsJsonArray();
                for (int j = 0; j < list.size(); j++) {
                    JsonElement value = list.get(j);
                    if (!(value == null || value instanceof JsonNull)) {
                        int lastIndex = -1;
                        if (isNumeric(nest[nest.length - 1])) {
                            lastIndex = Integer.parseInt(nest[nest.length - 1]);
                        }
                        if (j == lastIndex) {
                            list.remove(j);
                        } else {
                            if (!(value instanceof JsonPrimitive)) elements.offerLast(value);
                        }
                    }
                }
            }
        }
        return input;
    }
}
