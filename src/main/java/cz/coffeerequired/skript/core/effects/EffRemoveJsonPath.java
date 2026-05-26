package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Remove json path")
@Description("Removes a value at the given path from a JSON object or array.")
@Since("5.6")
@Examples("""
        set {_data} to json from "{a: 1, nested: {x: 1}}"
        remove path "nested.x" from {_data}
        delete path "a" in {_data}
        """)
public class EffRemoveJsonPath extends Effect {

    private Expression<JsonElement> json;
    private Expression<String> path;

    @Override
    protected void execute(Event event) {
        JsonElement element = json.getSingle(event);
        String pathValue = path.getSingle(event);
        if (element == null || pathValue == null) {
            return;
        }
        var tokens = PathParser.tokenize(pathValue, Api.Records.PROJECT_DELIM);
        new JsonAccessor(element).remover.byKey(tokens);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "remove path " + path.toString(event, debug) + " from " + json.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        json = defendExpression(expressions[1]);
        path = defendExpression(expressions[0]);
        return canInitSafely(json, path);
    }
}
