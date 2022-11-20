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
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Since("1.2.2")
@Name("Count of Value/Key")
@Description({"Displays the result of how many keys/values are found with JsonElement"})
@Examples({"on script load:",
        "\tset {-data} to json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tsend count value \"There\" of {-data}"
})
public class ExprCountOf extends SimpleExpression<Integer> {

    static {
        Skript.registerExpression(ExprCountOf.class, Integer.class, ExpressionType.COMBINED,
                "(count|number) (:value|:key) %string% of %jsonelement%"
        );
    }

    private Expression<String> str;
    private Expression<JsonElement> json;
    private int tag;


    @Override
    protected Integer @NotNull [] get(@NotNull Event e) {
        GsonUtils map = new GsonUtils();
        JsonElement jsonElement = json.getSingle(e);
        String search = str.getSingle(e);
        if(jsonElement == null) return new Integer[0];
        return new Integer[]{map.countOfKey(jsonElement, search, tag).getCount()};
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
        return "count "+ (tag == 1 ? "key" : "value") + "of " + json.toString(e, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        json = (Expression<JsonElement>) exprs[1];
        str = (Expression<String>) exprs[0];
        tag = parseResult.hasTag("key") ? 1 : 2;
        return true;
    }
}
