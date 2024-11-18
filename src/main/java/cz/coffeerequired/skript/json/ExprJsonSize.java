package cz.coffeerequired.skript.json;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
