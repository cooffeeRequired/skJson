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
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.SerializedJson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;



@Name("Sort JSON in descending/ascending order by key/value")
@Description({
    "Sorts the JSON element in descending/ascending order by key/value.",
    "The JSON element can be a JSON object or an array.",
    "The sort type can be specified as a string in the format of 'by key ascending' or 'by value descending'.",
    "The sort type can also be specified as a SortType enum value."
})
@Examples({
    "{_json} in descending order by key",
    "{_json} in ascending order by key",
    "{_json} in descending order by value",
    "{_json} in ascending order by value"
})
@Since("5.1.2")
public class ExprSortJson extends SimpleExpression<JsonElement> {

    private SortType sortType;
    private Expression<JsonElement> jsonElementExpression;

    @Override
    protected JsonElement @Nullable [] get(Event event) {
        JsonElement jsonElement = jsonElementExpression.getSingle(event);
        if (jsonElement == null) {
            SkJson.severe("Cannot sort null json element");
            return new JsonElement[0];
        }
        SerializedJson json = new SerializedJson(jsonElement);
        return new JsonElement[]{json.sort(sortType)};

    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "sorted json %s by %s".formatted(jsonElementExpression.toString(event, debug), sortType.name);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        var direction = parseResult.hasTag("descending") ? "descending" : parseResult.hasTag("ascending") ? "ascending" : null;
        var type = parseResult.hasTag("key") ? "key" : parseResult.hasTag("value") ? "value" : null;
        this.sortType = SortType.fromName("by %s %s".formatted(type, direction));
        jsonElementExpression = LiteralUtils.defendExpression(expressions[0]);
        SkJson.debug("Sort type: %s".formatted(sortType));
        return LiteralUtils.canInitSafely(jsonElementExpression) && sortType != null;
    }

    public enum SortType {
        BY_KEY_ASC("by key ascending"),
        BY_KEY_DESC("by key descending"),
        BY_VALUE_ASC("by value ascending"),
        BY_VALUE_DESC("by value descending");

        public final String name;
        SortType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static SortType fromName(String name) {
            for (SortType sortType : values()) {
                if (sortType.name.equalsIgnoreCase(name)) return sortType;
            }
            return null;
        }
    }
}
