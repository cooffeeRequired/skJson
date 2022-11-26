package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Since("1.3.0")
@Name("[Reworked] Mapping JSON to List.")
@Description("")
@Examples({"on script load:",
        "\tset {_json} to new json from string \"{'players': {'coffee': {'data': {'name': 'coffeeRequired'}}}}\"",
        "\tmap {_json} to {_json::*}",
        "send {_json::*}"
})


public class EffMapJson extends Effect {

    static {
        Skript.registerEffect(EffMapJson.class,
                "map [json from] %jsonelement% to %objects%"
        );
    }

    private Expression<Object> raw_json;
    private VariableString variableString;
    private boolean isLocal;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        raw_json = (Expression<Object>) exprs[0];
        Expression<?> objects = exprs[1];
        if (objects instanceof Variable<?> variable) {
            if (variable.isList()) {
                variableString = variable.getName();
                isLocal = variable.isLocal();
                return true;
            }
        }
        SkriptGson.severe(variableString + " variable is not a type list variable");
        return false;
    }


    @Override
    protected void execute(@NotNull Event e) {

        JsonElement element;

        if (raw_json == null) return;
        element = (JsonElement) raw_json.getSingle(e);

        String clearVarName = variableString.toString(e).toLowerCase(Locale.ENGLISH).substring(0, variableString.toString(e).length() - 3);

        GsonUtils utils = new GsonUtils();

        utils.mapJson(e, element, clearVarName, isLocal);


    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "";
    }
}
