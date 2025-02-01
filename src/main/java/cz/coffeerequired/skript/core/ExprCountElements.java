package cz.coffeerequired.skript.core;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.json.SerializedJson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Count keys/values in the json")
@Description("You can get counts of keys/values inside json object/value")
@Since("4.1 - API UPDATE")
@Examples("""
            set {_json} to json from "{random: 1, key: {random: 2}}"
            send count of value 2 in {_json}
        """)
public class ExprCountElements extends SimpleExpression<Integer> {

    private Expression<JsonElement> jsonVariable;
    private Expression<?> inputValues;
    private boolean isKeys;


    @Override
    protected @Nullable Integer[] get(Event event) {
        Integer[] found = {0};

        final JsonElement json = jsonVariable.getSingle(event);
        SerializedJson serialized = new SerializedJson(json);
        if (json == null) return new Integer[0];

        Collections.singleton(inputValues.getSingle(event))
                .forEach((value) -> {
                    if (isKeys && value instanceof String str) {
                        found[0] = serialized.counter.keys(str);
                    } else {
                        found[0] = serialized.counter.values(value);
                    }
                });

        return found;
    }

    @Override
    public boolean isSingle() {
        return inputValues.isSingle();
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return Classes.getDebugMessage(inputValues) + " in " + Classes.getDebugMessage(jsonVariable);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        isKeys = parseResult.mark == 0;
        jsonVariable = defendExpression(expressions[1]);
        inputValues = defendExpression(expressions[0]);

        return canInitSafely(jsonVariable) && canInitSafely(inputValues);
    }
}
