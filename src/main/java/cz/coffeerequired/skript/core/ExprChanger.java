package cz.coffeerequired.skript.core;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.JsonPath;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.support.SkriptUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ExprChanger extends SimpleExpression<Object> {

    private Expression<JsonPath> exprJsonPath;
    private ChangerType changeType;
    private enum ChangerType {
        KEY, VALUE
    }

    @Override
    protected @Nullable Object[] get(Event event) {
        return new Object[0];
    }

    @Override
    public boolean isSingle() { return false; }

    @Override
    public Class<?> getReturnType() { return Object.class; }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return Classes.getDebugMessage(event) + " " + getClass().getSimpleName();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET -> CollectionUtils.array(Object.class, Object[].class);
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        JsonPath jsonPath = null;
        if (mode.equals(Changer.ChangeMode.SET)) {
            jsonPath = exprJsonPath.getSingle(event);

            if (jsonPath == null) {
                SkJson.logger().exception("path cannot be null", new Exception("Path is null"));
                return;
            }
        }

        if (mode == Changer.ChangeMode.SET) {
            if (delta == null || delta.length < 1) {
                SkJson.logger().exception("delta need to be defined", new Exception("delta is null"));
                return;
            }

            SkJson.logger().info("Changing " + getClass().getSimpleName());
            SkJson.logger().info("delta: " + Arrays.toString(delta));
            SkJson.logger().info("json-path: " + exprJsonPath.getSingle(event));
            SkJson.logger().info("changeType: " + changeType);

            if(changeType.equals(ChangerType.KEY)) {
                if (!SkriptUtils.isSingleton(delta)) {
                    SkJson.logger().exception("incorrect format of delta", new Exception("delta cannot be an list, when changing JSON key!"));
                    return;
                }

                if (!(delta[0] instanceof String)) {
                    SkJson.logger().exception("incorrect format of delta", new Exception("delta need to be list, cause JSON expecting key as string"));
                    return;
                }

                SerializedJson serializedJson = new SerializedJson(jsonPath.getInput());
                serializedJson.changer.key(jsonPath.getKeys(), (String) delta[0]);

            } else if (changeType.equals(ChangerType.VALUE)) {
                JsonElement parsed = GsonParser.toJson(delta[0]);
                SerializedJson serializedJson = new SerializedJson(jsonPath.getInput());

                serializedJson.changer.value(jsonPath.getKeys(), parsed);
            }

        }
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        changeType = ChangerType.values()[parseResult.mark];
        exprJsonPath = LiteralUtils.defendExpression(expressions[0]);
        return LiteralUtils.canInitSafely(exprJsonPath) && exprJsonPath.isSingle();
    }
}
