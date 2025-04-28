package cz.coffee.skjson.skript.changer;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.skjson.SkJsonElements;
import cz.coffee.skjson.json.JsonParser;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.skript.base.JsonBase;
import cz.coffee.skjson.utils.PatternUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static cz.coffee.skjson.api.ConfigRecords.*;
import static cz.coffee.skjson.parser.ParserUtil.GsonConverter;
import static cz.coffee.skjson.utils.Logger.error;
import static cz.coffee.skjson.utils.PatternUtil.convertStringToKeys;

@SuppressWarnings("unused")
public abstract class SkJsonChanger {

    public static <V> List<JsonElement> parseAliases(V value) {
        LinkedList<JsonElement> output = new LinkedList<>();
        if ((value instanceof ItemType type) && type.getTypes().size() > 1) {
            type.getTypes().forEach(data -> output.add(ParserUtil.parse(data)));
        }
        return output;
    }

    @SuppressWarnings("unused")
    @Since("2.9")
    @Name("Changer - JsonArray set/add/remove/removeAll")
    @Description({"The new documentation you will find here: https://skjsonteam.github.io/skJsonDocs/beta/defaults", "A very general effect that can change many json array. The json object can be only add/set/remove/removeAll"})
    @Examples({
            "set value of json list \"1::list[1]::data\" in {_json} to \"[]\"",
            "remove values 1 and \"Hello true\" of json list from {_json}",
            "remove 2nd element of json list from {_json}",
            "add diamond sword to json list \"0::list\" in {_json}",
            "remove all \"new test key\" of json list \"array[0]\" from {_json}"
    })
    public static class JsonArrayChanger extends SimpleExpression<Object> {

        static {
            SkJsonElements.registerExpression(JsonArrayChanger.class, Object.class, ExpressionType.SIMPLE,
                    "json (list|array) [%-string%] in %jsons%",
                    "(1:value|2:key) of json (list|array) %string% in %json%",
                    "[value[s]] %objects% of json (list|array) [%-string%]",
                    "(1:(1st|first)|2:(2nd|second)|3:(3rd|third)|4:last|5:%-integer%) element of json (list|array) [%-string%]"
            );
        }

        private ParseResult result;
        private int line, tag;
        private Expression<JsonElement> inputJsonExpression;
        private Expression<String> pathExpression;
        private Expression<Integer> integerExpression;
        private boolean isNested;
        private Expression<?> dataExpression;

        @Override
        @SuppressWarnings("all")
        protected @Nullable Object @NotNull [] get(@NotNull Event e) {
            final JsonObject object = new JsonObject();
            if (line == 2) {
                String path = "Undefined";
                if (pathExpression != null) path = pathExpression.getSingle(e);
                assert path != null;
                return new Object[]{List.of("array", dataExpression.getAll(e), path)};
            } else if (line == 3) {
                String path = "Undefined";
                if (pathExpression != null) path = pathExpression.getSingle(e);
                int i = -9139;
                switch (tag) {
                    case 1 -> i = 0;
                    case 2 -> i = 1;
                    case 3 -> i = 2;
                    case 4 -> i = JsonBase.JsonSupportElement.lastElementConst;
                    case 5 -> {
                        if (integerExpression != null) {
                            int number = integerExpression.getSingle(e);
                            i = number - 1;
                        }
                    }
                }
                object.addProperty("element-index", i);
                object.addProperty("element-path", path);
            }
            return new JsonElement[]{object};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(e);
        }

