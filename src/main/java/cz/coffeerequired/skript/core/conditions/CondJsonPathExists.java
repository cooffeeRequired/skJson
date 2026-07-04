package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Json path exists")
@Description({
        "Checks whether a path exists inside a JSON value without changing it.",
        "Aliases: `contains path …` / `does not contain path …`."
})
@Since("5.5")
@Examples("""
        set {_json} to parse "{""user"": {""name"": ""Alex""}}" as json
        if {_json} has path "user.name":
            send "found"
        if {_json} does not have path "user.age":
            send "missing"
        """)
public class CondJsonPathExists extends Condition {

    private Expression<JsonElement> jsonExpression;
    private Expression<String> pathExpression;
    private boolean negated;

    @Override
    public boolean check(Event event) {
        JsonElement json = jsonExpression.getSingle(event);
        String path = pathExpression.getSingle(event);
        if (json == null || path == null) {
            return negated;
        }
        boolean exists = JsonAccessorUtils.pathExists(json, PathParser.tokenize(path, Api.Records.PROJECT_DELIM));
        return exists != negated;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return jsonExpression.toString(event, debug) + (negated ? " does not have path " : " has path ")
                + pathExpression.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        negated = matchedPattern % 2 == 1;
        jsonExpression = defendExpression(expressions[0]);
        pathExpression = defendExpression(expressions[1]);
        setNegated(negated);
        return canInitSafely(jsonExpression, pathExpression);
    }
}
