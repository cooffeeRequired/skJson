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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.JsonUtils.countKeys;
import static cz.coffee.core.utils.JsonUtils.countValues;

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
 * Created: Friday (3/10/2023)
 */

@Name("Count of objects/phrases")
@Description("You can get the exact number of identical keys or values from the entire json because `count of` works recursively.")
@Examples({
        "set {_json} to json from string \"{'A': [{'B': {}}], 'X': {}, 'UN': 'A'}\"",
        "add diamond sword to {_json} for given path \"A[1]:B\"",
        "send {_json}",
        "send count of diamond sword in {_json}",
        "send number of key \"A\" in {_json}",
        "send count of \"A\" in {_json}"
})
@Since("2.8.0 performance & clean")

public class ExprJsonCountOf extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(ExprJsonCountOf.class, Integer.class, ExpressionType.SIMPLE, "(count|number) of (:key|[value]) %object% in %json%");
    }

    private boolean isValue;
    private Expression<?> valueExpression;
    private Expression<JsonElement> jsonElementExpression;

    @Override
    protected @Nullable Integer @NotNull [] get(@NotNull Event e) {
        JsonElement json = jsonElementExpression.getSingle(e);
        Object unparsedValue = valueExpression.getSingle(e);
        assert json != null;
        if (isValue) {
            JsonElement parsed = parseItem(unparsedValue, valueExpression, e);
            assert parsed != null;
            return new Integer[]{countValues(parsed, json)};
        } else {
            if (unparsedValue instanceof String) {
                return new Integer[]{countKeys((String) unparsedValue, json)};
            }
        }
        return new Integer[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "count of " + (isValue ? valueExpression.toString(e, debug) : "key " + valueExpression.toString(e, debug)) + " in " + jsonElementExpression.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        isValue = !parseResult.hasTag("key");
        jsonElementExpression = (Expression<JsonElement>) exprs[1];
        valueExpression = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(valueExpression);
    }
}
