package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;

@Name("Size of JSON")
@Description("Return size of JSON elements.")
@Since("1.0")

@SuppressWarnings({"unused","NullableProblems"})
public class ExprSizeOfJson extends SimplePropertyExpression<JsonElement, Integer> {

    static {
        register(ExprSizeOfJson.class, Integer.class,
                "size", "jsonelements");
    }



    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends JsonElement>) exprs[0]);
        return true;
    }

    @Override
    protected String getPropertyName() {
        return "json size of json element";
    }

    @Override
    public Integer convert(JsonElement jsonElement) {
        if (jsonElement.isJsonNull() || jsonElement.isJsonPrimitive())
            return null;

        return jsonElement.isJsonObject() ? jsonElement.getAsJsonObject().size() : jsonElement.getAsJsonArray().size();
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }
}