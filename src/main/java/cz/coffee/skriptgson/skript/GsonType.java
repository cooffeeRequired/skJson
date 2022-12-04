/**
 * Messy, will clean and fixed some issues in next Version
 */

package cz.coffee.skriptgson.skript;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.util.GsonUtils;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;

import static cz.coffee.skriptgson.util.Utils.newGson;

@SuppressWarnings({"unused",})
public class GsonType {

    private static final Parser<JsonElement> parser = new Parser<>() {
        @Override
        public boolean canParse(final @NotNull ParseContext context) {
            return false;
        }

        @Override
        public @NotNull String toString(JsonElement json, int flags) {
            return json.toString();
        }

        @Override
        public @NotNull String toVariableNameString(JsonElement json) {
            return json.toString();
        }
    };

    private static final Serializer<JsonElement> serializer = new Serializer<>() {

        @Override
        public @NotNull Fields serialize(JsonElement json) {
            Fields fields = new Fields();
            fields.putObject("json", json.toString());
            return fields;
        }

        @Override
        public void deserialize(JsonElement json, @NotNull Fields f) {
            assert false;
        }

        public JsonElement deserialize(Fields f) throws StreamCorruptedException {

            JsonElement fromField = null;
            Object bField = f.getObject("json");

            if (bField != null) {
                fromField = JsonParser.parseString(bField.toString());
            }
            if (fromField != null) {
                if (!fromField.isJsonNull()) {
                    if (fromField.isJsonObject()) {
                        return fromField.getAsJsonObject();
                    } else if (fromField.isJsonArray()) {
                        return fromField.getAsJsonArray();
                    } else if (fromField.isJsonPrimitive()) {
                        return fromField.getAsJsonPrimitive();
                    }
                }
            }

            f.removeField("json");
            return new JsonObject();
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

    private static final Changer<JsonElement> changer = new Changer<>() {
        @Override
        public Class<?> @NotNull [] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case ADD, SET, REMOVE -> CollectionUtils.array(JsonElement.class);
                default -> CollectionUtils.array(Object.class);
            };

        }

        @Override
        public void change(JsonElement @NotNull [] what, Object @NotNull [] delta, ChangeMode mode) {
            switch (mode) {
                case ADD -> {
                    for (JsonElement varElement : what) {
                        for (Object addElement : delta) {
                            if (varElement.isJsonObject()) {
                                String size = String.valueOf(varElement.getAsJsonObject().entrySet().size());
                                varElement.getAsJsonObject().add(size, newGson().toJsonTree(addElement));
                            } else if (varElement.isJsonArray()) {
                                String size = String.valueOf(varElement.getAsJsonArray().size());
                                varElement.getAsJsonArray().add(newGson().toJsonTree(addElement));
                            } else {
                                return;
                            }
                        }
                    }
                }
                case REMOVE -> {
                    for (JsonElement varElement : what) {
                        for (Object removeElement : delta) {
                            if (GsonUtils.check(varElement, removeElement.toString(), GsonUtils.Type.KEY)) {
                                if (varElement.isJsonObject()) {
                                    varElement.getAsJsonObject().remove(removeElement.toString());
                                } else if (varElement.isJsonArray()) {
                                    varElement.getAsJsonArray().remove(Integer.parseInt(removeElement.toString()));
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    static {
        Classes.registerClass(new ClassInfo<>(JsonElement.class, "jsonelement")
                .user("json[[ ]element]")
                .name("Json Element")
                .description("Representing a JSON element")
                .examples("on script load:",
                        "# Add",
                        "   set {_e} to new json from text \"{'hello' : 'hi'}\"",
                        "   add (new json from text \"{'bye': 'bb'}\") to {_e}",
                        "# Remove",
                        "   set {_e} to new json from text \"{'hello' : 'hi'}\"",
                        "   remove \"hello\" from {_e}"
                )
                .since("1.0")
                .parser(parser)
                .changer(changer)
                .serializer(serializer)
        );
    }
}