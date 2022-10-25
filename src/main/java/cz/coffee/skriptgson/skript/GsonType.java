package cz.coffee.skriptgson.skript;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@SuppressWarnings({"unused", "NullableProblems"})
public class GsonType {

    private static final Parser<JsonElement> parser = new Parser<>() {
        @SuppressWarnings("NullableProblems")
        @Override
        public boolean canParse(final ParseContext context) {
            return false;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString(JsonElement json, int flags) {
            return json.toString();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public String toVariableNameString(JsonElement json) {
            return json.toString();
        }
    };

    private static final Changer<JsonElement> changer = new Changer<>() {
        @SuppressWarnings("NullableProblems")
        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            //noinspection EnhancedSwitchMigration
            switch (mode) {
                case ADD:
                case REMOVE:
                    return CollectionUtils.array(JsonElement.class);
            }
            return null;
        }

        @SuppressWarnings("NullableProblems")
        @Override

        public void change(JsonElement[] what, Object[] delta, ChangeMode mode) {
            switch (mode) {
                case ADD:
                    JsonElement[] value = new JsonElement[]{JsonParser.parseString(String.valueOf(delta[0]))};
                    for (JsonElement object : what) {
                        for (JsonElement jsonElement : value) {
                            if (object.isJsonArray()) {
                                object.getAsJsonArray().add(jsonElement);
                            } else {
                                String i = object.getAsJsonObject().entrySet().isEmpty() ? String.valueOf(0) : String.valueOf(object.getAsJsonObject().entrySet().toArray().length);
                                object.getAsJsonObject().add(i, jsonElement);
                            }
                        }
                    }
                case REMOVE: {
                    Integer[] parsedDelta = new Integer[]{Integer.parseInt(String.valueOf(delta[0]))};
                    for (JsonElement object : what) {
                        for (Integer n : parsedDelta) {
                            if (object.isJsonObject()) {
                                object.getAsJsonObject().remove(String.valueOf(n));
                            } else {
                                try {
                                    object.getAsJsonArray().remove(n);
                                } catch (IndexOutOfBoundsException e) {
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
                .since("1.0")
                .parser(parser)
                .changer(changer)
        );
    }
}