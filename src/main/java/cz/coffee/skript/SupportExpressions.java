package cz.coffee.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static cz.coffee.core.utils.AdapterUtils.parseItem;


@NoDoc
public class SupportExpressions extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(SupportExpressions.class, JsonElement.class, ExpressionType.SIMPLE,
                "(:1st|:first|:2nd|:second|:3rd|:third|:last|%integer%) element"
        );
    }

    private int pattern;
    private SkriptParser.ParseResult result;
    private Expression<Integer> intExpression;
    private static final JsonObject JSON_OBJECT = new JsonObject();


    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        ArrayList<JsonElement> returnItems = new ArrayList<>();
        if (pattern == 0) {
            String type = result.tags.size() > 0 ? result.tags.get(0) : "expression";
            int i;
            i = switch (type) {
                case "1st", "first" -> 1;
                case "2nd", "second" -> 2;
                case "3rd", "third" -> 3;
                case "last" -> -99;
                case "expression" -> -2;
                default -> -1;
            };
            if (i == -1 || i == -2) {
                Integer iIndex = intExpression.getSingle(e);
                if (iIndex != null) i = iIndex;
            }
            if (i == 0) return new JsonElement[0];
            JsonObject element = JSON_OBJECT;
            element.add("element expression", parseItem(--i, intExpression, e));
            returnItems.add(element);
        }
        return returnItems.toArray(new JsonElement[0]);
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
        return Classes.getDebugMessage(e);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        result = parseResult;
        pattern = matchedPattern;
        if (pattern == 0) {
            intExpression = (Expression<Integer>) exprs[0];
        }
        return true;
    }
}
