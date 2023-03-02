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
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.utils.JsonMapping.listToJson;

@Name("Array or List formatted to JSON.")
@Description({"It allows you to convert the sheet back to Json!",
        "Value changes don't work for nested objects, to change the values of a nested object use Change"})
@Examples({"on script load:",
        "\tset {-json} to json from string \"{'test': [1,2,3,false,null,'some'], 'test2': {'something': false}}\"",
        "\tmap {-json} to {_json::*}",
        "\tsend \"&9%{_json::*}'s form with pretty print%\""
})
@Since("1.3.0")

public class ExprSkriptToJson extends SimpleExpression<JsonElement> {

    static {
        PropertyExpression.register(ExprSkriptToJson.class, JsonElement.class, "(form|formatted json)", "objects");
    }

    private VariableString variableString;
    private boolean isLocal;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        String variableName = variableString.toString(e);
        return new JsonElement[]{listToJson(variableName.substring(0, variableName.length() - 1), isLocal, e)};
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
        return "form of " + variableString.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        Expression<?> objects = exprs[0];
        if (objects instanceof Variable<?>) {
            Variable<?> var = (Variable<?>) objects;
            if (var.isList()) {
                isLocal = var.isLocal();
                variableString = var.getName();
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
