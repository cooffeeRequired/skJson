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
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.utils.ErrorHandler;
import cz.coffee.utils.nbt.NBTInternalConvertor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static cz.coffee.adapter.DefaultAdapters.assignFrom;
import static cz.coffee.utils.ErrorHandler.sendMessage;
import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.SimpleUtil.printPrettyStackTrace;
import static cz.coffee.utils.config.Config._STACKTRACE_LENGTH;

@Name("Json to Inventory, Chunk, World, NBT, Entity, Location, Unknown-Type")
@Description({"You can deserialize correct json to skript-type, for example a tool a location, etc."})
@Examples({"command saveLocToJson:",
        "\ttrigger:",
        "\t\tset {-json} to new json from sender's location",
        "\t\tsend \"Saved location as JSON &e%{-json}%\"",
        "",
        "command teleporttoJson:",
        "\ttrigger",
        "\t\tset {-loc} to {-json} parsed as a location",
        "\t\tsend \"You will be tp to &b%{-loc}%&r from Json\"",
        "\t\tteleport sender to {-loc}"
})
@Since("2.0.0")
public class EffParse extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(EffParse.class, Object.class, ExpressionType.SIMPLE,
                "%object% parsed as [a] [skript] (:inv[entory]|:chunk|:world|:nbt|:loc[ation]|:item[type|stack]|(unknown |):type)"
        );
    }

    private Expression<Object> json;
    private int patternType;
    private List<String> tags;

    private String[] types = {
            "inventory",
            "chunk",
            "world",
            "nbt",
            "location",
            "item",
            "others"
    };

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return json.toString(e, debug) + " parsed as " + types[patternType];
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        tags = parseResult.tags;

        if (tags.contains("inv")) {
            patternType = 1;
        } else if (tags.contains("chunk")) {
            patternType = 2;
        } else if (tags.contains("world")) {
            patternType = 3;
        } else if (tags.contains("nbt")) {
            patternType = 4;
        } else if (tags.contains("loc")) {
            patternType = 100;
        } else if (tags.contains("item")) {
            patternType = 6;
        } else {
            patternType = 0;
        }

        json = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(json);
    }

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        Object object = json.getSingle(e);

        try {
            if (object instanceof JsonElement) {
                return new Object[]{assignFrom((JsonElement) object)};
            }
        } catch (Exception ex) {
            printPrettyStackTrace(ex, _STACKTRACE_LENGTH);
        }
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }
}