        @Override
        @SuppressWarnings("all")
        public Class<?> @NotNull [] acceptChange(Changer.@NotNull ChangeMode mode) {
            return switch (mode) {
                case SET, ADD -> CollectionUtils.array(Object[].class);
                default -> null;
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public void change(@NotNull Event e, @Nullable Object @Nullable [] inputDelta, Changer.@NotNull ChangeMode mode) {
            switch (mode) {
                case ADD -> {
                    if (inputDelta == null || inputJsonExpression == null) {
                        error(new RuntimeException("Input or json cannot be null"), null, getParser().getNode());
                        return;
                    }
                    JsonElement json = JsonNull.INSTANCE;
                    Object parsedJson;
                    String path;
                    for (Object delta : inputDelta) {
                        parsedJson = parseAliases(delta);
                        if (((LinkedList<JsonElement>) parsedJson).isEmpty()) parsedJson = ParserUtil.parse(delta);

                        try {
                            if (isNested) {
                                JsonElement input = inputJsonExpression.getSingle(e);
                                path = pathExpression.getSingle(e);
                                LinkedList<PatternUtil.keyStruct> keys = convertStringToKeys(path, PATH_VARIABLE_DELIMITER, false);
                                JsonElement old = json;
                                if (!keys.isEmpty()) json = JsonParser.search(input).key(keys);
                                var last = keys.getLast().key();

                                if (json == null) {
                                    json = old;
                                    JsonParser.change(input).value(keys, new JsonArray());
                                    json = JsonParser.search(input).key(keys);
                                    keys.clear();
                                    keys.add(new PatternUtil.keyStruct(last, PatternUtil.KeyType.LIST));
                                    json = JsonParser.search(json).key(keys);
                                }
                            } else {
                                json = inputJsonExpression.getSingle(e);
                            }
                            if (json == null) return;


                            if (json.isJsonArray()) {
                                if (parsedJson instanceof JsonElement element) {
                                    if (!element.isJsonNull()) json.getAsJsonArray().add(element);
                                } else {
                                    assert parsedJson != null;
                                    for (JsonElement element : ((LinkedList<JsonElement>) parsedJson)) {
                                        if (!element.isJsonNull()) json.getAsJsonArray().add(element);
                                    }
                                }
                            } else {
                                if (LOGGING_LEVEL > 1)
                                    error(new RuntimeException("You can add values only to JSON arrays."), null, getParser().getNode());
                                return;
                            }
                        } catch (Exception ex) {
                            if (PROJECT_DEBUG)
                                error(ex, null, getParser().getNode());
                        }
                    }
                }
                case SET -> {
                    if (inputDelta == null || inputJsonExpression == null) {
                        error(new RuntimeException("Input or json cannot be null"), null, getParser().getNode());
                        return;
                    }
                    JsonElement json;
                    Object parsedJson;
                    String path;
                    boolean isValue = result.mark == 1 && line == 1;
                    try {
                        path = pathExpression.getSingle(e);
                        LinkedList<PatternUtil.keyStruct> keys = PatternUtil.convertStringToKeys(path, PATH_VARIABLE_DELIMITER, false);
                        json = inputJsonExpression.getSingle(e);
                        for (Object delta : inputDelta) {
                            if (keys.isEmpty()) return;
                            if (isValue) {
                                parsedJson = parseAliases(delta);
                                if (((LinkedList<JsonElement>) parsedJson).isEmpty())
                                    parsedJson = ParserUtil.parse(delta);
                                if (parsedJson instanceof JsonElement element) {
                                    JsonParser.change(json).value(keys, element);
                                } else {
                                    JsonParser.change(json).value(keys, GsonConverter.toJsonTree(parsedJson, LinkedList.class));
                                }
                            } else {
                                keys = PatternUtil.convertStringToKeys(path, PATH_VARIABLE_DELIMITER);
                                if (keys.isEmpty()) return;
                                if (delta instanceof String st) {
                                    JsonParser.change(json).key(keys, st);
                                }
                            }
                        }

                    } catch (Exception ex) {
                        if (PROJECT_DEBUG)
                            error(ex, null, getParser().getNode());
                    }
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
            result = parseResult;
            line = matchedPattern;
            tag = result.mark;
            if (line == 2) {
                pathExpression = (Expression<String>) exprs[1];
                dataExpression = LiteralUtils.defendExpression(exprs[0]);
                isNested = pathExpression != null;
                return LiteralUtils.canInitSafely(dataExpression);
            } else if (line == 3) {
                integerExpression = (Expression<Integer>) exprs[0];
                pathExpression = (Expression<String>) exprs[1];
                return true;
            } else {
                pathExpression = (Expression<String>) exprs[0];
                inputJsonExpression = LiteralUtils.defendExpression(exprs[1]);
                isNested = pathExpression != null;
                return LiteralUtils.canInitSafely(inputJsonExpression);
            }
        }
    }


    @Since("2.9")
    @Name("Changer - JsonObject set/remove/removeAll")
    @Description({"The new documentation you will find here: https://skjsonteam.github.io/skJsonDocs/beta/defaults", "A very general effect that can change many json object. The json object can be only set/remove/removeAll"})
    @Examples({
            "set value of json object \"data::key\" in {_json} to \"new test key\"",
            "set key of json object \"data::key\" in {_json} to \"test key\"",
            "set key of json object \"list\" in {_json} to \"array\"",
            "set value of json object \"this-a test-what i need to <>-_::data\" in {_json} to iron sword",
            "remove value \"test key\" of json object \"data\" from {_json}",
            "remove all \"new test key\" of json object \"data\" from {_json}"
    })
    public static class JsonObjectChanger extends SimpleExpression<Object> {
        static {
            SkJsonElements.registerExpression(JsonObjectChanger.class, Object.class, ExpressionType.SIMPLE,
                    "(:key|:value)[2:s] of json object %-string% in %json%",
                    "[by] (:key|:value)[s] %objects% of json object [%-string%]",
                    "%objects% of json (object|array|list) [%-string%]"
            );
        }

        private Expression<JsonElement> jsonInput;
        private Expression<String> pathInput;
        private Expression<?> objectsInput;
        private ParseResult result;
        private int line;


        @Override
        protected @Nullable Object @NotNull [] get(@NotNull Event e) {
            if (line == 1 || line == 2) {
                String path = "Undefined";
                if (pathInput != null) path = pathInput.getSingle(e);
                assert path != null;
                if (line == 1) {
                    return new Object[]{List.of("object", objectsInput.getAll(e), path, result.hasTag("value"))};
                } else {
                    return new Object[]{List.of("object", objectsInput.getAll(e), path)};
                }
            }
            return new Object[]{new JsonObject()};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public @NotNull Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return Classes.getDebugMessage(e);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            result = parseResult;
            line = matchedPattern;
            if (line == 0) {
                jsonInput = LiteralUtils.defendExpression(exprs[1]);
                pathInput = (Expression<String>) exprs[0];
                return LiteralUtils.canInitSafely(jsonInput);
            } else if (line == 1 || line == 2) {
                pathInput = (Expression<String>) exprs[1];
                objectsInput = LiteralUtils.defendExpression(exprs[0]);
                return LiteralUtils.canInitSafely(objectsInput);
            }
            return false;
        }

        @SuppressWarnings("all")
        public Class<?> @NotNull [] acceptChange(Changer.@NotNull ChangeMode mode) {
            return switch (mode) {
                case SET -> CollectionUtils.array(Object.class, Object[].class);
                default -> null;
            };
        }

        @Override
        @SuppressWarnings("unchecked")
        public void change(@NotNull Event e, @Nullable Object @Nullable [] inputDelta, Changer.@NotNull ChangeMode mode) {
            if (mode == Changer.ChangeMode.SET) {
                if (inputDelta == null || jsonInput == null) {
                    error(new RuntimeException("Input or json cannot be null"), null, getParser().getNode());
                    return;
                }
                boolean isValue = result.hasTag("value");
                JsonElement json;
                Object parsedJson;
                String path;
                for (Object delta : inputDelta) {
                    try {
                        path = pathInput.getSingle(e);
                        LinkedList<PatternUtil.keyStruct> keys = PatternUtil.convertStringToKeys(path, PATH_VARIABLE_DELIMITER, true);
                        json = jsonInput.getSingle(e);
                        assert !keys.isEmpty();
                        if (!isValue) {
                            keys = PatternUtil.convertStringToKeys(path, PATH_VARIABLE_DELIMITER);
                            if (keys.isEmpty()) return;
                            if (delta instanceof String st) JsonParser.change(json).key(keys, st);
                        } else {
                            parsedJson = parseAliases(delta);
                            if (((LinkedList<JsonElement>) parsedJson).isEmpty()) parsedJson = ParserUtil.parse(delta);
                            if (parsedJson instanceof JsonElement element) {
                                JsonParser.change(json).value(keys, element);
                            } else {
                                JsonParser.change(json).value(keys, GsonConverter.toJsonTree(parsedJson, LinkedList.class));
                            }
                        }
                    } catch (Exception ex) {
                        error(ex, null, getParser().getNode());
                    }
                }
            }
        }
    }
}
