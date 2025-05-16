package cz.coffeerequired.skript.core.support;

import ch.njol.skript.lang.util.SimpleExpression;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.skript.loops.AbstractExpressionInitializer;
import cz.coffeerequired.api.skript.loops.AbstractLoop;
import cz.coffeerequired.api.skript.loops.AbstractLoopExpression;
import cz.coffeerequired.skript.core.expressions.ExprJson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ExprJsonLoop extends AbstractLoopExpression<Object> {

    private LoopState state;
    private ExprJson.JsonState jsonState;

    public enum LoopState {
        ID, VALUE, KEY, INDEX
    }

    @Override
    public boolean isIntendedLoop(AbstractLoop loop, final AbstractExpressionInitializer initializer) {
        if (loop.getLoopedExpression() instanceof ExprJson<?> exprJsonExpression) {
            jsonState = exprJsonExpression.getState();
            switch (initializer.parser().mark) {
                case 1 -> state = LoopState.KEY;
                case 2 -> state = LoopState.VALUE;
                case 5 -> state = LoopState.INDEX;
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends SimpleExpression> getExpressionToLoop() {
        return ExprJson.class;
    }

    @Override
    public Class<?> getReturnType() {
        if (state.equals(LoopState.KEY)) return String.class;
        else if (state.equals(LoopState.INDEX)) return Integer.class;
        return loop.getLoopedExpression().getReturnType();
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
    public boolean isSingle() {
        return true;
    }
}
