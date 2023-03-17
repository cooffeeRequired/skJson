package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static cz.coffee.SkJson.JSON_STORAGE;

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

@Name("All cached jsons")
@Description("That will return jsons from your cache.")
@Examples({
        "command AllCachedJsons:",
        "\ttrigger:",
        "\t\tsend all cached jsons"
})
@Since("2.8.0 performance & clean")
public class GetAllCachedJson extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(GetAllCachedJson.class, JsonElement.class, ExpressionType.SIMPLE,
                "all cached jsons")
        ;
    }

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event event) {
        ArrayList<JsonElement> finalElements = new ArrayList<>();
        JSON_STORAGE.forEach((id, map) -> map.forEach((json, file) -> finalElements.add(json)));
        return finalElements.toArray(new JsonElement[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "all cached jsons";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }
}
