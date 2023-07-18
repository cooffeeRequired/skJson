package cz.coffee.skjson.skript.base;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.json.ParsedJson;
import cz.coffee.skjson.json.ParsedJsonException;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.njol.skript.lang.Variable.SEPARATOR;
import static ch.njol.skript.variables.Variables.getVariable;
import static cz.coffee.skjson.api.Config.LOGGING_LEVEL;
import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;
import static cz.coffee.skjson.parser.ParserUtil.GsonConverter;
import static cz.coffee.skjson.utils.Util.parseNumber;

@SuppressWarnings({"Unchecked", "unused"})
public abstract class JsonBase {

    @Name("Count values/elements in the Json.")
    @Description("You can get the count values in the given json")
    @Examples({
            "number of key \"test\" in %json%"
    })
    @Since("2.9")
    public static class CountElements extends SimpleExpression<Integer> {

        static {
            Skript.registerExpression(CountElements.class, Integer.class, ExpressionType.SIMPLE, "number of (0:key|1:value) %objects% in %json%");
        }

        private Expression<?> inputObjects;
        private Expression<JsonElement> inputJson;
        private boolean isKey;

        @Override
        protected @Nullable Integer @NotNull [] get(@NotNull Event e) {
            JsonElement json = inputJson.getSingle(e);
            if (json == null) return new Integer[0];
            ParsedJson parsedJson = null;
            try {
                parsedJson = new ParsedJson(json);
            } catch (ParsedJsonException exception) {
                if (LOGGING_LEVEL == 1) Util.error(exception.getLocalizedMessage());
            }
            assert parsedJson != null;

            Object[] unparsedInput = inputObjects.getAll(e);
            final List<Integer> output = new ArrayList<>();
            ParsedJson finalParsedJson = parsedJson;
            CompletableFuture<List<Integer>> ft = CompletableFuture.supplyAsync(() -> {
                Arrays.stream(unparsedInput).forEach(input -> {
                    if (isKey) {
                        if (input instanceof String str) output.add(finalParsedJson.keys(str));
                    } else {
                        JsonElement parsed = ParserUtil.parse(input);
                        assert parsed != null;
                        output.add(finalParsedJson.values(parsed));
                    }
                });
                return output;
            });
            return ft.join().toArray(new Integer[0]);
        }

        @Override
        public boolean isSingle() {
            return inputObjects.isSingle();
        }

        @Override
        public @NotNull Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(inputObjects) + "in" + Classes.getDebugMessage(inputJson);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            isKey = parseResult.mark == 0;
            inputJson = (Expression<JsonElement>) exprs[1];
            inputObjects = LiteralUtils.defendExpression(exprs[0]);
            return LiteralUtils.canInitSafely(inputObjects);
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
            Skript.registerExpression(LoopExpression.class, Object.class, ExpressionType.SIMPLE,
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
                if (PROJECT_DEBUG) Util.error(exception.getLocalizedMessage(), ErrorQuality.NONE, getParser().getNode());
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
            MatchResult numberOfLoop = parseResult.regexes.size() > 0 ? parseResult.regexes.get(0) : null;
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
            Skript.registerExpression(Elements.class, Object.class, ExpressionType.SIMPLE,
                    "(0:(value %-string% of %-json%)|1:(values [%-string%] of %-json%))"
            );
        }

        private boolean isValues;
        private Expression<JsonElement> jsonInput;
        private Expression<String> pathInput;
        private boolean needConvert;
        private Node node;

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
            JsonElement json = null;
            try {
                json = jsonInput.getSingle(e);
            } catch (Exception ignored) {}

            if (json == null) return new Object[0];
            boolean emptyPath = pathInput == null;
            String keys = !emptyPath ? pathInput.getSingle(e) : null;

            LinkedList<String> wrappedKeys = Util.extractKeysToList(keys, Config.PATH_VARIABLE_DELIMITER);
            ParsedJson parsedJson;

