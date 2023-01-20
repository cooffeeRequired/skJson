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

package cz.coffee.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Since("1.3.0")
@Name("JSON empty")
@Description("You can simply check if the file is empty.")
@Examples({"on script load:",
        "\tset {_j} to json from string \"{'Hello': 'There'}\"",
        "\t{_j} is empty: ",
        "\t\tbroadcast \"is empty\"",
})
public class CondJsonEmpty extends Condition {

    private Expression<JsonElement> exprJson;
    private int pattern;


    static {
        Skript.registerCondition(CondJsonEmpty.class,
                "json %json% is empty",
                "json %json% is(n't| not) empty"
        );
    }

    @Override
    public boolean check(@NotNull Event e) {
        JsonElement json = exprJson.getSingle(e);
        if (json == null) return false;
        if (json instanceof JsonObject)
            return (pattern == 0) == json.getAsJsonObject().entrySet().isEmpty();
        else if (json instanceof JsonArray)
            return (pattern == 0) == json.getAsJsonArray().isEmpty();
        else if (json instanceof JsonPrimitive)
            return (pattern == 0) == json.getAsJsonPrimitive().isJsonNull();
        return false;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json " + exprJson.toString(e, debug) + (pattern == 0 ? "is" : "is not") + " empty";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprJson = (Expression<JsonElement>) exprs[0];
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }
}
