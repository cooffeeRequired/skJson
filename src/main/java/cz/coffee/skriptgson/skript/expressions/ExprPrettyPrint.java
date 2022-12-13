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
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.utils.Utils.color;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("Json outputting as pretty printed.")
@Description("You can do colorize and smart output of your current json.")
@Examples({"on load:",
        "\tset {_json} to json from text \"{'player': 'your name', 'number': 10, 'bool': false}\"",
        "\tsend {_json} pretty printed"
})
@Since("2.0.0")

public class ExprPrettyPrint extends SimpleExpression<String> {

    private static final String RESET = "§r";

    static {
        Skript.registerExpression(ExprPrettyPrint.class, String.class, ExpressionType.COMBINED,
                "%jsonelement% [with] pretty print[(ing|ed)]"
        );
    }

    private Expression<JsonElement> jsonElementExpression;

    @Override
    protected @Nullable String @NotNull [] get(@NotNull Event e) {
        JsonElement json = jsonElementExpression.getSingle(e);
        String jsonString = hierarchyAdapter().toJson(json);
        String coloredJsonString = color(jsonString
                .replaceAll("(true)", "§a$0" + RESET)
                .replaceAll("(false)", "§c$0" + RESET)
                .replaceAll("(null)", "§5$0" + RESET)
                .replaceAll("([{}])", "§7$0" + RESET)
                .replaceAll("([\\[\\]])", "§6$0" + RESET)
                .replaceAll("(\")(.*?)(\")", "$1§f$2$3" + RESET)
                .replaceAll("(?<=\\s|^)\\d+", "§3$0" + RESET)
        );

        String prettyPrintedJsonString = "\n" + coloredJsonString.replaceAll("\\\\\"", "\"");
        return new String[]{prettyPrintedJsonString};
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
        return jsonElementExpression.toString(e, debug) + " pretty printed";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        jsonElementExpression = (Expression<JsonElement>) exprs[0];
        return true;
    }
}
