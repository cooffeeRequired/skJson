package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;


@Since("1.3.0")
@Name("Array or List formatted to JSON.")
@Description({"It allows you to convert the sheet back to Json!",
        "Value changes don't work for nested objects, to change the values of a nested object use Change"})
@Examples({"on script load:",
        "\tset {-json} to new json from string \"{'test': [1,2,3,false,null,'some'], 'test2': {'something': false}}\"",
        "\tmap {-json} to {_json::*}",
        "\tsend \"&9%{_json::*}'s form with pretty print%\""
})


public class ExprArrayToJson extends SimpleExpression<JsonElement> {


    static {
        PropertyExpression.register(ExprArrayToJson.class, JsonElement.class, "(form|formatted json)", "objects");
    }


    private VariableString variableString;
    private boolean isLocal;


    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        Expression<?> objects = exprs[0];
        if(objects instanceof Variable<?> variable) {
            if(variable.isList()) {
                variableString = variable.getName();
                isLocal = variable.isLocal();
                return true;
            }
        }
        SkriptGson.severe(variableString + " variable is not a type list variable");
        return false;
    }


    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        GsonUtils utils = new GsonUtils();
        String variableName = variableString.toString(e).toLowerCase(Locale.ENGLISH);
        JsonElement json = utils.mapList(e, variableName.substring(0, variableName.length() - 1), false, isLocal);
        return new JsonElement[]{json};
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
        return "{"+(isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "")+variableString.toString(e, debug) + "}'form";
    }
}