            try {
                parsedJson = new ParsedJson(json);
            } catch (ParsedJsonException ex) {
                if (PROJECT_DEBUG) Util.error(ex.getLocalizedMessage(), ErrorQuality.NONE, node);
                return new Object[0];
            }

            if (wrappedKeys == null && (!emptyPath || !isValues)) return new Object[0];

            if (isValues) {
                if (emptyPath) {
                    return needConvert ? new Object[]{json} : getNestedElements(json).toArray(new Object[0]);
                } else {
                    JsonElement jsonResult = parsedJson.byKey(wrappedKeys);
                    if (jsonResult == null) return new Object[0];
                    return needConvert ? new Object[]{jsonResult} : getNestedElements(jsonResult).toArray(new Object[0]);
                }
            } else {
                JsonElement jsonResult = parsedJson.byKey(wrappedKeys);
                Object[] result;
                Object assigned = ParserUtil.from(jsonResult);
                if (assigned == null) {
                    assigned = ParserUtil.jsonToType(jsonResult);
                    if (assigned == null) return new Object[0];
                }
                result = new Object[]{assigned};
                return result;
            }
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
            return (isValues ? "values" : "value") + " of " + jsonInput.toString(e, debug);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            node = getParser().getNode();
            assert node != null;
            final String key = node.getKey();
            assert key != null;
            needConvert = getParser().getCurrentSections(SecLoop.class).size() >= 1 || key.startsWith("loop");

            isValues = parseResult.mark == 1;
            if (isValues) {
                jsonInput = LiteralUtils.defendExpression(exprs[3]);
                pathInput = (Expression<String>) exprs[2];
            } else {
                jsonInput = LiteralUtils.defendExpression(exprs[1]);
                pathInput = (Expression<String>) exprs[0];
            }
            return LiteralUtils.canInitSafely(jsonInput);
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

        static {
            Skript.registerExpression(JsonSupportElement.class, Object.class, ExpressionType.SIMPLE,
                    "(1:(1st|first)|2:(2nd|second)|3:(3rd|third)|4:last|5:%integer%) element of %jsons%"
            );
        }

        private Expression<Integer> intExpression;
        private Expression<JsonElement> jsonInput;
        private Integer tag;

        private static final JsonObject JSON_OBJECT = new JsonObject();

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
                        i = number -1;
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
                            jsonResult = object.get(keys[keys.length-1]);
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
            jsonInput = LiteralUtils.defendExpression(exprs[1]);
            return LiteralUtils.canInitSafely(jsonInput);
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
            Skript.registerEffect(MapJson.class, "[:async] (map|copy) %json/string% to %objects%");
        }

        private Expression<?> jsonInput;
        private VariableString variableString;
        private boolean isLocal;

        private boolean async;

