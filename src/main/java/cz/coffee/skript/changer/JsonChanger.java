package cz.coffee.skript.changer;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.SkJson;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.JsonUtils.*;
import static cz.coffee.core.utils.Util.extractKeys;
import static cz.coffee.core.utils.Util.jsonToObject;

@SuppressWarnings("unused")
@Since("2.8 - b2")
@Name("Default changer of skJson (SET/REMOVE/ADD)")
@Description({
        "Default changer you can change your current's jsons by those expressions",
        "More information you can found here.. : https://github.com/cooffeeRequired/skJson"
})
@Examples({
        "remove diamond sword from {_json}",
        "remove 2nd element from json list \"pathxys\" in {_json}",
        "remove player's location from json list \"pathxys\" in {_json}",
        "remove \"hello\" from keys of json object \"pathxys\" in {_json}",
        "remove diamond sword from values of json object \"pathxys\" in {_json}",
        "set json value \"test:A\" in {_json} to diamond sword",
        "add player's location to json list \"pathxys\" in {_json}"
})
public class JsonChanger extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(JsonChanger.class, Object.class, ExpressionType.COMBINED,
                "json list [%-string%] in %json%",
                "(:keys|:values) of json object [%-string%] in %json%",
                "json (:value|:key) %string% in %json%"
        );
    }

    private int pattern;
    private SkriptParser.ParseResult result;
    private Expression<JsonElement> jsonExpression;
    private Expression<String> pathExpression;

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        ArrayList<Object> objects = new ArrayList<>();
        final JsonElement json = jsonExpression.getSingle(e);
        final String pathString = pathExpression.getSingle(e);
        if (json == null) return new Object[0];
        if (pattern == 0) {
            JsonElement in = JsonNull.INSTANCE;
            if (pathString == null) {
                LinkedList<String> keys = extractKeys(pathString, null, true);
                in = getByKey(json, keys);
            } else {
                in = json;
            }

            if (in == null) return new JsonElement[0];
            if (in instanceof JsonArray array) {
                for (JsonElement a : array) {
                    objects.add(jsonToObject(a));
                }

                System.out.println(objects);

                return objects.toArray(new Object[0]);
            }

        } else if (pattern == 1) {
            LinkedList<String> keys = extractKeys(pathString, null, true);
            final JsonElement in = getByKey(json, keys);
            if (result.hasTag("keys")) {
                if (in == null) return new JsonElement[0];
                if (in instanceof JsonObject object) {
                    objects.addAll(object.keySet());
                }
            } else if (result.hasTag("values")) {
                if (in == null) return new JsonElement[0];
                if (in instanceof JsonObject object) {
                    object.entrySet().forEach(entry -> objects.add(entry.getValue()));
                }
            }
            return objects.toArray(new Object[0]);
        }
        return new Object[0];
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

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Class<?> @NotNull [] acceptChange(Changer.@NotNull ChangeMode mode) {
        switch (mode) {
            case SET:
            case ADD:
            case REMOVE:
                return CollectionUtils.array(Object.class);
            default:
                return null;
        }
    }

    @Override
    public void change(@NotNull Event e, @Nullable Object @Nullable [] inputDelta, Changer.@NotNull ChangeMode mode) {
        Object[] delta = null;
        if (inputDelta != null && inputDelta.length == 1)
            delta = inputDelta;
        else if (inputDelta != null && inputDelta.length > 1) {
            final JsonArray array = new JsonArray();
            for (Object o : inputDelta) {
                assert o != null;
                array.add(parseItem(o, null, null, o.getClass()));
            }
            delta = new Object[]{array};
        }

        switch (mode) {
            case ADD:
                String pathString = null;
                JsonElement input = jsonExpression.getSingle(e);
                if (pathExpression != null) {
                    pathString = pathExpression.getSingle(e);
                }
                LinkedList<String> keys = extractKeys(pathString, null, true);
                JsonElement json;

                if (pathString == null) {
                    json = input;
                } else {
                    json = getByKey(input, keys);
                }

                if (json != null) {
                    if (json.isJsonObject()) {
                        Skript.error("You can add object only to JsonArray.types", ErrorQuality.SEMANTIC_ERROR);
                    } else if (json.isJsonArray()) {
                        assert inputDelta != null;
                        for (Object o : inputDelta) {
                            assert o != null;
                            json.getAsJsonArray().add(parseItem(o, null, null, o.getClass()));
                        }
                    }
                }
                break;
            case SET:
                input = jsonExpression.getSingle(e);
                pathString = pathExpression.getSingle(e);

                keys = extractKeys(pathString, null, true);
                if (keys == null) {
                    Skript.error("Unsupported input for square bracket " + 0 + ", index start with 1,2,3...", ErrorQuality.SEMANTIC_ERROR);
                    return;
                }

                assert delta != null;

                try {
                    for (Object o : delta) {
                        if (result.hasTag("value")) {
                            changeValue(input, keys, o);
                        } else if (result.hasTag("key")) {
                            changeKey(input, keys, o.toString());
                        }
                    }
                } catch (Exception ignored) {}
                break;
            case REMOVE:
                boolean type = false;
                Object value = null;
                if (inputDelta == null) {
                    Skript.error("You cannot remove a null", ErrorQuality.SEMANTIC_ERROR);
                    return;
                }
                json = jsonExpression.getSingle(e);
                if (pathExpression == null) return;
                String string = pathExpression.getSingle(e);
                keys = extractKeys(string, null);

                if (keys == null && string != null) {
                    Skript.error("Unsupported input", ErrorQuality.SEMANTIC_ERROR);
                    return;
                }

                for (Object o : inputDelta) {
                    if (o instanceof JsonObject object) {
                        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                            value = entry.getValue();
                            if (entry.getKey().equals("element expression")) {
                                type = true;
                                break;
                            }
                        }
                    }

                    if (type) {
                        if (json == null) return;
                        final JsonElement element = getByKey(json, keys);
                        if (element == null) return;
                        if (!element.isJsonArray()) {
                            Skript.error("You can remove object only from JsonArray.types", ErrorQuality.SEMANTIC_ERROR);
                            return;
                        } else {
                            int size = element.getAsJsonArray().size();
                            if (value == null) return;
                            if (value instanceof JsonElement jsonElement) {
                                if (jsonElement.getAsInt() == -100) value = (size - 1);
                                keys.add(value.toString());
                                removeByIndex(json, keys);
                            }
                        }
                    } else {
                        if (o == null) return;
                        JsonElement parsedElement = parseItem(o, o.getClass());
                        final JsonElement element = getByKey(json, keys);
                        if (element == null) return;
                        if (pattern == 0) {
                            if (element.isJsonArray()) {
                                removeByValue(json, keys, parsedElement);
                            } else {
                                Skript.error("You can remove object only from JsonArray.types", ErrorQuality.SEMANTIC_ERROR);
                                return;
                            }
                        } else if (pattern == 1) {
                            if (result.hasTag("keys")) {
                                if (parsedElement.isJsonPrimitive() && parsedElement.getAsJsonPrimitive().isString()) {
                                    if (element.isJsonObject()) {
                                        keys.add(parsedElement.getAsString());
                                        removeByKey(json, keys);
                                    } else {
                                        Skript.error("You can remove the keys only from JsonObject.types", ErrorQuality.SEMANTIC_ERROR);
                                        return;
                                    }
                                }
                            } else if (result.hasTag("values")) {
                                if (element.isJsonObject()) removeByValue(json, keys, parseItem(o, o.getClass()));
                                else {
                                    Skript.error("You can remove the values only from JsonObject.types", ErrorQuality.SEMANTIC_ERROR);
                                    return;
                                }
                            }
                        }
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mode);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        result = parseResult;
        pattern = matchedPattern;
        if (pattern != 3) {
            jsonExpression = (Expression<JsonElement>) exprs[1];
            pathExpression = (Expression<String>) exprs[0];
        }
        return true;
    }
}
