package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.Util.PluginUtils.color;

@SuppressWarnings({"uncheked","unused"})
public class ExprPrettyPrint extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprPrettyPrint.class,
                Object.class,
                ExpressionType.COMBINED,
                "%jsonelement% [with] pretty(-| )print[(ing|ed)]");
    }

    private Expression<JsonElement> exprPrint;

    @SuppressWarnings({"unchecked","unused"})
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        exprPrint = (Expression<JsonElement>) exprs[0];
        return true;
    }

    public String[] get(@NotNull Event event) {
        return new String[]{color(new GsonBuilder().setPrettyPrinting().create().toJson(exprPrint.getSingle(event))
                .replaceAll("(true)", "§a$0§r")
                .replaceAll("(false)", "§c$0§r")
                .replaceAll("(null)", "§7$0§r")
                .replaceAll("(\\{|})", "§7$0§r")
                .replaceAll("(\\[|])", "§6$0§r")
                .replaceAll("((?<![\\\\])['\\\"])((?:.(?!(?<![\\\\])\\1))*.?)\\1", "§7$0§r")
                .replaceAll("(?<=\\s|^)\\d+(?=)", "§3$0§r"))};
    }

    public String toString(Event event, boolean debug) {
        return "";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    public Class<String> getReturnType() {
        return String.class;
    }

}
