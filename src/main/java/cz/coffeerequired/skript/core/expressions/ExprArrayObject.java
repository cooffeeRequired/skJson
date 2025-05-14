package cz.coffeerequired.skript.core.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.json.JsonAccessor;
import cz.coffeerequired.api.json.Parser;
import cz.coffeerequired.api.json.PathParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;


@Name("Array/Object")
@Description({
        "Changer.",
        "You can SET/ADD/REMOVE/RESET/DELETE/REMOVE_ALL",
})
@Examples("""
            set json value "x.y" of {_json} to 1
            set json values "x.y" of {_json} to 2, 3 and 4 #-> #?throws error when single

            add 10 to json array "x.y.z[]" of {_json}
            add 20 and 30 and "lol" to json array "x.y.z" of {_json}

            remove 10 from json array "x.y.z" of {_json}
            remove "lol" from json array "x.y.z" of {_json}

            delete json value "x" of {_json}
            delete json values "x.y" of {_json}

            set {_value} to json value "x.y" of {_json}
            set {_values::*} to json values "x.y" of {_json}

            remove all 1 from json values "x.y" of {_json}
            remove all 2 and 3 from json values "x.y" of {_json}
            \s
            reset json array "x.y.z" of {_json}
            reset json object "x.y" of {_json}
        """
)
@Since("5.1.2")
public class ExprArrayObject extends SimpleExpression<Object> {

    private JSON_TYPE type;
    private Expression<JsonElement> jsonVariable;
    private Expression<String> pathVariable;

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        type = parseResult.hasTag("array") ? JSON_TYPE.ARRAY : JSON_TYPE.OBJECT;
        jsonVariable = LiteralUtils.defendExpression(exprs[1]);
        pathVariable = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(jsonVariable, pathVariable);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "json %s %s of %s".formatted(type.name().toLowerCase(), pathVariable.toString(event, debug), jsonVariable.toString(event, debug));
    }

    @Override
    protected Object[] get(Event event) {
        return new Object[0];
    }

    @Override
    public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE -> CollectionUtils.array(Object[].class);
            case RESET -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
        var tokens = PathParser.tokenize(pathVariable.getSingle(event), Api.Records.PROJECT_DELIM);
        var json = jsonVariable.getSingle(event);
        if (json == null) {
            SkJson.severe(getParser().getNode(), "Trying to change JSON %s what is null", jsonVariable.toString(event, false));
            return;
        }
        var serializedJson = new JsonAccessor(json);

        delta = delta == null ? new Object[0] : delta;

        switch (mode) {
            case ADD:
                for (Object o : delta) {
                    JsonElement parsed = Parser.toJson(o);
                    serializedJson.changer.add(tokens, parsed);
                }
                break;
            case REMOVE:
                for (var o : delta) {
                    serializedJson.remover.byValue(tokens, o);
                }
                break;
            case RESET:
                serializedJson.remover.reset(tokens);
                break;
            default:
                break;
        }
    }

    enum JSON_TYPE {
        ARRAY, OBJECT
    }
}
