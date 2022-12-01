/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

@Name("Size of JSON")
@Description("Return size of JSON elements.")
@Examples({"on load:",
        "   set {-e} to new json from string {\"anything\": [1,2,\"false\"]}",
        "   broadcast {-e}'size",
        "   broadcast size of {-e}"
})
@Since("1.0")

public class ExprSizeOfJson extends SimplePropertyExpression<JsonElement, Integer> {

    static {
        register(ExprSizeOfJson.class, Integer.class,
                "size", "jsonelements");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        setExpr((Expression<? extends JsonElement>) exprs[0]);
        return true;
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "json size of json element";
    }

    @Override
    public Integer convert(JsonElement jsonElement) {
        if (jsonElement.isJsonNull() || jsonElement.isJsonPrimitive())
            return null;

        return jsonElement.isJsonObject() ? jsonElement.getAsJsonObject().size() : jsonElement.getAsJsonArray().size();
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }
}