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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Name("JSON File exists")
@Description("You can simply check whether the json-file exists or not.")
@Examples({"on load:",
        "\tbroadcast json file \"test\\test.json\" exists"
})
@Since("1.3.1")

public class CondFileExist extends Condition {

    static {
        Skript.registerCondition(CondFileExist.class,
                "json file %string% exists",
                "json file %string% does(n't| not) exists"
        );
    }

    private Expression<String> exprFile;
    private int pattern;

    @Override
    public boolean check(@NotNull Event e) {
        String fileString = exprFile.getSingle(e);
        assert fileString != null;
        File file = new File(fileString);
        return (pattern == 0) == file.exists();
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json file " + exprFile.toString(e, debug) + (pattern == 0 ? " exists" : "does not exists");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprFile = (Expression<String>) exprs[0];
        pattern = matchedPattern;
        setNegated(pattern == 1);
        return true;
    }
}
