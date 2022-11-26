package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
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
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.util.Utils.color;
import static cz.coffee.skriptgson.util.Utils.newGson;

@Since("1.0")
@Name("Pretty printed JSON")
@Examples({"on load:",
        "\tset {_json} to \"{'test': true}\"",
        "\tsend {_json} with pretty print"
})

public class ExprPrettyPrint extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprPrettyPrint.class,
                Object.class,
                ExpressionType.COMBINED,
                "%jsonelement% [with] pretty(-| )print[(ing|ed)]");
    }

    private Expression<JsonElement> exprPrint;


    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        exprPrint = (Expression<JsonElement>) exprs[0];
        return true;
    }

    public String @NotNull [] get(@NotNull Event event) {
        return new String[]{color("\n&f" + newGson().toJson(exprPrint.getSingle(event))
                .replaceAll("(true)", "§a$0§r")
                .replaceAll("(false)", "§c$0§r")
                .replaceAll("([{}])", "§7$0§r")
                .replaceAll("([\\[\\]])", "§6$0§r")
                .replaceAll("((?<!\\\\)['\"])((?:.(?!(?<!\\\\)\\1))*.?)\\1", "§7$0§r")
                .replaceAll("(\"(.*)\")", "§7$1§r")
                .replaceAll("(null)", "§5$0§r")
                .replaceAll("(\")(.*?)(\")", "$1§3$2§r$3")
                .replaceAll("\"", "§r\"")
                .replaceAll("(?<=\\s|^)\\d+", "§3$0§r"))};
    }

    public @NotNull String toString(Event event, boolean debug) {
        return "";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    public @NotNull Class<String> getReturnType() {
        return String.class;
    }

}
