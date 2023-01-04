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
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.ERROR;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.*;

@Name("Mapping Json to List")
@Description("Mapping json to the List and get those values")
@Examples({"on load:",
        "\tset {_json} to json from string \"{'test': 'test2': {}}\"",
        "\tmap {_json} to {_json::*}",
        "\tsend {_json::*}"
})
@Since("1.4.0")

public class EffMapJson extends Effect {

    static {
        Skript.registerEffect(EffMapJson.class, "map [json from] %jsonelement% to %objects%");
    }

    private Expression<JsonElement> jsonElementExpression;
    private VariableString variableString;
    private boolean isLocal;

    @Override
    protected void execute(@NotNull Event e) {
        JsonElement json;
        json = jsonElementExpression.getSingle(e);
        if (json == null) return;
        GsonUtils.GsonMapping.jsonToList(e, variableString.toString(e).substring(0, variableString.toString(e).length() - 3), json, isLocal);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "map json to json List";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        Expression<Object> objectExpression = LiteralUtils.defendExpression(exprs[1]);
        jsonElementExpression = (Expression<JsonElement>) exprs[0];

        if (objectExpression instanceof Variable<Object> var) {
            if (var.isList()) {
                isLocal = var.isLocal();
                variableString = var.getName();
            } else {
                sendErrorMessage(VAR_NEED_TO_BE_LIST, ERROR);
                return false;
            }
        } else {
            sendErrorMessage(ONLY_JSONVAR_IS_ALLOWED, ERROR);
            return false;
        }
        return LiteralUtils.canInitSafely(objectExpression);
    }
}
