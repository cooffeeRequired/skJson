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
import cz.coffee.core.ColoredJson;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Pretty json")
@Description({
        "Allows you to better parse json",
        "<pre>",
        "{",
        "\t\"test\": \"skJson\"",
        "\t\"Object\": {",
        "\t\t\"new\": \"data\"",
        "\t}",
        "</pre>"
})
@Examples({
        "set {_json} to json from \"{'test': 'skJson', 'Object' : {'new': 'data'}}\"",
        "send {_json} with pretty print"
})
@Since("2.8.0 - performance & clean")


public class ExprPrettyPrint extends SimpleExpression<String> {

    static Expression<JsonElement> jsonExpression;

    static {
        Skript.registerExpression(ExprPrettyPrint.class, String.class, ExpressionType.SIMPLE, "%json% with pretty print");
    }

    @Override
    protected @Nullable String @NotNull [] get(@NotNull Event e) {
        JsonElement json = jsonExpression.getSingle(e);
        return new String[]{new ColoredJson(json).getOutput()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return jsonExpression.toString(e, debug) + " with pretty print";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        jsonExpression = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(jsonExpression);
    }
}
