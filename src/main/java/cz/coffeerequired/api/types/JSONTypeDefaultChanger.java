package cz.coffeerequired.api.types;

import ch.njol.skript.classes.Changer;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.support.SkriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class JSONTypeDefaultChanger implements Changer<JsonElement> {
    @Override
    @SuppressWarnings("all")
    public @Nullable Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {
        return switch (mode) {
//            case RESET, REMOVE, REMOVE_ALL -> CollectionUtils.array(Object[].class);
            case REMOVE -> CollectionUtils.array(JsonPath.class);
            case ADD -> CollectionUtils.array(Object.class, Object[].class);
            default -> null;
        };
    }

    @Override
    public void change(JsonElement[] what, @Nullable Object[] delta, ChangeMode mode) {
        if (mode == Changer.ChangeMode.REMOVE) {
            if (delta == null || delta.length < 1) {
                SkJson.exception(new Exception("delta is null"), "delta need to be defined");
                return;
            }
            var jsonPath = (JsonPath) delta[0];

            if (SkriptUtils.isSingleton(delta)) {
                assert jsonPath != null;
                SerializedJson serializedJson = new SerializedJson(jsonPath.getInput());
                serializedJson.remover.byKey(jsonPath.getKeys());
            } else {
                for (Object op : delta) {
                    if (op instanceof JsonPath p) {
                        SerializedJson serializedJson = new SerializedJson(p.getInput());
                        serializedJson.remover.byKey(p.getKeys());
                    }
                }
            }
        } else if (mode == Changer.ChangeMode.ADD) {
            JsonElement jsonElement = what[0];
            if (jsonElement == null) {
                return;
            }
            if (jsonElement instanceof JsonArray array) {
                for (Object o : delta) {
                    array.add(GsonParser.toJson(o));
                }
            }


        }
    }
}
