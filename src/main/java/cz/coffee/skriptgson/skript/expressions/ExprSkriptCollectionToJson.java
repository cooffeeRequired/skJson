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
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;


@Name("Array or List formatted to JSON.")
@Description({"It allows you to convert the sheet back to Json!",
        "Value changes don't work for nested objects, to change the values of a nested object use Change"})
@Examples({"on script load:",
        "\tset {-json} to new json from string \"{'test': [1,2,3,false,null,'some'], 'test2': {'something': false}}\"",
        "\tmap {-json} to {_json::*}",
        "\tsend \"&9%{_json::*}'s form with pretty print%\""
})
@Since("1.3.0")


public class ExprSkriptCollectionToJson extends SimpleExpression<JsonElement> {

    static {
        PropertyExpression.register(ExprSkriptCollectionToJson.class, JsonElement.class, "(form|formatted json)", "objects");
    }

    private VariableString variableString;
    private boolean isLocal;


    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        GsonErrorLogger err = new GsonErrorLogger();
        Expression<?> objects = exprs[0];
        if (objects instanceof Variable<?> var) {
            if (var.isList()) {
                isLocal = var.isLocal();
                variableString = var.getName();
            } else {
                SkriptGson.severe(err.VAR_NEED_TO_BE_LIST);
                return false;
            }
        } else {
            SkriptGson.severe(err.ONLY_JSONVAR_IS_ALLOWED);
            return false;
        }
        return true;
    }


    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        String variableName = variableString.toString(e);
        JsonElement json = GsonUtils.GsonMapping.listToJson(e, variableName.substring(0, variableName.length() - 1), isLocal);
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
        return variableString.toString() + "'s form";
    }
}