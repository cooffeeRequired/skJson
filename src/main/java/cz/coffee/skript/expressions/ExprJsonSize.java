package cz.coffee.skript.expressions;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Name("Json size of given Json")
@Examples({
        "set {_json} to json from text \"{'A': 1, 'B': 2, 'C': {'A': 'B', 'X': 'Y'}}\"",
        "if json size of {_json} > 1:",
        "\tsend \"JSON's size is bigger the 1\""
})
@Since("2.8.3, 2.8.0 - performance & clean")

public class ExprJsonSize extends SimplePropertyExpression<JsonElement, Integer> {

    static {
        register(ExprJsonSize.class, Integer.class, "json size", "json");
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "json size";
    }

    @Override
    public @Nullable Integer convert(JsonElement jsonElement) {
        return jsonElement.isJsonArray() ? ((JsonArray) jsonElement).size() : ((JsonObject) jsonElement).size();
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }
}
