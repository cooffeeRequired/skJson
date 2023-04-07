package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

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

@Name("Save cached json to file")
@Description("It's allow save cached json back to the file")
@Examples({
        "on unload:",
        "\tsave cached json \"test\""
})
@Since("2.8.0 - performance & clean")
public class EffSaveCache extends AsyncEffect {

    static {
        Skript.registerEffect(EffSaveCache.class,
                "[:async] save cached json %string%",
                "[:async] save all cached jsons"
        );
    }

    private int line;
    private boolean async;
    private Expression<String> externalExprID;

    @Override
    protected void execute(@NotNull Event e) {
        if (line == 0) {
            String id = externalExprID.getSingle(e);
            for (Map.Entry<String, Map<JsonElement, File>> mapEntry : JSON_STORAGE.entrySet()) {
                for (Map.Entry<JsonElement, File> entry : mapEntry.getValue().entrySet()) {
                    if (mapEntry.getKey().equals(id)) {
                        FileUtils.write(entry.getValue(), entry.getKey(), async);
                        return;
                    }
                }
            }
        } else {
            for (Map.Entry<String, Map<JsonElement, File>> mapEntry : JSON_STORAGE.entrySet()) {
                for (Map.Entry<JsonElement, File> entry : mapEntry.getValue().entrySet()) {
                    FileUtils.write(entry.getValue(), entry.getKey(), async);
                }
                return;
            }
        }

    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if (line == 0) return "save cached json " + externalExprID.toString(e, debug);
        else
            return "save all cached jsons";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        async = parseResult.hasTag(("async"));
        line = matchedPattern;
        if (line == 0) {
            externalExprID = (Expression<String>) exprs[0];
        }
        return true;
    }
}
