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
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.SkJson.FILE_JSON_MAP;
import static cz.coffee.SkJson.JSON_STORAGE;

@Name("Remove json from internal cache")
@Description({"You can basically remove the json of given id from internal cache"})
@Examples({"on load:",
        "\tremove json \"test\"",
})
@Since("2.5.0")

public class RemoveJsonFromCache extends Effect {

    static {
        Skript.registerEffect(RemoveJsonFromCache.class,
                "(remove|unlink) cached json %string%"
        );
    }

    private Expression<String> externalExprID;

    @Override
    protected void execute(@NotNull Event e) {
        String externalID = externalExprID.getSingle(e);
        if (JSON_STORAGE.containsKey(externalID) && FILE_JSON_MAP.containsKey(externalID)) {
            JSON_STORAGE.remove(externalID);
            FILE_JSON_MAP.remove(externalID);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "(remove|unlink) cached json" + externalExprID.toString(e, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        externalExprID = (Expression<String>) exprs[0];
        return true;
    }
}
