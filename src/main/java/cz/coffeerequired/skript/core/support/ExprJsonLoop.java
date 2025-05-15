package cz.coffeerequired.skript.core.support;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.skript.AbstractLoop;
import cz.coffeerequired.skript.core.expressions.ExprJson;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.MatchResult;

public class ExprJsonLoop extends SimpleExpression<Object> {

    public enum LoopState {
        ID, VALUE, KEY, INDEX
    }

    private String name;
    boolean itsIntendedLoop = false;

    private AbstractLoop loop;
    private LoopState state;
    private ExprJson.JsonState jsonState;

    @Override
    public Class<?> getReturnType() {
        if (state.equals(LoopState.KEY)) return String.class;
        else if (state.equals(LoopState.INDEX)) return Integer.class;
        return ((ExprJson<?>) loop.getLoopedExpression()).getReturnType();
    }

    @Override
    public boolean isSingle() {
        // TODO -> make intended by state
        return true;
    }

    @Override
    public @Nullable Object[] get(Event event) {
        Object current = loop.getCurrent(event);
        ExprJson<?> jsonExpr = (ExprJson<?>) loop.getLoopedExpression();
        jsonState = jsonExpr.getState();
        if (current == null) return null;

        switch (state) {
            case VALUE -> {
                return new Object[] { getValue(current) };
            }
            case KEY -> {
                if (!(jsonState.equals(ExprJson.JsonState.KEY) || jsonState.equals(ExprJson.JsonState.ENTRIES))) {
                    SkJson.severe(getParser().getNode(), "Loop: %s. Cannot get loop-key while it is loop of %s.",
                            jsonExpr.toString(),
                            jsonState.toString().toLowerCase()
                    );
                    return null;
                }
                return new Object[] { getKey(current) };
            } case INDEX -> {
                if (!(jsonState.equals(ExprJson.JsonState.INDEX) || jsonState.equals(ExprJson.JsonState.INDICES))) {
                    SkJson.severe(getParser().getNode(), "Loop: %s. Cannot get loop-index while it is loop of %s.",
                            jsonExpr.toString(),
                            jsonState.toString().toLowerCase()
                    );
                    return null;
                }
                return new Object[] { getIndex(current) };
            }

        }
        return null;
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {	//TODO
        if (e == null)
            return name;
        if (itsIntendedLoop) {
            final Object current = loop.getCurrent(e);
            Object[] objects = ((ExprJson<?>) loop.getLoopedExpression()).get(e);

            if (current == null || objects == null)
                return Classes.getDebugMessage(null);
            return Classes.getDebugMessage(current);
        }
        return Classes.getDebugMessage(loop.getCurrent(e));
    }

    @SuppressWarnings("unchecked")
    private Object getValue(Object current) {
        if (current instanceof Map.Entry<?,?> e_) {
            var entry = (Map.Entry<String, JsonElement>) e_;
            return Parser.fromJson(entry.getValue());
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private String getKey(Object current) {
        if (current instanceof Map.Entry<?,?> e_) {
            var entry = (Map.Entry<String, JsonElement>) e_;
            return entry.getKey();
        }
        return current.toString();
    }

    private Object getIndex(Object current) {
        return current;
    }

    @Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        name = parser.expr;

        MatchResult numberOfLoop = !parser.regexes.isEmpty() ? parser.regexes.getFirst() : null;
        var number = SkriptUtils.getLoopIndex(numberOfLoop);

        String expressionName = name.split("-")[1];
        SkJson.debug("expressionName: %s, name: %s", expressionName, name);

        int i = -1;
        if (number != null) i = number;

        AbstractLoop loop = SkriptUtils.getSecLoop(i, expressionName);

        if (loop == null) {
            SkJson.severe(getParser().getNode(), "here are multiple loops that match loop-%s. Use loop-%s-1/2/3/etc. to specify which loop's value you want.", expressionName, expressionName);
            return false;
        }

        if (loop.getObject() == null) {
            SkJson.severe(getParser().getNode(), "There's no loop that matches 'loop-%s'", expressionName);
            return false;
        }

        if (loop.getLoopedExpression() instanceof ExprJson<?> exprJsonExpression) {
            jsonState = exprJsonExpression.getState();
            switch (parser.mark) {
                case 1 -> state = LoopState.KEY;
                case 2 -> state = LoopState.VALUE;
                case 5 -> state = LoopState.INDEX;
            }
        } else {
            SkJson.severe(getParser().getNode(), "A 'loop-%s' can only be used in a json expression loop ie. 'xxxx' ", expressionName);
            return false;
        }
        this.loop = loop;
        return true;
    }
}
