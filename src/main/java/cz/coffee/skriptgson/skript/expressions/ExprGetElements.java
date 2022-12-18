package cz.coffee.skriptgson.skript.expressions;


import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
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

import static cz.coffee.skriptgson.utils.GsonUtils.fromPrimitive;
import static cz.coffee.skriptgson.utils.Utils.isNumeric;

@Name("Get elements from Json.")
@Description("You can get all elements from Object/Array")
@Examples({"on load:",
        "\tset {_json} to new json from string \"{'testArray': [1,2,3,4,5,{'nested': [1,2,3,4,5]}]}\"",
        "\tloop all json elements \"5:nested\" from {_json}:",
        "\t\tsend loop-value",
        " ",
        "\tloop all json elements from {_json}:",
        "\t\tsend loop-value"
})
@Since("2.0.2")

public class ExprGetElements extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprGetElements.class, Object.class, ExpressionType.COMBINED,
                "[all] json elements [%-string% ]from %jsonelement%"
        );
    }

    private Expression<String> fromObject;
    private Expression<JsonElement> jsonElementExpression;
    private boolean isAll = false;

    private Object[] getElements(JsonElement element) {
        List<Object> jsons = new ArrayList<>();
        if (element instanceof JsonArray array) {
            for (int index = 0; array.size() > index; index++) {
                JsonElement element1 = array.get(index);
                if (element1 instanceof JsonPrimitive primitive) {
                    jsons.add(fromPrimitive(primitive));
                } else {
                    jsons.add(element1);
                }
            }
            return jsons.toArray();
        } else if (element instanceof JsonObject object) {
            for (Map.Entry<String, JsonElement> map : object.entrySet()) {
                JsonElement element1 = map.getValue();
                if (element1 instanceof JsonPrimitive primitive) {
                    jsons.add(fromPrimitive(primitive));
                } else {
                    jsons.add(element1);
                }
            }
            return jsons.toArray();
        }
        return null;
    }

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        String fromString;
        JsonElement json = this.jsonElementExpression.getSingle(e);

        if (!isAll) {
            fromString = this.fromObject.getSingle(e);
            if (fromString == null) return new JsonElement[0];
            if (fromString.contains(":")) {
                String[] nestedKeys = fromString.split(":");
                for (String key : nestedKeys) {
                    if (json instanceof JsonObject object) {
                        json = object.get(key);
                    } else if (json instanceof JsonArray array) {
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
            } else {
                if (json instanceof JsonObject object) {
                    json = object.get(fromString);
                } else if (json instanceof JsonArray array) {
                    int index = 0;
                    if (isNumeric(fromString)) {
                        index = Integer.parseInt(fromString);
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
        if (returnElements == null) return new JsonElement[0];
        return returnElements;
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
        return "all json elements " + (isAll ? "" : fromObject.toString(e, debug)) + " from " + jsonElementExpression.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        fromObject = (Expression<String>) exprs[0];
        jsonElementExpression = (Expression<JsonElement>) exprs[1];
        if (fromObject == null) isAll = true;
        return true;
    }
}
