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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.cache.Cache;
import cz.coffee.core.utils.JsonFilesHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;


@Name("Load json-file to internal cache")
@Description("You can load the json-file to cache with your defined string id")
@Examples({"on script load:",
        "\tload json file \"plugins/skJson/test.json\" as \"skJson\""
})
@Since("2.5.0")

public class EffLoadJsonFileToCache extends Effect {

    static {
        Skript.registerEffect(EffLoadJsonFileToCache.class,
                "(load|link) json file %string% [(:as) %-string%]"
        );
    }

    private Expression<String> filePathExpr, asIDExpr;
    private boolean asExternal;

    @Override
    protected void execute(@NotNull Event e) {
        JsonFilesHandler jfh = new JsonFilesHandler();
        String jsonFilePathString = filePathExpr.getSingle(e);
        String externalID = "";
        if (asExternal) {
            externalID = asIDExpr.getSingle(e);
            if (externalID == null) externalID = jsonFilePathString;
        }
        if (jsonFilePathString != null) {
            JsonElement jsonFileData = jfh.readFile(jsonFilePathString);
            if (jsonFileData != null) {
                Cache.addTo(externalID, jsonFileData, new File(jsonFilePathString));
            }
        }

    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "load json file " + filePathExpr.toString(e, debug) + (asExternal ? " as " + asIDExpr.toString(e, debug) : "");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        asExternal = parseResult.hasTag(("as"));
        filePathExpr = (Expression<String>) exprs[0];
        asIDExpr = (Expression<String>) exprs[1];
        return true;
    }
}
