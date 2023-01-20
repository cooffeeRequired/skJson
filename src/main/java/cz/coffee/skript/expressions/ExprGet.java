/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */

package cz.coffee.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.coffee.utils.SimpleUtil.isNumeric;
import static cz.coffee.utils.json.JsonUtils.fromPrimitive2Object;


@Name("Get element/elements from Json")
@Description({"Get single value or values from the json"})
@Examples({"on load:",
        "\tset {_json} to json from text \"{'test': true}\"",
        "\tset {-e} to element \"test\" from json {_json}",
        "\tsend {-e}",
})
@Since("2.5.0")


public class ExprGet extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprGet.class, Object.class, ExpressionType.COMBINED,
                "element %string% from %json%",
                "elements [%-string%] from %json%"
        );
    }

    private Expression<String> exprKey;
    private Expression<JsonElement> exprJson;
    private int pattern;


    private Object[] getElements(JsonElement element) {
        List<Object> jsons = new ArrayList<>();
        if (element instanceof JsonArray) {
            JsonArray array = (JsonArray) element;
            for (int i = 0; array.size() > i; i++) {
                JsonElement value = array.get(i);
                if (value instanceof JsonPrimitive) {
                    jsons.add(fromPrimitive2Object(value));
                } else {
                    jsons.add(value);
                }
            }
            return jsons.toArray();
        } else if (element instanceof JsonObject) {
            JsonObject object = (JsonObject) element;
            for (Map.Entry<String, JsonElement> map : object.entrySet()) {
                JsonElement value = map.getValue();
                if (value instanceof JsonPrimitive) {
                    jsons.add(fromPrimitive2Object(value));
                } else {
                    jsons.add(value);
                }
            }
            return jsons.toArray();
        }
        return null;
    }


    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        String key = null;
        if (exprKey != null) {
            key = exprKey.getSingle(e);
        }
   
        JsonElement json = exprJson.getSingle(e);

        if (pattern == 1 && key == null) {
            Object[] data = getElements(json);
            assert data != null;
            return data;
        }

        if (key != null || json != null){
            assert key != null;
            String[] keys = key.split(":");
            if (keys[0] != null) {
                if (pattern == 0) {
                    for (String nKey : keys) {
                        if (json instanceof JsonArray) {
                            JsonArray array = (JsonArray) json;
                            int index = 0;
                            if (isNumeric(nKey))
                                index = Integer.parseInt(nKey);

                            if (array.size() > index) {
                                json = array.get(index);
                            }
                        } else if (json instanceof JsonObject) {
                            JsonObject object = (JsonObject) json;
                            json = object.get(nKey);
                        }
                        if (nKey.equals(keys[keys.length-1])) {
                            if (json instanceof JsonPrimitive) {
                                JsonPrimitive primitive = (JsonPrimitive) json;
                                return new Object[]{fromPrimitive2Object(primitive)};
                            }
                            return new Object[]{json};
                        }
                    }
                } else if (pattern == 1) {
                    for (String nKey : keys) {
                        if (json instanceof JsonObject) {
                            JsonObject object = (JsonObject) json;
                            json = object.get(key);
                        } else if (json instanceof JsonArray) {
                            JsonArray array = (JsonArray) json;
                            int index = 0;
                            if (isNumeric(key)) {
                                index = Integer.parseInt(key);
                            }
                            try {
                                json = array.get(index);
                            } catch (IndexOutOfBoundsException exception) {
                                return new Object[0];
                            }
                        }
                    }
                }
                Object[] returnElements = getElements(json);
                if (returnElements != null) {
                    return returnElements;
                }
            }
        }
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return pattern == 0;

    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (pattern == 1 ? "elements" + (exprKey == null ? "" : exprKey.toString(e, debug)) : "element" + exprKey.toString(e, debug)) + " from " + exprJson.toString(e, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        exprKey = (Expression<String>) exprs[0];
        exprJson = (Expression<JsonElement>) exprs[1];
        return true;
    }
}
