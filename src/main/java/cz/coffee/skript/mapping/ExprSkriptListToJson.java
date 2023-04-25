package cz.coffee.skript.mapping;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.core.mapping.JsonMap;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Array or List formatted to JSON.")
@Description({"It allows you to convert the sheet back to Json!",
        "Value changes don't work for nested objects, to change the values of a nested object use Change"})
@Examples({"on script load:",
        "\tset {-json} to json from string \"{'test': [1,2,3,false,null,'some'], 'test2': {'something': false}}\"",
        "\tmap {-json} to {_json::*}",
        "\tsend \"&9%{_json::*}'s form with pretty print%\""
})
@Deprecated
@Since("1.3.0")
public class ExprSkriptListToJson extends SimpleExpression<JsonElement> {

    static {
        PropertyExpression.register(ExprSkriptListToJson.class, JsonElement.class, "form[atted json]", "objects");
    }

    private VariableString variableString;
    private boolean isLocal;


    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        String variableName = variableString.toString(e);
        String var = (variableName.substring(0, variableName.length() - 1));
        JsonElement element = JsonMap.Json.convert(var, isLocal, false, e);
        return new JsonElement[]{element};
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
        return "form of " + variableString.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        SkJson.warning("Using a deprecated API, The form of %objects%. It's may nor works properly with 2.7 Skript");
        Expression<?> objects = exprs[0];
        if (objects instanceof Variable<?> var) {
            if (var.isList()) {
                isLocal = var.isLocal();
                variableString = var.getName();
            } else {
                Skript.error("You can map json only to List variable.", ErrorQuality.SEMANTIC_ERROR);
                return false;
            }
        } else {
            Skript.error("You can change only json variables", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        return true;
    }
}
