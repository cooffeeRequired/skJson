package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static cz.coffee.skriptgson.util.GsonUtils.fromPrimitive;
import static cz.coffee.skriptgson.util.Utils.newGson;

public class ExprGetJsonElement extends SimpleExpression<Object> {

    private Expression<String> getString;
    private Expression<JsonElement> rawJson;

    static {
        Skript.registerExpression(ExprGetJsonElement.class, Object.class, ExpressionType.COMBINED,
                "element %string% from [json] %jsonelement%"
        );
    }

    @Override
    protected @Nullable Object @NotNull [] get(@NotNull Event e) {
        String getStringSingle = getString.getSingle(e);

        if(getStringSingle == null) return new JsonElement[0];

        String[] str = getStringSingle.split(";");
        JsonElement json = rawJson.getSingle(e);

        if(json == null || str[0] == null) return new JsonElement[0];
        if(json instanceof JsonObject object) {
            for(String key : object.keySet()) {
                for (String mKey : str) {
                    if (mKey.equals(key)) {

                        if(!object.has(mKey)) return new JsonElement[0];

                        JsonElement j = object.get(mKey);
                        Map<String, Object> hash = new HashMap<>();
                        for(Map.Entry<String, JsonElement> map : j.getAsJsonObject().entrySet()) {
                            if(map.getValue() instanceof JsonObject || map.getValue() instanceof JsonArray) {
                                hash.put(map.getKey(), map.getValue());
                            } else if(map.getValue() instanceof JsonPrimitive primitive) {
                                hash.put(map.getKey(), fromPrimitive(primitive));
                            }
                        }
                        ConfigurationSerializable co = ConfigurationSerialization.deserializeObject(hash);

                        if(co == null) {
                            return new Object[]{newGson().toJson(hash)};
                        } else {
                            return new Object[]{ConfigurationSerialization.deserializeObject(hash)};
                        }
                    }
                }
            }
        }

        return new JsonElement[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        getString = (Expression<String>) exprs[0];
        rawJson = (Expression<JsonElement>) exprs[1];
        return true;
    }
}
