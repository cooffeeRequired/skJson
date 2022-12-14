package cz.coffee.skriptgson.skript.types;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.*;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;

import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;


@Since("2.0.0")


@SuppressWarnings("unused")
public class JsonElementType {
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
            System.out.println("Parse?");
            Fields fields = new Fields();
            fields.putObject("json-element", json.toString());
            return fields;
        }

        @Override
        public void deserialize(JsonElement o, @NotNull Fields f) {
            System.out.println("parse ? - G");
            assert false;
        }

        @Override
        @SuppressWarnings("deprecation")
        public JsonElement deserialize(Fields fields) throws StreamCorruptedException {
            System.out.println("Parse? - N");
            JsonElement fromField = null;
            Object fieldContent = fields.getObject("json-element");
            if (fieldContent != null) fromField = JsonParser.parseString(fieldContent.toString());
            if (fieldContent == null) return new JsonNull();

            if (fromField instanceof JsonObject object)
                return object;
            else if (fromField instanceof JsonArray array)
                return array;
            else if (fromField instanceof JsonPrimitive primitive)
                return primitive;

            fields.removeField("json-element");
            return new JsonNull();
        }

        @Override
        public boolean mustSyncDeserialization() {
            return true;
        }

        @Override
        protected boolean canBeInstantiated() {
            return false;
        }
    };

    private static final Changer<JsonElement> changer = new Changer<>() {
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

        @Override
        public Class<?> @NotNull [] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case ADD, SET, REMOVE -> CollectionUtils.array(JsonElement.class);
                default -> CollectionUtils.array(Object.class);
            };
        }

        @Override
        public void change(JsonElement @NotNull [] what, @Nullable Object @NotNull [] delta, @NotNull ChangeMode mode) {
            switch (mode) {
                case ADD -> {
                    for (JsonElement varElement : what) {
                        for (Object addElement : delta) {
                            if (varElement.isJsonObject()) {
                                String size = String.valueOf(varElement.getAsJsonObject().entrySet().size());
                                varElement.getAsJsonObject().add(size, hierarchyAdapter().toJsonTree(addElement));
                            } else if (varElement.isJsonArray()) {
                                varElement.getAsJsonArray().add(hierarchyAdapter().toJsonTree(addElement));
                            } else {
                                return;
                            }
                        }
                    }
                }
                case REMOVE -> {

                    for (JsonElement varElement : what) {
                        for (Object removeElement : delta) {
                            assert removeElement != null;
                            String remove = removeElement.toString().replaceAll("\"", "");

                            if (varElement instanceof JsonObject element) {
                                if (element.has(remove)) {
                                    element.remove(remove);
                                }
                            } else if (varElement instanceof JsonArray elementA) {
                                elementA.remove(Integer.parseInt(remove));
                            }
                        }
                    }
                }
            }
        }
    };
}
