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

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.ERROR;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.ONLY_JSONVAR_IS_ALLOWED;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;
import static cz.coffee.skriptgson.utils.Utils.isNumeric;


@Since("2.0.0")

public class JsonElementType {



    private static final String KEY_PARSED_TAG = ";";
    private static final String KEY_NESTED_TAG = ":";

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
            fields.putObject("json-element", json.toString());
            return fields;
        }

        @Override
        public void deserialize(JsonElement o, @NotNull Fields f) {
            assert false;
        }

        @Override
        @SuppressWarnings("deprecation")
        public JsonElement deserialize(Fields fields) throws StreamCorruptedException {
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

    private static final Changer<Object> changer = new Changer<>() {
        @Override
        public Class<?> @NotNull [] acceptChange(@NotNull ChangeMode mode) {
            switch (mode) {
                case ADD, SET, REMOVE ->
                        CollectionUtils.array(Object.class, JsonElement.class);
            }
            return CollectionUtils.array(Object.class);
        }

        @Override
        public void change(Object @NotNull [] what, @Nullable Object @NotNull [] delta, @NotNull ChangeMode mode) {

            JsonElement deltaJson;
            String Key = null;
            String[] nestedKeys = new String[0];
            boolean isNested = false;


            switch (mode) {
                case ADD -> {
                    for (Object parsedDelta : delta) {
                        /*
                        setup of JsonElement witch contains a key and the value.
                         */
                        if (parsedDelta == null) return;
                        if (parsedDelta.toString().contains(KEY_PARSED_TAG)) {
                            /*
                            being is object. cause delta contains a KEY_PARSED_TAG
                             */
                            String[] splitParsedDelta = ((String) parsedDelta).split(KEY_PARSED_TAG);
                            deltaJson = JsonParser.parseString(splitParsedDelta[1]);
                            if (splitParsedDelta[0].contains(KEY_NESTED_TAG)) {
                                isNested = true;
                                nestedKeys = splitParsedDelta[0].split(KEY_NESTED_TAG);
                                Key = splitParsedDelta[splitParsedDelta.length - 1];
                            } else {
                                Key = splitParsedDelta[0];
                            }

                            for (Object parsedWhat : what) {
                                if (!(parsedWhat instanceof JsonElement)) {
                                    sendErrorMessage(ONLY_JSONVAR_IS_ALLOWED, ERROR);
                                    return;
                                } else {
                                    JsonElement value;
                                    int index = 0;
                                    if (isNested) {
                                        for (String nestedKey : nestedKeys) {
                                            boolean isLast = nestedKey.equals(nestedKeys[nestedKeys.length - 1]);

                                            if (parsedWhat == null) return;

                                            if (((JsonElement) parsedWhat).isJsonObject()) {
                                                value = ((JsonElement) parsedWhat).getAsJsonObject().get(nestedKey);
                                                if (!isLast) {
                                                    parsedWhat = value;
                                                } else {
                                                    if (value != null) {
                                                        if (value.isJsonArray()) {
                                                            ((JsonElement) parsedWhat).getAsJsonObject().get(nestedKey).getAsJsonArray().add(deltaJson);
                                                        } else {
                                                            ((JsonElement) parsedWhat).getAsJsonObject().add(nestedKey, deltaJson);
                                                        }
                                                    } else {
                                                        ((JsonElement) parsedWhat).getAsJsonObject().add(nestedKey, deltaJson);
                                                    }
                                                }
                                            } else if (((JsonElement) parsedWhat).isJsonArray()) {
                                                if (isNumeric(nestedKey)) {
                                                    index = Integer.parseInt(nestedKey);
                                                }
                                                if (((JsonElement) parsedWhat).getAsJsonArray().size() < index) return;
                                                try {
                                                    value = ((JsonElement) parsedWhat).getAsJsonArray().get(index);
                                                } catch (IndexOutOfBoundsException indexOut) {
                                                    return;
                                                }

                                                if (!isLast) {
                                                    parsedWhat = value;
                                                } else {
                                                    ((JsonElement) parsedWhat).getAsJsonArray().add(deltaJson);
                                                }
                                            } else {
                                                return;
                                            }
                                        }
                                    } else {
                                        if (((JsonElement) parsedWhat).isJsonArray()) {
                                            ((JsonElement) parsedWhat).getAsJsonArray().add(deltaJson);
                                        } else if (((JsonElement) parsedWhat).isJsonObject()) {
                                            ((JsonElement) parsedWhat).getAsJsonObject().add(Key, deltaJson);
                                        } else {
                                            return;
                                        }
                                    }
                                }
                            }
                        } else {
                            for (Object parsedWhat : what) {
                                deltaJson = JsonParser.parseString(parsedDelta.toString());
                                ((JsonElement) parsedWhat).getAsJsonArray().add(deltaJson);
                            }
                        }
                    }
                }
                case REMOVE -> {
                    for (Object parsedDelta : delta) {
                        if (parsedDelta == null) return;
                        if (parsedDelta.toString().contains(KEY_NESTED_TAG)) {
                            isNested = true;
                            String[] splitParsedDelta = ((String) parsedDelta).split(KEY_PARSED_TAG);
                            nestedKeys = splitParsedDelta[0].split(KEY_NESTED_TAG);
                        } else {
                            Key = parsedDelta.toString();
                        }
                        for (Object parsedWhat : what) {
                            if (!(parsedWhat instanceof JsonElement)) {
                                sendErrorMessage(ONLY_JSONVAR_IS_ALLOWED, ERROR);
                                return;
                            } else {
                                if (!isNested) {
                                    if (((JsonElement) parsedWhat).isJsonObject()) {
                                        ((JsonElement) parsedWhat).getAsJsonObject().remove(Key);
                                    } else if (((JsonElement) parsedWhat).isJsonArray()) {
                                        if (isNumeric(Key)) {
                                            ((JsonElement) parsedWhat).getAsJsonArray().remove(Integer.parseInt(Key));
                                        }
                                    } else {
                                        return;
                                    }
                                } else {
                                    int index = 0;
                                    JsonElement value;
                                    for (String nestedKey : nestedKeys) {
                                        boolean isLast = nestedKey.equals(nestedKeys[nestedKeys.length - 1]);
                                        if (((JsonElement) parsedWhat).isJsonObject()) {
                                            value = ((JsonElement) parsedWhat).getAsJsonObject().get(nestedKey);
                                            if (isLast) {
                                                ((JsonElement) parsedWhat).getAsJsonObject().remove(nestedKey);
                                            } else {
                                                parsedWhat = value;
                                            }
                                        } else if (((JsonElement) parsedWhat).isJsonArray()) {
                                            if (isNumeric(nestedKey)) {
                                                index = Integer.parseInt(nestedKey);
                                            }
                                            value = ((JsonElement) parsedWhat).getAsJsonArray().get(index);

                                            if (isLast) {
                                                ((JsonElement) parsedWhat).getAsJsonArray().remove(index);
                                            } else {
                                                parsedWhat = value;
                                            }
                                        } else {
                                            return;
                                        }
                                    }
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
                .description("Representing a JSON element, You can add to them, remove from them.. Also you can remove from/add to nested object/arrays")
                .examples(
                        "on load:",
                        "\tset {_e} to new json from text \"{'some': {'a': {}}\"",
                        "",
                        "# Adding",
                        "\tadd \"some:bool;false\" to {_e}",
                        "",
                        "# Remove",
                        "\tremove \"some:bool\" from {_e}",
                        "",
                        "# Result",
                        "{\"some\": {\"a\": {}}"
                )
                .since("2.0.0")
                .parser(parser)
                .changer(changer)
                .serializer(serializer)
        );
    }

}
