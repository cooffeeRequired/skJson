package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.Parser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

@Name("Json has value/keys")
@Description({
        "Checks whether a JSON object or array contains the given keys or values.",
        "Use `has key` / `has value` or the `contains` alias; prefix with `all` to require every listed item."
})
@Since("4.2")
@Examples("""
        set {_json} to parse "[1, 2, 3, 8, ""TEST""]" as json
        set {_obj} to parse "{""A"": 1, ""B"": 2, ""C"": 3}" as json

        if {_json} has values 1 and 3:
            send true
        if {_obj} contains key "A":
            send true
        """)
public class CondJsonHas extends Condition {

    private Expression<JsonElement> jsonExpression;
    private Expression<?> objectsForCheck;
    private boolean negated;
    private boolean isValues;

    @Override
    public boolean check(Event event) {
        JsonElement json = jsonExpression.getSingle(event);
        if (json == null) return false;
        var objects = objectsForCheck.getAll(event);


        SkJson.debug("Json %s -< %s", json, Arrays.toString(objects));

        if (objects == null) return false;
        boolean found = true;


        for (Object object : objects) {
            if (isValues) {
                JsonElement needle = Parser.toJson(object);
                if (json instanceof JsonArray array) {
                    if (!array.contains(needle)) {
                        found = false;
                    }
                } else if (json instanceof JsonObject jsonObject) {
                    boolean contains = false;
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        JsonElement value = entry.getValue();
                        if (value != null && value.equals(needle)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        found = false;
                    }
                }
            } else {
                String key = object.toString();
                if (json instanceof JsonObject jsonElement) {
                    boolean contains = jsonElement.keySet().contains(key);
                    if (!contains) found = false;
                } else if (json instanceof JsonArray) {
                    SkJson.warning("Keys are not supported for arrays");
                    return false;
                }
            }
            if (!found) break;
        }
        SkJson.debug("Found keys %s in %s", Arrays.toString(objects), json);
        return found == !negated;
    }


    @Override
    public String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return jsonExpression.toString(event, debug) + " has " + (isValues ? "values" : "keys") + " " + objectsForCheck.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        negated = matchedPattern % 2 == 1;
        isValues = parseResult.hasTag("value") || parseResult.hasTag("values");
        jsonExpression = LiteralUtils.defendExpression(expressions[0]);
        objectsForCheck = LiteralUtils.defendExpression(expressions[1]);
        setNegated(negated);
        return LiteralUtils.canInitSafely(jsonExpression, objectsForCheck);
    }
}
