package cz.coffee.skjson.skript.base;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.api.FileHandler;
import cz.coffee.skjson.json.JsonParser;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.PatternUtil;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.njol.skript.lang.Variable.SEPARATOR;
import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;
import static ch.njol.skript.variables.Variables.getVariable;
import static cz.coffee.skjson.api.ConfigRecords.*;
import static cz.coffee.skjson.parser.ParserUtil.*;
import static cz.coffee.skjson.utils.Logger.*;
import static cz.coffee.skjson.utils.PatternUtil.convertStringToKeys;
import static cz.coffee.skjson.utils.Util.parseNumber;

@SuppressWarnings({"Unchecked", "unused"})
public abstract class JsonBase {

    @Name("Count values/elements in the Json.")
    @Description({
            "You can get the number of values or keys in the given json",
            "",
            "**Explanatory notes**:",
            "\t > `<json>`: represent a placeholder for your json e.g. `{_json}`"
    })
    @Examples({
            "number of keys \"test\" and \"something\" in <json>",
            "number of key \"test\" in <json>"
    })
    @ApiStatus.Experimental
    @Since("2.9.9-pre (Api change)")
    public static class CountElements extends SimpleExpression<Integer> {
        static {
            SkJsonElements.registerExpression(CountElements.class, Integer.class, ExpressionType.SIMPLE,
                    "number of (0:key[s]|1:value[s]) %objects% in %json%"
            );
        }

        private Expression<?> inputValues;
        private Expression<JsonElement> expressionJson;
        private boolean isKey;

        @Override
        protected @Nullable Integer @NotNull [] get(@NotNull Event e) {
            JsonElement json = expressionJson.getSingle(e);
            if (json == null) return new Integer[0];
            Collection<?> unparsedValues = Collections.singleton(inputValues.getAll(e));
            final List<Integer> counts = new ArrayList<>();

            unparsedValues.forEach((value) -> {
                if (isKey) {
                    if (value instanceof String str) counts.add(JsonParser.count(json).keys(str));
                } else {
                    var parsedValue = parse(value);
                    counts.add(JsonParser.count(json).values(parsedValue));
                }
            });
            return counts.toArray(Integer[]::new);
        }

        @Override
        public boolean isSingle() {
            return inputValues.isSingle();
        }

