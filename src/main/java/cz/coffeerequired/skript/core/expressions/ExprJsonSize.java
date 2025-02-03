package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Name("Size of json object/array")
@Examples({
        "set {_json} to json from \"{'sample': {}, 'second': []}\"",
        "send {_json} is bigger than 1."
})
@Since("4.1 - API UPDATE")
public class ExprJsonSize extends SimplePropertyExpression<JsonElement, Integer> {

    protected @NotNull String getPropertyName() {
        return "json size of " + getExpr();
    }

    @Override
    public @Nullable Integer convert(JsonElement element) {
        if (element.isJsonNull() || element.isJsonPrimitive()) return null;
        return element.isJsonObject() ? ((JsonObject) element).size() : ((JsonArray) element).size();
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }
}