        @Override
        protected void execute(@NotNull Event e) {
            Object jsonInputSingle = jsonInput.getSingle(e);
            JsonElement json = ParserUtil.parse(jsonInputSingle);
            String var = variableString.toString(e).substring(0, variableString.toString().length() - 3);
            if (json == null) return;
            if (async) {
                CompletableFuture.runAsync(() ->toList(var, json, isLocal, e));
            } else {
                toList(var, json, isLocal, e);
            }
        }

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
                            if (PROJECT_DEBUG && LOGGING_LEVEL > 2)Util.log("List-Element: &b" + element + " &fParsed? ->  &a" + parsed);
                            parsed(name + (index + 1), parsed, isLocal, event);
                        }
                    }
                } else if (inputJson instanceof JsonObject map) {
                    map.keySet().stream().filter(Objects::nonNull).forEach(key -> {
                        JsonElement element = map.get(key);
                        Object parsed = ParserUtil.from(element);
                        if (parsed == null) {
                            if (element.isJsonPrimitive()) {
                                primitive(name + key, element.getAsJsonPrimitive(), isLocal, event);
                            } else {
                                toList(name + key + SEPARATOR, element, isLocal, event);
                            }
                        } else {
                            if (PROJECT_DEBUG && LOGGING_LEVEL > 2)Util.log("Map-Element: &e" + element + " &fParsed? ->  &a" + parsed);
                            parsed(name + key, parsed, isLocal, event);
                        }
                    });
                }
            }
        }

        static  <T> void parsed(String name, T object, boolean isLocal, Event event) {
            if (PROJECT_DEBUG && LOGGING_LEVEL > 2)Util.log("&fNAME: &a" + name + "  &fOBJECT: &a" + object + "  &fISLOCAL: &a" + isLocal);
            Variables.setVariable(name, object, event, isLocal);
        }

        static void primitive(String name, JsonPrimitive input, boolean isLocal, Event event) {
            if (name == null || input == null || event == null) return;
            if (input.isBoolean())
                Variables.setVariable(name, input.getAsBoolean(), event, isLocal);
            else if (input.isNumber())
                Variables.setVariable(name, input.getAsNumber(), event, isLocal);
            else if (input.isString())
                Variables.setVariable(name, input.getAsString(), event, isLocal);
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(jsonInput);
        }

        @Override
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            Expression<?> unparsedObject = LiteralUtils.defendExpression(exprs[1]);
            async = parseResult.hasTag("async");
            if (!unparsedObject.getReturnType().isAssignableFrom(JsonElement.class)) {
                Util.error("You can map only Json or stringify json (String)", ErrorQuality.SEMANTIC_ERROR, getParser().getNode());
                return false;
            }
            jsonInput = exprs[0];
            if (unparsedObject instanceof Variable<?> var) {
                if (var.isList()) {
                    isLocal = var.isLocal();
                    variableString = var.getName();
                    return LiteralUtils.canInitSafely(unparsedObject);
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
            PropertyExpression.register(ParseVariable.class, JsonElement.class,"form[atted json]", "jsons");
        }

        private VariableString variable;
        private boolean isLocal;

        @Override
        protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
            String variableName = variable.toString(e);
            String var = (variableName.substring(0, variableName.length() - 1));
            return new JsonElement[]{convert(var, isLocal, true, e)};
        }

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
                    Util.error("Variable need to be a list", ErrorQuality.NONE, getParser().getNode());
                    return false;
                }
            } else {
                Util.error("You need to use a Variable not.. " + objects.getReturnType());
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
            Skript.registerCondition(CondJsonType.class,
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
            return "type of " + inputJson.toString(e, debug);
        }

        @Override
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
            line = matchedPattern;
            mark = parseResult.mark;
            inputJson = LiteralUtils.defendExpression(exprs[0]);
            setNegated(matchedPattern == 1);
            return LiteralUtils.canInitSafely(inputJson);
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
            Skript.registerCondition(CondJsonHas.class,
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
                        final LinkedList<String> list = Util.extractKeysToList(element, Config.PATH_VARIABLE_DELIMITER, true);
                        ParsedJson parsedJson = null;
                        try {
                            parsedJson = new ParsedJson(json);
                        } catch (Exception exception) {
                            if (LOGGING_LEVEL >= 1) Util.error(exception.getLocalizedMessage());
                        }
                        if (parsedJson != null) {
                            final JsonElement result = parsedJson.byKey(list);
                            if (result == null || result.isJsonNull()) {
                                found = false;
                                break;
                            }
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
            return inputJson.toString(e, debug) + " has " + (isValues ? "values" : "keys") + " " + unparsedInput.toString(e, debug);

        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
            directly = parseResult.hasTag("directly");
            line = matchedPattern;
            isValues = parseResult.hasTag("value");
            setNegated(line == 1);
            unparsedInput = LiteralUtils.defendExpression(exprs[1]);
            inputJson = (Expression<JsonElement>) exprs[0];
            if (!isValues) if (unparsedInput.getReturnType() != String.class) return false;
            return LiteralUtils.canInitSafely(unparsedInput);
        }
    }
}