package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffee.core.cache.JsonWatcher;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

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

@Name("UnLink or Unload json file")
@Description("You can unload the json file.")
@Examples({
        "on load:",
        "\tlunink json \"mine.id\""
})
@Since("2.8.0 - performance & clean")
public class EffUnlinkJsonFile extends Effect {

    static {
        Skript.registerEffect(EffUnlinkJsonFile.class, "unlink json %string%");
    }

    private Expression<String> exprID;


    @Override
    protected void execute(@NotNull Event event) {
        String id = exprID.getSingle(event);
        if (id == null) return;
        if (JSON_STORAGE.containsKey(id)) {
            AtomicReference<File> file = new AtomicReference<>();
            JSON_STORAGE.get(id).forEach((json, file0) -> file.set(file0));
            JsonWatcher.unregister(file.get());
            JSON_STORAGE.remove(id);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "unlink json " + exprID.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        exprID = (Expression<String>) expressions[0];
        return true;
    }
}
