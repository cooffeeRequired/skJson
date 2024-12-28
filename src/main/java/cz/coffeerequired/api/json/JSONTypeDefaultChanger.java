package cz.coffeerequired.api.json;

import ch.njol.skript.classes.Changer;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSONTypeDefaultChanger implements Changer<JsonElement> {
    @Override
    @SuppressWarnings("all")
    public @Nullable Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {
        return switch (mode) {
//            case RESET, REMOVE, REMOVE_ALL -> CollectionUtils.array(Object[].class);
            default -> null;
        };
    }

    @Override
    public void change(JsonElement[] what, @Nullable Object[] delta, ChangeMode mode) {

    }
}
