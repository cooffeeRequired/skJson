package cz.coffeerequired.skript.core.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Type of Json")
@Description("You can get type of given Json.")
@Since("2.7")
@Examples({
        "set {_json} to json from \"{data: {}}\"",
        "if json type of {_j} is json object"
})
public class CondJsonType extends Condition {

    private Expression<JsonElement> json;
    private Type type;
    private boolean negated;

    @Override
    public boolean check(Event event) {
        JsonElement jsonElement = json.getSingle(event);
        if (jsonElement == null) return false;

        return negated == !switch (type) {
            case OBJECT -> jsonElement.isJsonObject();
            case ARRAY -> jsonElement.isJsonArray();
            case PRIMITIVE -> jsonElement.isJsonPrimitive();
            case NULL -> jsonElement.isJsonNull();
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        assert event != null;
        return "type of " + json.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        negated = matchedPattern % 2 == 1;
        json = LiteralUtils.defendExpression(expressions[0]);
        if (parseResult.hasTag("object") || parseResult.hasTag("json-object")) {
            type = Type.OBJECT;
        } else if (parseResult.hasTag("array") || parseResult.hasTag("json-array")) {
            type = Type.ARRAY;
        } else if (parseResult.hasTag("primitive") || parseResult.hasTag("json-primitive")) {
            type = Type.PRIMITIVE;
        } else if (parseResult.hasTag("null") || parseResult.hasTag("json-null")) {
            type = Type.NULL;
        } else {
            for (String tag : parseResult.tags) {
                try {
                    type = Type.fromString(tag);
                    return LiteralUtils.canInitSafely(json);
                } catch (IllegalArgumentException ignored) {
                }
            }
            Skript.error("Unknown json type in condition.");
            return false;
        }
        return LiteralUtils.canInitSafely(json);
    }

    enum Type {
        OBJECT,
        ARRAY,
        PRIMITIVE,
        NULL;

        public static Type fromString(String string) {
            return switch (string) {
                case "object", "json-object" -> OBJECT;
                case "array", "json-array" -> ARRAY;
                case "primitive", "json-primitive" -> PRIMITIVE;
                case "null", "json-null" -> NULL;
                default -> throw new IllegalArgumentException("Unknown type: " + string);
            };
        }
    }
}
