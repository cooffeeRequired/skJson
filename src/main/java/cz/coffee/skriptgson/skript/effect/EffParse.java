package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

public class EffParse extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(EffParse.class, Object.class, ExpressionType.COMBINED, "parse %jsonelement%");
    }

    private Expression<JsonElement> json;

    @Override
    public String toString(@Nullable Event e, boolean debug) {

        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        json = (Expression<JsonElement>) exprs[0];
        return true;
    }

    @Override
    protected @Nullable Object[] get(Event e) {
        JsonElement json2 = json.getSingle(e);
        ConfigurationSerializable cg = hierarchyAdapter().fromJson(json2, ConfigurationSerializable.class);

        System.out.println(cg);

        return new Object[]{cg};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ConfigurationSerializable> getReturnType() {
        return ConfigurationSerializable.class;
    }
}
