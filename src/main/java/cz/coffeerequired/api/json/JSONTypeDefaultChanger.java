package cz.coffeerequired.api.json;

import ch.njol.skript.classes.Changer;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import cz.coffeerequired.support.SkriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static cz.coffeerequired.SkJson.logger;

@Slf4j
public class JSONTypeDefaultChanger implements Changer<JsonElement> {
    @Override
    @SuppressWarnings("all")
    public @Nullable Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {

        logger().debug("Main changer: " + mode);

        return switch (mode) {
//            case RESET, REMOVE, REMOVE_ALL -> CollectionUtils.array(Object[].class);
            case REMOVE -> CollectionUtils.array(JsonPath.class);
            default -> null;
        };
    }

    @Override
    public void change(JsonElement[] what, @Nullable Object[] delta, ChangeMode mode) {
        if (mode == Changer.ChangeMode.REMOVE) {

            logger().debug("@[WHAT]1: " + Arrays.toString(what));
            logger().debug("@[DELTA]2: " + Arrays.toString(delta));

            logger().debug("Removing " + getClass().getSimpleName());

            if (delta == null || delta.length < 1) {
                logger().exception("delta need to be defined", new Exception("delta is null"));
                return;
            }

            logger().debug("delta: " + Arrays.toString(delta));
            var jsonPath = (JsonPath) delta[0];
            logger().debug("json-path: " + jsonPath);


            if (SkriptUtils.isSingleton(delta)) {
                assert jsonPath != null;
                SerializedJson serializedJson = new SerializedJson(jsonPath.getInput());
                serializedJson.remover.byKey(jsonPath.getKeys());
            } else {
                for(Object op : delta) {
                    if (op instanceof JsonPath p) {
                        SerializedJson serializedJson = new SerializedJson(p.getInput());
                        serializedJson.remover.byKey(p.getKeys());
                    }
                }
            }
        }
    }
}