        @Override
        public @NotNull Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(inputValues) + "in" + Classes.getDebugMessage(expressionJson);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            isKey = parseResult.mark == 0;
            expressionJson = (Expression<JsonElement>) exprs[1];
            inputValues = defendExpression(exprs[0]);
            return canInitSafely(inputValues);
        }
    }

    /**
     * The type Loop expression.
     */
    @Name("Loops")
    @Description({
            "loops of values/key for json",
            "",
            "<b>json-value, json-key</b>"
    })
    @Since("2.9")
    @Examples({
            "on script load:",
            "\tset {_json} to json from location(10,1,1)",
            "\tloop values of {_json}:",
            "\t\tsend json-value, json-key",
            "",
    })
    public static class LoopExpression extends SimpleExpression<Object> {
        static {
            SkJsonElements.registerExpression(LoopExpression.class, Object.class, ExpressionType.SIMPLE,
                    "[the] json-(:value|:key)[-<(\\d+)>]"
            );
        }

        private boolean isKey;
        private String name;
        private SecLoop loop;
        private boolean isCanceled = false;


        @Override
        @SuppressWarnings("unchecked")
        protected @Nullable Object @NotNull [] get(@NotNull Event e) {
            if (isCanceled) return new Object[0];

            WeakHashMap<String, Object> outputMap;
            try {
                outputMap = (WeakHashMap<String, Object>) loop.getCurrent(e);
            } catch (ClassCastException exception) {
                if (PROJECT_DEBUG) error(exception);
                return new Object[0];
            }

            if (outputMap == null) return new Object[0];

            for (Map.Entry<String, Object> entry : outputMap.entrySet()) {
                if (isKey) return new String[]{entry.getKey()};
                Object[] first = (Object[]) Array.newInstance(getReturnType(), 1);
                if (entry.getValue() instanceof JsonElement element) {
                    Object assignedValue = ParserUtil.parse(element);
                    if (assignedValue == null) assignedValue = ParserUtil.jsonToType(element);
                    first[0] = assignedValue;
                } else {
                    first[0] = entry.getValue();
                }
                return first;
            }
            return new Object[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            if (loop == null) return Object.class;
            return loop.getLoopedExpression().getReturnType();
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            if (e == null) return name;
            return Classes.getDebugMessage(loop.getCurrent(e));
        }

        @Override
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            MatchResult numberOfLoop = !parseResult.regexes.isEmpty() ? parseResult.regexes.get(0) : null;
            Object group = 0;
            if (numberOfLoop != null) group = numberOfLoop.group(0);
            int i = 0;
            isKey = parseResult.hasTag("key");
            String firstField = parseResult.expr, s = "";
            Pattern pattern = Pattern.compile("json-(.+)(.)");
            Matcher matchingPattern = pattern.matcher(firstField);

            if (matchingPattern.matches()) {
                String[] split = firstField.split("-");
                s = split[1];
                i = parseNumber(group);
            }
            Class<?> inputClass = Classes.getClassFromUserInput(s);
            name = s;
            int j = 1;
            SecLoop loop = null;
            for (SecLoop l : getParser().getCurrentSections(SecLoop.class)) {
                if ((inputClass != null
                        && inputClass.isAssignableFrom(l.getLoopedExpression().getReturnType()))
                        || l.getLoopedExpression().isLoopOf("value")
                ) {
                    if (j < i) {
                        j++;
                        continue;
                    }
                    if (loop != null) {
                        isCanceled = true;
                        break;
                    }
                    loop = l;
                    if (j == i) break;
                }
            }

            if (loop == null) {
                Skript.error("There's no loop that matches json-" + s + "'", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
            if (isCanceled) {
                Skript.error("There are multiple loops that match json-" + s + ". Use json-" + s + "-1/2/3/etc. to specify witch loop's value you want.", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
            this.loop = loop;
            return true;
        }

        public boolean isLoopOf(@NotNull String s) {
            return false;
        }
    }

    /**
     * The type Elements.
     */
    @Name("Values of Json")
    @Description("Values/Key of Json")
    @Since("2.9")
    @Examples({
            "on script load:",
            "\tset {_json} to json from location(10,1,1)",
            "\tsend values of {_json}",
            "\tsend value \"world\" of {_json}",
            "",
            "Checkout <b> Loops </b>"
    })
    public static class Elements extends SimpleExpression<Object> {
        static {
            SkJsonElements.registerExpression(Elements.class, Object.class, ExpressionType.SIMPLE,
                    "(0:(value %-string% of %-json%)|1:(values [%-string%] of %-json%))"
            );
        }

        private boolean isValues;
        private Expression<JsonElement> jsonInput;
        private Expression<String> pathInput;
        private boolean needConvert;

        public static LinkedList<Object> getNestedElements(JsonElement current) {
            LinkedList<Object> results = new LinkedList<>();
            if (current == null || current.isJsonPrimitive() || current.isJsonNull()) return null;
            if (current.isJsonObject()) {
                ((JsonObject) current).entrySet().forEach(entry -> {
                    JsonElement value = entry.getValue();
                    if (value != null) {
                        Object assign = ParserUtil.from(value);
                        results.add(value.isJsonPrimitive() ? ParserUtil.jsonToType(value) : assign == null ? value : assign);
                    }
                });
            } else if (current.isJsonArray()) {
                ((JsonArray) current).forEach(value -> {
                    if (value != null) {
                        Object assign = ParserUtil.from(value);
                        results.add(value.isJsonPrimitive() ? ParserUtil.jsonToType(value) : assign == null ? value : assign);
                    }
                });
            }
            return results;
        }

        @Override
        protected @Nullable Object @NotNull [] get(@NotNull Event e) {
            try {

                JsonElement json = null;
                try {
                    json = jsonInput.getSingle(e);
                } catch (Exception ignored) {
                }
                if (json == null) return new Object[0];
                boolean emptyPath = pathInput == null;
                String keys = !emptyPath ? pathInput.getSingle(e) : null;

                Deque<PatternUtil.keyStruct> wrappedKeys = convertStringToKeys(keys);
                if (wrappedKeys.isEmpty() && (!emptyPath || !isValues)) return new Object[0];

                if (isValues) {
                    if (emptyPath) {
                        return needConvert ? new Object[]{json} : getNestedElements(json).toArray(new Object[0]);
                    } else {
                        JsonElement jsonResult = JsonParser.search(json).key(wrappedKeys);
                        if (jsonResult == null) return new Object[0];
                        return needConvert ? new Object[]{jsonResult} : getNestedElements(jsonResult).toArray(new Object[0]);
                    }
                } else {
                    JsonElement jsonResult = JsonParser.search(json).key(wrappedKeys);
                    Object[] result;
                    Object assigned = ParserUtil.from(jsonResult);
                    if (assigned == null) {
                        assigned = ParserUtil.jsonToType(jsonResult);
                        if (assigned == null) return new Object[0];
                    }
                    result = new Object[]{assigned};
                    return result;
                }
            } catch (Exception ex) {
                if (ex.getMessage().contains("Cannot invoke \"java.util.LinkedList.toArray(Object[])\"")) {
                    warn("Incorrect json format %s", jsonInput.toString(e, true));
                }
            }
            return new Object[0];
        }

        @Override
        public boolean isSingle() {
            return !isValues;
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public boolean isLoopOf(@NotNull String s) {
            return s.equals("value") || s.equals("key");
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return (isValues ? "values" : "value") + " of " + jsonInput.toString(e, debug);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            try {
                Node node = getParser().getNode();
                assert node != null;
                final String key = node.getKey();
                assert key != null;
                needConvert = !getParser().getCurrentSections(SecLoop.class).isEmpty() || key.startsWith("loop");
            } catch (Exception ex) {
                warn("Any loop key or object key doesn't exist, please check your syntax!");
            }
            isValues = parseResult.mark == 1;
            if (isValues) {
                jsonInput = defendExpression(exprs[3]);
                pathInput = (Expression<String>) exprs[2];
            } else {
                jsonInput = defendExpression(exprs[1]);
                pathInput = (Expression<String>) exprs[0];
            }
            return canInitSafely(jsonInput);
        }

        @Override
        public @Nullable Iterator<?> iterator(@NotNull Event e) {
            Object object = null;
            JsonElement finalObject;

            Iterator<?> superIterator = super.iterator(e);

            if (superIterator == null) return null;
            if (superIterator.hasNext()) object = superIterator.next();

            if (!(object instanceof JsonElement))
                return null;
            else {
                finalObject = (JsonElement) object;
            }
            return new Iterator<>() {
                int index = 0;

                @Override
                public boolean hasNext() {
                    if (finalObject.isJsonArray()) {
                        JsonArray array = finalObject.getAsJsonArray();
                        return index < array.size();
                    } else if (finalObject.isJsonObject()) {
                        JsonObject json = finalObject.getAsJsonObject();
                        return index < json.keySet().size();
                    }
                    return false;
                }

                @Override
                public Object next() {
                    if (finalObject.isJsonArray()) {
                        final WeakHashMap<String, Object> weak = new WeakHashMap<>();
                        JsonArray array = finalObject.getAsJsonArray();
                        weak.put(String.valueOf(index), ParserUtil.jsonToType(array.get(index)));
                        index++;
                        return weak;
                    } else if (finalObject.isJsonObject()) {
                        JsonObject json = finalObject.getAsJsonObject();
                        Set<String> keys = json.keySet();
                        final WeakHashMap<String, Object> weak = new WeakHashMap<>();
                        String declaredKey = keys.toArray(new String[0])[index];
                        weak.put(declaredKey, ParserUtil.jsonToType(json.get(declaredKey)));
                        index++;
                        return weak;
                    }
                    return null;
                }
            };
        }
    }

    /**
     * The type Json support element.
     */
    @Name("Literals")
    @Description("Represent sort of literals for skJson")
    @Since("2.9")
    @Examples({
            "on script load:",
            "\tset {_json} to json from location(10,1,1)",
            "\tsend first element of {_json}",
            "\tsend last element of {_json}",
            "\tsend 5 element of {_json}",
            "\tsend 3rd element of {_json}",
            "",
    })
    public static class JsonSupportElement extends SimpleExpression<Object> {

        public static final int lastElementConst = -928171;
        private static final JsonObject JSON_OBJECT = new JsonObject();

        static {
            SkJsonElements.registerExpression(JsonSupportElement.class, Object.class, ExpressionType.SIMPLE,
                    "(1:(1st|first)|2:(2nd|second)|3:(3rd|third)|4:last|5:%integer%) element of %jsons%"
            );
        }

        private Expression<Integer> intExpression;
        private Expression<JsonElement> jsonInput;
        private Integer tag;

        @Override
        @SuppressWarnings("all")
        protected @Nullable Object @NotNull [] get(Event e) {
            final JsonElement[] jsons = jsonInput.getAll(e);

            int i = -9139;
            switch (tag) {
                case 1 -> i = 0;
                case 2 -> i = 1;
                case 3 -> i = 2;
                case 4 -> i = lastElementConst;
                case 5 -> {
                    if (intExpression != null) {
                        int number = intExpression.getSingle(e);
                        i = number - 1;
                    }
                }
            }

            JsonElement jsonResult = null;
            for (JsonElement json : jsons) {
                try {
                    if (json.isJsonArray()) {
                        if (i == lastElementConst) {
                            jsonResult = ((JsonArray) json).get(-1);
                        } else {
                            jsonResult = ((JsonArray) json).get(i);
                        }
                    } else if (json.isJsonObject()) {
                        JsonObject object = (JsonObject) json;
                        if (i == lastElementConst) {
                            String[] keys = object.keySet().toArray(new String[0]);
                            jsonResult = object.get(keys[keys.length - 1]);
                        } else {
                            int c = 0;
                            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                                if (c == i) {
                                    jsonResult = entry.getValue();
                                }
                                c++;
                            }
                        }
                    }
                    Object assigned = ParserUtil.from(jsonResult);
                    if (assigned == null) {
                        assigned = ParserUtil.jsonToType(jsonResult);
                        if (assigned == null) return new Object[0];
                        return new Object[]{assigned};
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return new Object[0];
        }

        @Override
        public boolean isSingle() {
            return jsonInput.isSingle();
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return switch (tag) {
                case 1 -> "first";
                case 2 -> "second";
                case 3 -> "third";
                case 4 -> "last";
                case 5 -> intExpression.toString(e, debug);
                default -> "";
            } + "element of " + jsonInput.toString(e, debug);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
            tag = parseResult.mark;
            intExpression = (Expression<Integer>) exprs[0];
            jsonInput = defendExpression(exprs[1]);
            return canInitSafely(jsonInput);
        }
    }

    @Name("Get index of key/value in ListObject")
    @Description({
            "Returns the index of the key/value in the ListObject",
            "What is ListObject? ListObject is shortcut for `[{}, {} ...]`",
            "That means the object indexed by integer in the list",
            "This expressions allows you found the key/value in the inner objects in the list."
    })
    @Examples({})
    @Since("2.9")
    public static class IndexListObject extends SimpleExpression<Integer> {

        static {
            SkJsonElements.registerExpression(IndexListObject.class, Integer.class, ExpressionType.SIMPLE,
                    "[get] index of (:key|:value) %object% in [object( |-)list] [%-string%] of [json] %json%"
            );
        }

        private Expression<Integer> integerExpression;
        private Expression<JsonElement> jsonElementExpression;
        private Expression<String> pathExpression;
        private Expression<?> inputExpression;

        @Override
        protected @Nullable Integer @NotNull [] get(@NotNull Event e) {
            Object input = inputExpression.getSingle(e);

            info("Executed...");
            return new Integer[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return "get a index of key/value " + inputExpression.toString(e, debug) + " in " + pathExpression.toString(e, debug) + " of " + jsonElementExpression.toString(e, debug);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            boolean isValue = parseResult.hasTag("value");
            inputExpression = defendExpression(exprs[0]);
            pathExpression = (Expression<String>) exprs[1];
            jsonElementExpression = (Expression<JsonElement>) exprs[2];
            return canInitSafely(inputExpression);
        }
    }

    @Name("Json to Skript variable list")
    @Description("Its allow convert Json to variable skript list")
    @Examples({
            "on script load:",
            "\tset {_json} to json from file \"plugins/skript/#.json\"",
            "\tmap {_json} to {_json::*}"
    })
    @Since("1.9, 2.9 - Support mapping json from functions")
    public static class MapJson extends Effect {

        static {
            SkJsonElements.registerEffect(MapJson.class, "[:async] (map|copy) %json/string% to %objects%");
        }

        private Expression<?> jsonInput;
        private VariableString variableString;
        private boolean isLocal;
        private boolean async;

        private static void toList(@NotNull String name, JsonElement inputJson, boolean isLocal, Event event) {
            if (inputJson.isJsonPrimitive()) {
                primitive(name, inputJson.getAsJsonPrimitive(), isLocal, event);
            } else if (inputJson.isJsonObject() || inputJson.isJsonArray()) {
                if (inputJson instanceof JsonArray list) {
                    for (int index = 0; index < list.size(); index++) {
                        JsonElement element = list.get(index);
                        Object parsed = ParserUtil.from(element);
                        if (parsed == null) {
                            if (element.isJsonPrimitive()) {
                                primitive(name + (index + 1), element.getAsJsonPrimitive(), isLocal, event);
                            } else {
                                toList(name + (index + 1) + SEPARATOR, element, isLocal, event);
                            }
                        } else {
                            if (PROJECT_DEBUG && LOGGING_LEVEL > 2)
                                info("List-Element &b %s &fParsed? -> &a %s", element, parsed);
                            parsed(name + (index + 1), parsed, isLocal, event);
                        }
                    }
                } else if (inputJson instanceof JsonObject map) {
                    map.keySet().stream().filter(Objects::nonNull).forEach(key -> {
                        try {
                            JsonElement element = map.get(key);
                            Object parsed = ParserUtil.from(element);
                            // parsed means that return a parsed value from BUKKIT/SKRIPT Objects
                            if (parsed == null) {
                                if (element.isJsonPrimitive()) {
                                    primitive(name + key, element.getAsJsonPrimitive(), isLocal, event);
                                } else {
                                    toList(name + key + SEPARATOR, element, isLocal, event);
                                }
                            } else {
                                parsed(name + key, parsed, isLocal, event);
                            }
                        } catch (Exception e) {
                            error(e);
                        }
                    });
                }
            }
        }

        static <T> void parsed(String name, T object, boolean isLocal, Event event) {
            Variables.setVariable(name, object, event, isLocal);
        }

        static void primitive(String name, JsonPrimitive input, boolean isLocal, Event event) {
            if (name == null || input == null || event == null) return;
            Object o = jsonToType(defaultConverter(input));
            if (o != null) {
                if (PROJECT_DEBUG && LOGGING_LEVEL > 2) info(o.getClass());
            }
            assert o != null;
            Variables.setVariable(name, o, event, isLocal);
        }

        @Override
        protected void execute(@NotNull Event e) {
            Object jsonInputSingle = jsonInput.getSingle(e);
            JsonElement json = ParserUtil.parse(jsonInputSingle);
            String vv = variableString.getSingle(e);
            String var = vv.substring(0, vv.length() - 3);
            if (json == null) return;
            if (async) {
                CompletableFuture.runAsync(() -> toList(var + SEPARATOR, json, isLocal, e));
            } else {
                toList(var + SEPARATOR, json, isLocal, e);
            }
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(jsonInput);
        }

        @Override
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            Expression<?> unparsedObject = defendExpression(exprs[1]);
            async = parseResult.hasTag("async");
            if (!unparsedObject.getReturnType().isAssignableFrom(JsonElement.class)) {
                simpleError("You can map only Json or stringify json (String)", getParser().getNode());
                return false;
            }
            jsonInput = exprs[0];
            if (unparsedObject instanceof Variable<?> var) {
                if (var.isList()) {
                    isLocal = var.isLocal();
                    variableString = var.getName();
                    return canInitSafely(unparsedObject);
                }
            }
            return false;
        }
    }

    @Name("Skript variable to Json")
    @Description("Its allow convert Skript list variable to Json")
    @Examples({
            "on script load:",
            "\tset {_json::A::1} to false",
            "\tset {_json::A::2} to true",
            "\tset {_json::B::some} to \"some great value\"",
            "\tsend {_json::*}'s form"
    })
    @Since("1.3.0")
    @SuppressWarnings("unchecked")
    public static class ParseVariable extends SimpleExpression<JsonElement> {

        static {
            SkJsonElements.registerPropertyExpression(ParseVariable.class, JsonElement.class, "form[atted json]", "jsons");
        }

        private VariableString variable;
        private boolean isLocal;

        private static JsonElement convert(String var, boolean isLocal, boolean nullable, Event event) {
            Map<String, Object> variable = (Map<String, Object>) Variables.getVariable(var + "*", event, isLocal);
            if (variable == null) return nullable ? null : new JsonObject();
            List<String> checkKeys = variable.keySet().stream().filter(Objects::nonNull).filter(f -> !f.equals("*")).toList();

            if (checkKeys.stream().allMatch(Util::isNumber)) {
                if (Util.isIncrement(checkKeys.toArray())) {
                    JsonArray structure = new JsonArray();
                    checkKeys.forEach(key -> {
                        Object value = subNode(var + key, isLocal, event);
                        JsonElement data = GsonConverter.toJsonTree(value);
                        if (value instanceof JsonPrimitive) {
                            JsonElement primitive = ParserUtil.defaultConverter(value);
                            if (Util.isNumber(primitive)) {
                                structure.add(ParserUtil.defaultConverter(value));
                            } else {
                                structure.add(primitive);
                            }
                        } else {
                            structure.add(data);
                        }
                    });
                    return structure;
                } else {
                    JsonObject structure = new JsonObject();
                    checkKeys.forEach(key -> {
                        JsonElement data = GsonConverter.toJsonTree(subNode(var + key, isLocal, event));
                        if (data instanceof JsonPrimitive primitive) {
                            structure.add(key, primitive);
                        } else {
                            structure.add(key, data);
                        }
                    });
                    return structure;
                }
            } else {
                JsonObject structure = new JsonObject();
                checkKeys.forEach(key -> {
                    JsonElement data = GsonConverter.toJsonTree(subNode(var + key, isLocal, event));
                    if (data instanceof JsonPrimitive) {
                        JsonPrimitive primitive = data.getAsJsonPrimitive();
                        structure.add(key, primitive);
                    } else {
                        structure.add(key, data);
                    }
                });
                return structure;
            }
        }

        static Object subNode(String name, boolean isLocal, Event event) {
            Object variable = getVariable(name, event, isLocal);
            if (variable == null) {
                variable = convert(name + SEPARATOR, isLocal, false, event);
            } else if (variable == Boolean.TRUE) {
                Object subVariable = convert(name + SEPARATOR, isLocal, true, event);
                if (subVariable != null) {
                    variable = subVariable;
                }
            }

            if (!(variable instanceof String || variable instanceof Number || variable instanceof Boolean || variable instanceof JsonElement || variable instanceof Map || variable instanceof List)) {
                if (variable != null) variable = ParserUtil.parse(variable);
            }
            return variable;
        }

        @Override
        protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
            String variableName = variable.toString(e);
            String var = (variableName.substring(0, variableName.length() - 1));
            return new JsonElement[]{convert(var, isLocal, true, e)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<? extends JsonElement> getReturnType() {
            return JsonElement.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return "form of " + variable;
        }

        @Override
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            Expression<?> objects = exprs[0];
            if (objects instanceof Variable<?> var) {
                if (var.isList()) {
                    isLocal = var.isLocal();
                    variable = var.getName();
                } else {
                    simpleError("Variable need to be a list", getParser().getNode());
                    return false;
                }
            } else {
                simpleError("You need to use a Variable not.. " + objects.getReturnType());
                return false;
            }
            return true;
        }
    }

    @Name("Type of Json")
    @Since("2.7")
    @Examples({
            "set {_j} to json from \"{data: {}}\"",
            "if type of {_j} is json object"
    })
    @Description("You can get type of given Json.")
    public static class CondJsonType extends Condition {

        static {
            SkJsonElements.registerCondition(CondJsonType.class,
                    "type of %json% (is|=) (1:primitive|2:json object|3:json array)",
                    "type of %json% (is(n't| not)|!=) (1:primitive|2:json object|3:json array)"
            );
        }

        private int line, mark;
        private Expression<JsonElement> inputJson;

        @Override
        public boolean check(@NotNull Event e) {
            final JsonElement json = inputJson.getSingle(e);
            if (json == null) return false;
            if (!json.isJsonNull()) {
                if (line == 0) {
                    if (mark == 1) return json.isJsonPrimitive();
                    if (mark == 2) return json.isJsonObject();
                    if (mark == 3) return json.isJsonArray();
                }
            }
            return false;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return "type of " + inputJson.toString(e, debug);
        }

        @Override
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
            line = matchedPattern;
            mark = parseResult.mark;
            inputJson = defendExpression(exprs[0]);
            setNegated(matchedPattern == 1);
            return canInitSafely(inputJson);
        }
    }

    @Name("Json has value/keys")
    @Description("You can check if the inserted keys or values already in your specified json")
    @Examples({
            "on script load:",
            "\tset {_json} to json from string \"{'test5': [1], 'test6': ['key', 'key2', 'key3']}\"",
            "\tif {_json} has keys \"test5\", \"test6\", \"A\":",
            "\t\tsend true"
    })
    @Since("2.8.0")
    public static class CondJsonHas extends Condition {

        static {
            SkJsonElements.registerCondition(CondJsonHas.class,
                    "%json% has [:directly] (:value|:key)[s] %objects%",
                    "%json% does(n't| not) have [:directly] (:value|:key)[s] %objects%"
            );
        }

        private int line;
        private boolean isValues, directly;
        private Expression<?> unparsedInput;
        private Expression<JsonElement> inputJson;

        @Override
        public boolean check(@NotNull Event e) {
            JsonElement json = inputJson.getSingle(e);
            Object[] values = unparsedInput.getAll(e);
            if (json == null) return false;
            boolean found = true;

            for (Object value : values) {
                if (isValues) {
                    JsonElement element = ParserUtil.parse(value);
                    if (!ParserUtil.checkValues(element, json)) {
                        found = false;
                        break;
                    }
                } else {
                    String element = (String) value;
                    if (directly) {
                        final Queue<PatternUtil.keyStruct> list = convertStringToKeys(element, PATH_VARIABLE_DELIMITER, true);
                        final JsonElement result = JsonParser.search(json).key(list);
                        if (result == null || result.isJsonNull()) {
                            found = false;
                            break;
                        }
                    } else {
                        if (!ParserUtil.checkKeys(element, json)) {
                            found = false;
                            break;
                        }
                    }
                }
            }
            return (line == 0) == found;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return inputJson.toString(e, debug) + " has " + (isValues ? "values" : "keys") + " " + unparsedInput.toString(e, debug);

        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
            directly = parseResult.hasTag("directly");
            line = matchedPattern;
            isValues = parseResult.hasTag("value");
            setNegated(line == 1);
            unparsedInput = defendExpression(exprs[1]);
            inputJson = (Expression<JsonElement>) exprs[0];
            return canInitSafely(unparsedInput);
        }
    }

    @Name("All json files in directory")
    @Description("You can get multiple file from directory and load that as json")
    @Examples({
            "on script load:",
            "\tset {_jsons::*} to all json file from dir \"./plugins/test\"",
            "\t#Or you can loop trough that",
            "\tloop all json file from dir \"./plugins/test\":",
            "\t\tsend loop-file",
            "\t\tsend json from file loop-file",
    })
    @Since("2.9.7")
    public static class AllJsonInFolder extends SimpleExpression<String> {

        static {
            SkJsonElements.registerExpression(AllJsonInFolder.class, String.class, ExpressionType.SIMPLE,
                    "All json [files] (from|in) (dir|directory|folder) %string%"
            );
        }

        private Expression<String> directoryInputString;

        @Override
        protected String @NotNull [] get(@NotNull Event event) {
            var inputDirectory = directoryInputString.getSingle(event);
            ArrayList<String> store = new ArrayList<>(Arrays.asList(FileHandler.walkDirectory(inputDirectory).join()));
            return store.toArray(String[]::new);
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public @NotNull Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            assert event != null;
            return "All json files from directory " + directoryInputString.toString(event, b);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
            directoryInputString = (Expression<String>) expressions[0];
            return true;
        }

        @Override
        public boolean isLoopOf(@NotNull String s) {
            return s.equals("file");
        }
    }
}
