package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static cz.coffeerequired.api.Api.Records.PROJECT_DELIM;


@Name("Get all keys from Json object")
@Description("Returns all keys from the given json object. If the path is provided, it will return all keys from the object at that path.")
@Examples("""
        set {_json} to json from "{array: [{A: 1, B: 2, C: 3}]}"
        send all keys "array::0" of {_json}
        send all keys of {_json} # that will return all root-keys
        """)
@Since("4.0.1")
public class ExprGetAllKeys extends SimpleExpression<String> {

    private Expression<JsonElement> jsonElementExpression;
    private Expression<String> pathExpression;

    @Override
    protected @Nullable String[] get(Event event) {
        JsonElement jsonElement = jsonElementExpression.getSingle(event);
        if (jsonElement == null) return new String[0];
        if (!jsonElement.isJsonObject()) {
            SkJson.warning("Only json objects are supported");
            return new String[0];
        }
        if (pathExpression != null) {
            JsonAccessor json = new JsonAccessor(jsonElement);
            String path = pathExpression.getSingle(event);
            var tokens = PathParser.tokenize(path, PROJECT_DELIM);
            var searched = json.searcher.keyOrIndex(tokens);
            if (searched instanceof JsonObject j) jsonElement = j;
            else {
                SkJson.warning("Only json objects are supported");
                return new String[0];
            }
        }
        return jsonElement.getAsJsonObject().keySet().toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "all keys of " + (pathExpression != null ? pathExpression.toString(event, debug) : null) + " of json " + jsonElementExpression.toString(event, debug);

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pathExpression = (Expression<String>) expressions[0];
        jsonElementExpression = LiteralUtils.defendExpression(expressions[1]);
        return LiteralUtils.canInitSafely(expressions[1]);
    }
}
