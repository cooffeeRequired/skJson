package cz.coffee.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.core.AdapterUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffee.core.NumberUtils.parsedNumber;
import static cz.coffee.core.Util.jsonToObject;

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

@Since("2.8.0 - performance & clean")

public class ExprJsonLoopExpressions extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprJsonLoopExpressions.class, Object.class, ExpressionType.SIMPLE,
                "[the] [json-]loop-(value|:element|:key)[-<(\\d+)>]"
        );
    }

    private boolean isKey;

    private String name;

    private SecLoop loop;
    private boolean isCanceled =  false;

    @Override
    @SuppressWarnings("unchecked")
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        if (isCanceled) return new Object[0];

        WeakHashMap<String, Object> o;
        try {
            o = (WeakHashMap<String, Object>) loop.getCurrent(e);
        } catch (ClassCastException ignored) {
            return new Object[0];
        }
        if (o == null) return new Object[0];

        for (Map.Entry<String, Object> entry : o.entrySet()) {
            if (isKey) return new String[] {entry.getKey()};
            Object[] one = (Object[]) Array.newInstance(getReturnType(), 1);
            if (entry.getValue() instanceof JsonElement) {
                Object assigned = AdapterUtils.assignFrom((JsonElement) entry.getValue());
                if (assigned == null)
                    assigned = jsonToObject((JsonElement) entry.getValue());
                one[0] = assigned;
            } else {
                one[0] = entry.getValue();
            }
            return one;
        }
        return new Object[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        if (loop == null) return Object.class;
        return loop.getLoopedExpression().getReturnType();
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if (e == null) return name;
        return Classes.getDebugMessage(loop.getCurrent(e));
    }
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        MatchResult mResult = parseResult.regexes.size() >0 ? parseResult.regexes.get(0) : null;
        Object group = -1;
        if (mResult != null) {
            group = mResult.group(0);
        }
        int i = -1;
        isKey = parseResult.hasTag("key");
        String firstField = parseResult.expr, s = "";
        Pattern p = Pattern.compile("loop-(.+)(.)");
        Matcher m = p.matcher(firstField);
        if (m.matches()) {
            String[] split = firstField.split("-");
            s = split[1];
            i = parsedNumber(group);
        }
        Class<?> c = Classes.getClassFromUserInput(s);
        name = s;
        int j = 1;
        SecLoop loop = null;
        for (SecLoop l : getParser().getCurrentSections(SecLoop.class)) {
            if ((c != null && c.isAssignableFrom(l.getLoopedExpression().getReturnType())) || "value".equals(s) || l.getLoopedExpression().isLoopOf("value") || ExprJsonElements.isChangedLoopOf(s)) {
                if (j < i) {
                    j++;
                    continue;
                }
                if (loop != null) {
                    isCanceled = true;
                    break;
                }
                loop = l;
                if (j == i) break;
            }
        }
        if (loop == null) {
            Skript.error("There's no loop that matches 'loop-" + s + "'", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        if (isCanceled)
            Skript.error("There are multiple loops that match loop-" + s + ". Use loop-" + s + "-1/2/3/etc. to specify which loop's value you want.", ErrorQuality.SEMANTIC_ERROR);
        this.loop = loop;
        return true;
    }

}
