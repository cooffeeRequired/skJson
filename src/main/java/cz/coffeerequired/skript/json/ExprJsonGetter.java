package cz.coffeerequired.skript.json;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.api.json.SerializedJsonUtils;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

public class ExprJsonGetter extends SimpleExpression<Object> {

    private ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> tokens;

    private Expression<JsonElement> jsonElementExpression;

    @Override
    protected @Nullable Object[] get(Event event) {
        JsonElement jsonElement = jsonElementExpression.getSingle(event);
        if (jsonElement == null) return new Object[0];

        SerializedJson serializedJson = new SerializedJson(jsonElement);
        Object searcherResult = serializedJson.searcher.keyOrIndex(tokens);
        if (searcherResult == null) return new Object[0];

        if (tokens.getLast().getValue().equals(SkriptJsonInputParser.Type.ListAll)) {
            return SerializedJsonUtils.getAsParsedArray(searcherResult);
        } else {
            return new Object[]{ searcherResult };
        }
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
        return Classes.getDebugMessage(jsonElementExpression) + " " + tokens.toString();
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        var r =  parseResult.regexes.getFirst();

        jsonElementExpression = defendExpression(expressions[0]);
        tokens = SkriptJsonInputParser.tokenizeFromPattern(r.group());
        //return !tokens.isEmpty() && canInitSafely(jsonElementExpression);

        SkJson.logger().info("group: " + r.group() + " tokens: " + tokens);

        return true;
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(Object.class, Object[].class);
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (mode.equals(Changer.ChangeMode.SET)) {
          if (delta == null) return;

          JsonElement jsonElement = jsonElementExpression.getSingle(event);
          for (Object o : delta) {
              JsonElement parsed = GsonParser.toJson(o);
              SerializedJson serializedJson = new SerializedJson(jsonElement);
              serializedJson.changer.value(tokens, parsed);
          }
        }
    }
}
