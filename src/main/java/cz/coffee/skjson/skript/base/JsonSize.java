package cz.coffee.skjson.skript.base;

import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.SkJson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Size of json object/array")
@Examples({
        "set {_json} to json from text \"{'sample': {}, 'second': []}\"",
        "if json size of {_json} > 1:",
        "\tsend {_json} is bigger than 1."
})
@Since("2.9")


public class JsonSize extends SimplePropertyExpression<JsonElement, Integer> {
    static {
        SkJson.registerSimplePropertyExpression(JsonSize.class, Integer.class, "json size", "jsons");
    }


    @Override
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
