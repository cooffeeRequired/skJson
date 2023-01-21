/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Size of JSON")
@Description("Return size of JSON")
@Examples({"on load:",
        "   set {-e} to new json from string \"{'anything': [1,2,false]}\"",
        "   broadcast {-e}'s json size",
        "   broadcast json size of {-e}"
})
@Since("2.5.0")

public class ExprSizeOfJson extends SimplePropertyExpression<JsonElement, Integer> {

    static {
        register(ExprSizeOfJson.class, Integer.class,
                "json size", "jsons");
    }


    @Override
    protected @NotNull String getPropertyName() {
        return "json size of json";
    }

    @Override
    public @Nullable Integer convert(JsonElement element) {
        if (!(element.isJsonNull() || element.isJsonPrimitive())) {
            return (element instanceof JsonObject ? element.getAsJsonObject().size() : (element instanceof JsonArray ? element.getAsJsonArray().size() : 0));
        }
        return null;
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull SkriptParser.ParseResult parseResult) {
        setExpr((Expression<? extends JsonElement>) exprs[0]);
        return true;
    }
}
