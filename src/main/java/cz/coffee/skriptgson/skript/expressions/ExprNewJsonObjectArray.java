package cz.coffee.skriptgson.skript.expressions;

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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Simple JsonObject/JsonArray")
@Description("You can create empty array or object.")
@Examples({"on load:",
        "\tsend new jsonobject",
        "\tsend new jsonarray"
})
@Since("2.0.4 - 3EA")

public class ExprNewJsonObjectArray extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprNewJsonObjectArray.class, JsonElement.class, ExpressionType.PROPERTY,
                "[new] (json[-]:object|json[-]:array)"
        );
    }

    private boolean object;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        JsonElement data;
        if (object) {
            data = new JsonObject();
        } else {
            data = new JsonArray();
        }
        return new JsonElement[]{data};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (object) ? new JsonObject().toString() : new JsonArray().toString();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        object = parseResult.hasTag("object");
        return true;
    }
}
