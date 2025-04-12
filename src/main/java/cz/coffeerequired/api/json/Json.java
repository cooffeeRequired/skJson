package cz.coffeerequired.api.json;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.types.JsonPath;
import cz.coffeerequired.support.SkriptUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

public abstract class Json {
    public static Parser<JsonElement> parser = new Parser<JsonElement>() {
        @Override
        public @NotNull String toString(JsonElement o, int flags) {
            return o.toString();
        }

        @Override
        public @NotNull String toVariableNameString(JsonElement o) {
            return toString(o, 1);
        }

        @Override
        public boolean canParse(@NotNull ParseContext context) {
            return false;
        }
    };

    public static Serializer<JsonElement> serializer = new Serializer<>() {
        @Override
        public Fields serialize(JsonElement o) throws NotSerializableException {
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
            return GsonParser.toJson(field);
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
            return switch (mode) {
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
    };
}
