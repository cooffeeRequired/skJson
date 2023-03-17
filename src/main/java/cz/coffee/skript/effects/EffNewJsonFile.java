package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static cz.coffee.core.utils.AdapterUtils.parseItem;

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

@Name("New json file")
@Description({"You can create a new json file."})
@Since("2.8.0 - performance & clean")

public class EffNewJsonFile extends Effect {

    static {
        Skript.registerEffect(EffNewJsonFile.class, "[:async] new json file %string% [(:with) (object|content)[s] %-object%]");
    }

    private boolean async, with;
    private Expression<String> expressionPath;
    private Expression<?> expressionObject;

    @Override
    protected void execute(@NotNull Event e) {
        String path = expressionPath.getSingle(e);
        JsonElement content;
        if (path == null) return;
        final File file = new File(path);
        if (with) {
            Object o = expressionObject.getSingle(e);
            if (o == null) {
                content = new JsonObject();
            } else {
                content = parseItem(o, o.getClass());
            }
        } else {
            content = new JsonObject();
        }
        FileUtils.write(file, content, async);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return String.format("%s new json file %s %s", (async ? "async" : ""), expressionPath.toString(e, debug), (with ? "with content " + expressionObject.toString(e, debug) : ""));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        with = parseResult.hasTag("with");
        async = parseResult.hasTag("async");
        expressionPath = (Expression<String>) exprs[0];
        expressionObject = LiteralUtils.defendExpression(exprs[1]);
        if (with) return LiteralUtils.canInitSafely(expressionObject);
        return true;
    }
}
