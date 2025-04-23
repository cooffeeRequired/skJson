package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Formatting skript variable to json")
@Description("Formats the skript variable to json. This is used to format the skript variable to json.")
@Since("4.1 - API UPDATE")
@Examples("""
            set {_test::A} to false
            set {_test::B} to "Raw"
            set {_test::B::C} to 100 // that will be ignored, because it violates the json standard
            set {_test::B::C::*} to 1, 2, 4, false, true, "A" and location(0, 1, 2) and world("world")
        
            set {_json} to {_test::*}'s form
        
            send {_json} as uncolored pretty printed
        """)
public class ExprFormattingJsonToVariable extends SimpleExpression<JsonElement> {

    private VariableString variableNaming;
    private boolean isLocal;

    @Override
    protected @Nullable JsonElement[] get(Event event) {
        String variableName = variableNaming.getSingle(event);
        var variable = SkriptUtils.getListVariable(variableName, event, isLocal);
        var json = SkriptUtils.convertSkriptVariableToJson(variable);
        return json == null ? new JsonElement[0] : new JsonElement[]{json};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "formating new json from skript variable";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        Expression<?> object = expressions[0];
        if (object instanceof Variable<?> variable) {
            if (!variable.isList())
                throw new IllegalArgumentException("Variable is need to be list");

            isLocal = variable.isLocal();
            variableNaming = variable.getName();
            return true;
        } else {
            throw new IllegalArgumentException("Variable is required");
        }
    }
}
