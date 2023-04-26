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

@Name("Json size of current element")
@Examples({
        "command SizeJson:",
        "  trigger:",
        "    set {_json} to json from text \"{'A': 1, 'B': 2, 'C': {'A': 'B', 'X': 'Y'}}\"",
        "    send size of {_json} # = 3 (A, B, C)",
        "    send size of (element \"C\" of {_json}) # = 2 (A, X)",
})
@Since("2.8.0 - performance & clean")

public class ExprJsonSize extends SimplePropertyExpression<JsonElement, Integer> {

    static {
        register(ExprJsonSize.class, Integer.class, "size", "jsons");
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
