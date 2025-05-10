package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.lang.Variable.SEPARATOR;
import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("formatting Json to Skript list variable")
@Description("Maps the json to a skript list variable. This is used to map the json to a skript list variable.")
@Since("1.9, 2.9 - Support mapping json from functions, 4.1 performance increase")
@Examples("""
            map {_json} to {_mapped::*}
            send {_mapped::b} to console
        """)
public class EffMapJson extends Effect {

    private Expression<JsonElement> variable;
    private VariableString variableNaming;
    private boolean isLocal;
    private boolean async;

    @Override
    protected void execute(Event event) {
        Object variableContent = variable.getSingle(event);
        JsonElement json = Parser.toJson(variableContent);
        String variableName = variableNaming.getSingle(event);
        if (variableName == null || json == null) {
            throw new IllegalArgumentException("Variable name or json is null");
        }
        variableName = variableName.substring(0, variableName.length() - 3);
        if (async) {
            String finalVariableName = variableName;
            new BukkitRunnable() {
                @Override
                public void run() {
                    SkriptUtils.convertJsonToSkriptVariable(finalVariableName + SEPARATOR, json, event, isLocal);
                }
            };
        } else {
            SkriptUtils.convertJsonToSkriptVariable(variableName + SEPARATOR, json, event, isLocal);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return Classes.getDebugMessage(variable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        Expression<?> unparsedObject = defendExpression(expressions[1]);
        async = parseResult.hasTag("async");
        if (!unparsedObject.getReturnType().isAssignableFrom(JsonElement.class)) {
            throw new IllegalArgumentException("You can map only Json or stringify json (String)");
        }
        variable = (Expression<JsonElement>) expressions[0];

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
