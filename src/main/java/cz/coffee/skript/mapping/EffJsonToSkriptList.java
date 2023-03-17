package cz.coffee.skript.mapping;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.core.mapping.JsonMap;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.utils.AdapterUtils.parseItem;

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
 * Created: úterý (14.03.2023)
 */

@Name("Map json to skript list")
@Description({
        "<b>@Deprecated</b>",
        "Mapping json to the List and get those values"
})
@Examples({"on load:",
        "\tset {_json} to json from string \"{'test': 'test2': {}}\"",
        "\tset {_json} to \"{\"\"test\"\": \"\"test2\"\": {}}\"",
        "\tmap {_json} to {_json::*}",
        "\tsend {_json::*}"
})
@Deprecated
@Since("2.8.0 - performance & clean")
public class EffJsonToSkriptList extends Effect {

    static {
        Skript.registerEffect(EffJsonToSkriptList.class, "(map|copy) %json/string% to %objects%");
    }

    private Expression<?> jsonElementExpression;
    private VariableString variableString;
    private boolean isLocal;

    @Override
    protected void execute(@NotNull Event e) {
        Object jsonObject = jsonElementExpression.getSingle(e);
        JsonElement json = parseItem(jsonObject, jsonElementExpression, e);
        String var = variableString.toString(e).substring(0, variableString.toString().length()-3);
        JsonMap.Skript.toList(var, json, isLocal, e);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "map " + jsonElementExpression.toString(e, debug) + " to " + variableString.toString(e);
    }
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        Expression<Object> objectExpression = LiteralUtils.defendExpression(exprs[1]);
        SkJson.warning("Using a deprecated API, the map %json%. It's may nor works properly with 2.7 Skript");
        if (!objectExpression.getReturnType().isAssignableFrom(JsonElement.class) || !objectExpression.getReturnType().isAssignableFrom(String.class)) {
            Skript.error("You can map only the json/string", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }

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
