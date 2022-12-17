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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;
import static cz.coffee.skriptgson.utils.Utils.isNumeric;

@Name("Get element from Json")
@Description({"Get value element from JsonElement or cached Json Map."})
@Examples({"on load:",
        "\tset {_json} to new json from text \"{'test': true}\"",
        "\tset {-e} to element \"test\" from json {_json}",
        "",
        "\tset {-e} to element \"test\" from json \"your\""
})
@Since("2.0.0")

public class ExprGetElement extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprGetElement.class, JsonElement.class, ExpressionType.COMBINED,
                "element %string% from %jsonelement%",
                "element %string% from [cached] json[(-| )id] %string%"
        );
    }

    private Expression<String> stringExpression;
    private Expression<JsonElement> jsonElementExpression;
    private Expression<String> stringIdExpression;
    private int pattern;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        String stringIdExpression, stringExpression;
        JsonElement jsonElementExpression, json;

        stringExpression = this.stringExpression.getSingle(e);
        if (pattern == 0) {
            jsonElementExpression = this.jsonElementExpression.getSingle(e);
            json = jsonElementExpression;
        } else {
            stringIdExpression = this.stringIdExpression.getSingle(e);
            json = hierarchyAdapter().toJsonTree(JSON_HASHMAP.get(stringIdExpression));
        }

        if (stringExpression == null) return new JsonElement[0];
        String[] values = stringExpression.split(":");

        if (values[0] == null || json == null) return new JsonElement[0];


        JsonElement next = null, returnValue = null;


        for (String key : values) {
            if (json instanceof JsonArray array) {
                int index = 0;
                if (isNumeric(key)) {
                    index = Integer.parseInt(key);
                }
                json = array.get(index);
            } else if (json instanceof JsonObject object) {
                json = object.get(key);
            }
            if (key.equals(values[values.length - 1])) {
                return new JsonElement[]{json};
            }
        }
        return new JsonElement[0];
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
        return "element " + stringExpression.toString(e, debug) + " from " + (pattern == 0 ? "json " : " json-id") + jsonElementExpression.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        stringExpression = (Expression<String>) exprs[0];
        if (pattern == 0) {
            jsonElementExpression = (Expression<JsonElement>) exprs[1];
        } else {
            stringIdExpression = (Expression<String>) exprs[1];
        }
        return true;
    }
}
