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
package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.utils.JsonFilesHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;


@Name("Write json/json file")
@Description({"You can write/re-write to jsons"})
@Examples({"command sk-example:",
        "\ttrigger:",
        "\t\twrite player's location to cached json \"your\"",
        "\t\tsend cached json \"your\" with pretty print",
        "",
        "\t\tset {_json} to json from player's world",
        "\t\twrite {_json} to json file \"*.json\"",
})
@Since("2.0.0")

public class EffWriteToJsonFile extends Effect {

    static {
        Skript.registerEffect(EffWriteToJsonFile.class,
                "write %object% to json file %string%"
        );
    }

    private Expression<?> inputExpr;
    private Expression<String> jsonOutputExpr;

    @Override
    protected void execute(@NotNull Event e) {
        JsonFilesHandler jfh = new JsonFilesHandler();
        Object inputData = inputExpr.getSingle(e);
        String jsonOutput = jsonOutputExpr.getSingle(e);
        assert jsonOutput != null;
        File file = new File(jsonOutput);

        jfh.writeFile(file, DefaultAdapter.parse(inputData, inputExpr, e), false);

    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "write " + inputExpr.toString(e, debug) + " to json file " + jsonOutputExpr.toString(e, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        jsonOutputExpr = (Expression<String>) exprs[1];
        inputExpr = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(inputExpr);
    }
}
