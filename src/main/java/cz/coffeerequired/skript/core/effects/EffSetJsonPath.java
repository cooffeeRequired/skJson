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
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Set json path")
@Description({
        "Writes a value at a JSON path, creating intermediate objects or arrays as needed.",
        "Preferred syntax: `set value at path … in … to …`."
})
@Since("5.6")
@Examples("""
        set {_data} to parse "{""user"": {""name"": ""Alex""}}" as json
        set value at path "user.name" in {_data} to "Bob"
        set value of json path "score" in {_data} to 100
        """)
public class EffSetJsonPath extends Effect {

    private Expression<String> path;
    private Expression<JsonElement> json;
    private Expression<Object> value;

    @Override
    protected void execute(Event event) {
        JsonElement element = json.getSingle(event);
        String pathValue = path.getSingle(event);
        Object rawValue = value.getSingle(event);
        if (element == null || pathValue == null) {
            return;
        }
        var tokens = PathParser.tokenize(pathValue, Api.Records.PROJECT_DELIM);
        new JsonAccessor(element).changer.value(tokens, rawValue == null ? com.google.gson.JsonNull.INSTANCE : Parser.toJson(rawValue));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set value at path " + path.toString(event, debug) + " in " + json.toString(event, debug)
                + " to " + value.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        path = defendExpression(expressions[0]);
        json = defendExpression(expressions[1]);
        value = defendExpression(expressions[2]);
        return canInitSafely(path, json, value);
    }
}
