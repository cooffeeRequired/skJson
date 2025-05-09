package cz.coffeerequired.api.json;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;

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
            return null;
        }

        @Override
        public void change(JsonElement[] what, @Nullable Object[] delta, ChangeMode mode) {
           
        }
    };
}
