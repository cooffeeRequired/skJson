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
package cz.coffee.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.core.cache.Cache;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@Name("All cached Jsons")
@Description({"Print out all your saved json in the cache"})
@Examples({"on load:",
        "\tsend all cached json",
        "\tsend 1 of cached jsons",
})
@Since("2.5.0")
public class ExprGetAllCachedJsons extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprGetAllCachedJsons.class, Object.class, ExpressionType.COMBINED,
                "all cached json[s]",
                "%integer% of cached json[s]"
        );
    }

    private Expression<Integer> intOfExpr;
    private int pattern;


    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        if (pattern == 0) {
            if (!Cache.isEmpty()) {
                JsonElement finalOutput = new JsonArray();
                Cache.getAll().forEach((key, value) -> {
                    JsonObject elementObject = new JsonObject();
                    elementObject.add(key, value.getJson());
                    finalOutput.getAsJsonArray().add(elementObject);
                });
                return new Object[]{finalOutput};
            } else {
                return new Object[0];
            }
        } else {
            Integer infOf = intOfExpr.getSingle(e);
            if (infOf != null) {
                if (!Cache.isEmpty()) {
                    if ((Cache.size() + 1) > infOf) {
                        JsonElement finalOutput = new JsonArray();
                        AtomicInteger i = new AtomicInteger(0);
                        Cache.getAll().forEach((key, value) -> {
                            if (infOf > i.get()) {
                                i.getAndIncrement();
                                JsonObject elementObject = new JsonObject();
                                elementObject.add(key, value.getJson());
                                finalOutput.getAsJsonArray().add(elementObject);
                            }
                        });
                        if (finalOutput.getAsJsonArray().size() == 1) {
                            return new Object[]{finalOutput.getAsJsonArray().get(finalOutput.getAsJsonArray().size() - 1)};
                        } else {
                            return new Object[]{finalOutput};
                        }
                    }
                }
            }
            return new Object[0];
        }
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (pattern == 0 ? "All " : intOfExpr.toString(e, debug) + "of ") + "cached jsons";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        if (pattern == 1) {
            intOfExpr = (Expression<Integer>) exprs[0];
        }
        return true;
    }
}
