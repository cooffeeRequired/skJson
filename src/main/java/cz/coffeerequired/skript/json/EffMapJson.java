package cz.coffeerequired.skript.json;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static ch.njol.skript.lang.Variable.SEPARATOR;
import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("formatting Json to Skript list variable")
@Description("Its allow convert Json to skript list variable")
@Since("1.9, 2.9 - Support mapping json from functions, 4.1 performance increase")
@Examples("""
    map {_json} to {_mapped::*}
    send {_mapped::b} to console
""")
public class EffMapJson extends Effect {

    private Expression<?> variable;
    private VariableString variableNaming;
    private boolean isLocal;
    private boolean async;

    @Override
    protected void execute(Event event) {
        Object variableContent = variable.getSingle(event);
        JsonElement json = GsonParser.toJson(variableContent);
        String variableName = variableNaming.getSingle(event);
        if (variableName == null || json == null) {
            throw new IllegalArgumentException("Variable name or json is null");
        }
        variableName = variableName.substring(0, variableName.length() - 3);
        if (async) {
            String finalVariableName = variableName;
            CompletableFuture.runAsync(() -> SkriptUtils.convertJsonToSkriptVariable(finalVariableName + SEPARATOR, json, event, isLocal));
        } else {
            SkriptUtils.convertJsonToSkriptVariable(variableName + SEPARATOR, json, event, isLocal);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return Classes.getDebugMessage(variable);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        Expression<?> unparsedObject = defendExpression(expressions[1]);
        async = parseResult.hasTag("async");
        if (!unparsedObject.getReturnType().isAssignableFrom(JsonElement.class)) {
            throw new IllegalArgumentException("You can map only Json or stringify json (String)");
        }
        variable = expressions[0];
        if (unparsedObject instanceof Variable<?> var) {
            if (var.isList()) {
                isLocal = var.isLocal();
                variableNaming = var.getName();
                return canInitSafely(unparsedObject);
            }
        }
        return false;
    }
}
