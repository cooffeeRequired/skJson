package cz.coffee.skriptgson.skript.effect;

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
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.adapters.Adapters;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Json to Skript-Type")
@Description({"You can deserialize correct json to skript-type, for example a tool a location, etc."})
@Examples({"command saveLocToJson:",
        "\ttrigger:",
        "\t\tset {-json} to new json from sender's location",
        "\t\tsend \"Saved location as JSON &e%{-json}%\"",
        "",
        "command teleporttoJson:",
        "\ttrigger",
        "\t\tset {-loc} to {-json} parsed as skript-type",
        "\t\tsend \"You will be tp to &b%{-loc}%&r from Json\"",
        "\t\tteleport sender to {-loc}"
})
@Since("2.0.0")
public class EffParse extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(EffParse.class, Object.class, ExpressionType.COMBINED, "[json] %jsonelement% parsed as [a] skript( type|-type)");
    }

    private Expression<JsonElement> json;

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return json.toString(e, debug) + " parsed as skript-type";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        json = (Expression<JsonElement>) exprs[0];
        return true;
    }

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        JsonElement bukkitObject = json.getSingle(e);
        return new Object[]{Adapters.fromJson(bukkitObject)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends ConfigurationSerializable> getReturnType() {
        return ConfigurationSerializable.class;
    }
}
