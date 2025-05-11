package cz.coffeerequired.api.json;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Map;

public abstract class Json {
    public static Parser<JsonElement> parser = new Parser<>() {
        @Override
        public @NotNull String toString(JsonElement o, int flags) {
            return o.toString();
        }

        @Override
        public @NotNull String toVariableNameString(JsonElement o) {
            return toString(o, 0);
        }

        @Override
        public boolean canParse(@NotNull ParseContext context) {
            return false;
        }
    };

    public static Serializer<JsonElement> serializer = new Serializer<>() {
        @Override
        public Fields serialize(JsonElement o) {
            Fields fields = new Fields();
            fields.putObject("json", o.toString());
            return fields;
        }

        @Override
        public void deserialize(JsonElement o, @NotNull Fields f) {
            assert false;
        }

        @Override
        public JsonElement deserialize(@NotNull Fields fields) throws StreamCorruptedException {
            Object field = fields.getObject("json");
            if (field == null) return JsonNull.INSTANCE;
            fields.removeField("json");
            return cz.coffeerequired.api.json.Parser.toJson(field);
        }

        @Override
        public boolean mustSyncDeserialization() {
            return false;
        }

        @Override
        protected boolean canBeInstantiated() {
            return false;
        }
    };

    public static Changer<JsonElement> changer = new Changer<>() {
        @Override
        @SuppressWarnings("all")
        public @Nullable Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {
            return switch(mode) {
                case RESET, REMOVE, REMOVE_ALL, ADD -> CollectionUtils.array(Object.class);
                default -> null;
            };
        }

        @Override
        public void change(JsonElement[] what, @Nullable Object[] delta, ChangeMode mode) {
            JsonElement jsonElement = what[0];
            JsonAccessor serializedJson = new JsonAccessor(jsonElement);

            ArrayList<Map.Entry<String, PathParser.Type>> emptyTokens = new ArrayList<>();
            switch(mode) {
                case RESET -> {
                    serializedJson.remover.reset(emptyTokens);
                    break;
                }
                case REMOVE  -> {
                    for (var o : delta) {
                        serializedJson.remover.byValue(emptyTokens, o);
                    }
                    break;
                }
                case REMOVE_ALL -> {
                    for (var o : delta) {
                        serializedJson.remover.allByValue(emptyTokens, o);
                    }
                    break;
                }
                case ADD -> {
                    for (Object o : delta) {
                        JsonElement parsed = cz.coffeerequired.api.json.Parser.toJson(o);
                        serializedJson.changer.add(emptyTokens, parsed);
                    }
                    break;
                }
                default -> {}
            }
        }
    };
}
