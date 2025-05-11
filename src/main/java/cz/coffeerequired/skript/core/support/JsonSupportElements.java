package cz.coffeerequired.skript.core.support;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.json.JsonAccessorUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Support literals")
@Description("Returns the first, second, third, last, random or custom value of the json. This is used to get the first, second, third, last, random or custom value of the json.")
@Since("4.1 - API UPDATE")
@Examples("""
            set {_json} to json from "[1, 2, 3, 8, 'TEST']"
            
            send 1st value of {_json}
            send 2nd value of {_json}
            send last value of {_json}
            send random value of {_json}
            send 4. value of {_json}
            """)
public class JsonSupportElements extends SimpleExpression<Object> {

    private SearchType searchType;
    private Type type;
    private Expression<JsonElement> jsonVariable;
    private Expression<Integer> userCustomIndexInput;


    @Override
    protected @Nullable Object[] get(Event event) {
        final JsonElement json = jsonVariable.getSingle(event);
        if (json == null) return new Object[0];
        return new Object[]{switch (type) {
            case FIRST -> JsonAccessorUtils.getFirst(json, searchType);
            case LAST -> JsonAccessorUtils.getLast(json, searchType);
            case SECOND -> JsonAccessorUtils.get(json, 1, searchType);
            case THIRD -> JsonAccessorUtils.get(json, 2, searchType);
            case RANDOM -> JsonAccessorUtils.getRandom(json, searchType);
            case CUSTOM -> {
                Integer index = userCustomIndexInput.getSingle(event);
                if (index == null) yield null;
                yield JsonAccessorUtils.get(json, index - 1, searchType);
            }
        }};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (type) {
            case FIRST -> "1st";
            case SECOND -> "2nd";
            case THIRD -> "3rd";
            case LAST -> "last";
            case RANDOM -> "random";
            case CUSTOM -> userCustomIndexInput.toString(event, debug) + ".";
        } + (searchType.equals(SearchType.KEY) ? " key" : " value") + " of " + jsonVariable.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        searchType = parseResult.hasTag("key") ? SearchType.KEY : SearchType.VALUE;
        type = switch (matchedPattern) {
            case 0 -> Type.FIRST;
            case 1 -> Type.SECOND;
            case 2 -> Type.THIRD;
            case 3 -> Type.LAST;
            case 4 -> Type.RANDOM;
            case 5 -> Type.CUSTOM;
            default -> null;
        };
        jsonVariable = defendExpression(expressions[0]);
        if (type.equals(Type.CUSTOM)) {
            userCustomIndexInput = (Expression<Integer>) expressions[0];
            jsonVariable = defendExpression(expressions[1]);
            if (userCustomIndexInput == null) return false;
        }
        return canInitSafely(jsonVariable);
    }

    public enum SearchType {VALUE, KEY}

    public enum Type {FIRST, SECOND, THIRD, LAST, RANDOM, CUSTOM}
}
