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
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.cache.Cache;
import cz.coffee.core.utils.JsonFilesHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Name("Save content of cached json")
@Description({"Save content of cached json of your given ID back to the file."})
@Examples({"on unload:",
        "\tsave all cached jsons",
        "",
        "command savedata:",
        "\ttrigger:",
        "\t\tsave cached json \"entity\""
})
@Since("2.5.0")
public class EffSave extends Effect {

    static {
        Skript.registerEffect(EffSave.class,
                "[:async] save cached json %string%",
                "[:async] save all cached json[s]"
        );
    }

    private int pattern;
    private boolean async;
    private Expression<String> externalExprID;

    @Override
    protected void execute(@NotNull Event e) {
        JsonFilesHandler jfh = new JsonFilesHandler(false);
        if (pattern == 1) {
            Cache.getAll().forEach((key, value) -> {
                File fileLink = value.getFile();
                jfh.writeFile(fileLink, value.getJson(), async);
            });
        } else {
            String externalID = externalExprID.getSingle(e);
            if (Cache.contains(externalID)) {
                JsonElement value = Cache.getPackage(externalID).getJson();
                File fileLink = Cache.getPackage(externalID).getFile();
                jfh.writeFile(fileLink, value, async);
            }
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (async ? "async" : "") + (pattern == 0 ? " save cached json " + externalExprID.toString(e, debug) : "save all cached jsons");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        async = parseResult.hasTag(("async"));
        pattern = matchedPattern;
        if (pattern == 0) {
            externalExprID = (Expression<String>) exprs[0];
        }
        return true;
    }
}
