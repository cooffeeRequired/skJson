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

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Friday (3/10/2023)
 */

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
