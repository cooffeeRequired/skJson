package cz.coffee.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.utils.JsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.Util.extractKeys;

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
 * Created: Sunday (3/12/2023)
 */

@Name("Json has value/key")
@Description({" You can check if the inserted keys or values already in your specified json"})
@Examples({"on load:",
        "    set {_json} to json from string \"{'test5': [1], 'test6': ['key', 'key2', 'key3']}\"",
        "    if {_json} has keys \"test5\", \"test6\", \"A\":",
        "        send true",
        "    else:",
        "        send false"
})
@Since("2.8.0 - performance & clean")

public class CondJsonHas extends Condition {

    static {
        Skript.registerCondition(CondJsonHas.class,
                "%json% [:directly] has (:value|:key)[s] %objects%",
                "%json% [:directly] does(n't| not) have (:value|:key)[s] %objects%"
        );
    }

    private int line;
    private boolean isValues, directly;
    private Expression<?> expressionRaw;
    private Expression<JsonElement> jsonElementExpression;


    @Override
    public boolean check(@NotNull Event e) {
        JsonElement json = jsonElementExpression.getSingle(e);
        Object[] values = expressionRaw.getArray(e);
        if (json == null) return false;

        boolean found = true;

        for (Object value : values) {
            if (isValues) {
                JsonElement element = parseItem(value, expressionRaw, e);
                if (!JsonUtils.checkValues(element, json)) {
                    found = false;
                    break;
                }
            } else {
                String element = (String) value;

                if (directly) {
                    final LinkedList<String> list = extractKeys(element, null);
                    final JsonElement result = JsonUtils.getByKey(json, list);
                    if (result == null || result.isJsonNull()) {
                        found = false;
                        break;
                    }
                } else {
                    if (!JsonUtils.checkKeys(element, json)) {
                        found = false;
                        break;
                    }
                }
            }
        }
        return (line == 0) == found;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return jsonElementExpression.toString(e, debug) +" has " + (isValues ? "values" : "keys") + " " + expressionRaw.toString(e, debug);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        directly = parseResult.hasTag("directly");
        line = matchedPattern;
        isValues = parseResult.hasTag("value");
        setNegated(line == 1);
        expressionRaw = LiteralUtils.defendExpression(exprs[1]);
        jsonElementExpression = (Expression<JsonElement>) exprs[0];
        if (!isValues) {
            if (expressionRaw.getReturnType() != String.class) {
                return false;
            }
        }
        return LiteralUtils.canInitSafely(expressionRaw);
    }
}
