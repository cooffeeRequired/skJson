package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

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
 * Created: pondělí (13.03.2023)
 */

@Name("Json file is cached")
@Description("Check if the file for given id is cached")
@Examples({
        "on load:",
        "\tif cached json \"test\" if linked:",
        "\t\tsend true"
})
@Since("2.8.0 - performance & clean")

public class CondJsonIsLoaded extends Condition {

    static {
        Skript.registerCondition(CondJsonIsLoaded.class,
                "cached json %string% is (load|linked)",
                "cached json %string% is(n't| not) (load|linked)"
        );
    }

    private Expression<String> exprId;
    private int line;

    @Override
    public boolean check(@NotNull Event event) {
        final String id = exprId.getSingle(event);
        return (line == 0) == JSON_STORAGE.containsKey(id);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "cached json " + exprId.toString(event, b) + (line == 0 ? " is " : " is not") + "loaded";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        line = i;
        setNegated(i == 1);
        exprId = (Expression<String>) expressions[0];
        return true;
    }
}
