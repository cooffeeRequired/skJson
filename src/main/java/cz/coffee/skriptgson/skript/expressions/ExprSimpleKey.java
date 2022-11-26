package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Simple Key for Changer")
@Since("1.3.1")

public class ExprSimpleKey extends SimpleExpression<JsonPrimitive> {

    private Expression<Object> key;

    static {
        Skript.registerExpression(ExprSimpleKey.class, JsonPrimitive.class, ExpressionType.COMBINED, "~%object%");
    }

    @Override
    protected @Nullable JsonPrimitive @NotNull [] get(@NotNull Event e) {
        Object object = key.getSingle(e);
        JsonElement json = null;

        if (object instanceof String) {
            json = new JsonPrimitive((String) object);
        } else if (object instanceof Double) {
            json = new JsonPrimitive(((Double) object).intValue());
        } else if (object instanceof Number) {
            json = new JsonPrimitive(((Number) object).intValue());
        } else if (object instanceof Boolean) {
            json = new JsonPrimitive((Boolean) object);

        }
        if (json == null) return new JsonPrimitive[0];
        return new JsonPrimitive[]{json.getAsJsonPrimitive()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends JsonPrimitive> getReturnType() {
        return JsonPrimitive.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return key.toString(e, debug) + " as JsonPrimitive";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        key = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(key);
    }
}
