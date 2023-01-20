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
package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.utils.json.JsonMapping.jsonToList;


@Name("Mapping Json to List")
@Description("Mapping json to the List and get those values")
@Examples({"on load:",
        "\tset {_json} to json from string \"{'test': 'test2': {}}\"",
        "\tset {_json} to \"{\"\"test\"\": \"\"test2\"\": {}}\"",
        "\tmap {_json} to {_json::*}",
        "\tsend {_json::*}"
})
@Since("1.4.0")
public class EffMap extends Effect {

    static {
        Skript.registerEffect(EffMap.class, "map %json/string% to %objects%");
    }

    private Expression<?> jsonElementExpression;
    private VariableString variableString;
    private boolean isLocal;


    @Override
    protected void execute(@NotNull Event e) {
        Object jsonObject = jsonElementExpression.getSingle(e);
        if (jsonObject instanceof String) {
            try {
                jsonToList(variableString.toString(e).substring(0, variableString.toString(e).length() -3), JsonParser.parseString(jsonObject.toString()), isLocal, e);
            } catch (JsonSyntaxException exception) {
            }
        } else if (jsonObject instanceof JsonElement) {
            jsonToList(variableString.toString(e).substring(0, variableString.toString(e).length() -3), (JsonElement) jsonObject, isLocal, e);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "map "+jsonElementExpression.toString(e, debug)+" to " + variableString.toString(e);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        Expression<Object> objectExpression = LiteralUtils.defendExpression(exprs[1]);
        jsonElementExpression = exprs[0];
        if (objectExpression instanceof Variable<?>) {
            Variable<Object> var = (Variable<Object>) objectExpression;
            if (var.isList()) {
                isLocal = var.isLocal();
                variableString = var.getName();
                return LiteralUtils.canInitSafely(objectExpression);
            }
        }
        return false;
    }
}
