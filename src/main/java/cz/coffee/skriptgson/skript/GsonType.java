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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.JsonMap;

import java.io.StreamCorruptedException;
import java.util.List;

import static cz.coffee.skriptgson.util.Utils.*;

@SuppressWarnings({"unused", "NullableProblems", "UnnecessaryReturnStatement"})
public class GsonType {

    private static final Parser<JsonElement> parser = new Parser<>() {
        @Override
        public boolean canParse(final ParseContext context) {
            return false;
        }

        @Override
        public String toString(JsonElement json, int flags) {
            return json.toString();
        }

        @Override
        public String toVariableNameString(JsonElement json) {
            return json.toString();
        }
    };

    private static final Serializer<JsonElement> serializer = new Serializer<>() {

        @Override
        public Fields serialize(JsonElement json) {
            Fields fields = new Fields();
            fields.putObject("json", json.toString());
            return fields;
        }

        @Override
        public void deserialize(JsonElement json, Fields f) {
            assert false;
        }

        public JsonElement deserialize(Fields f) throws StreamCorruptedException{

            JsonElement fromField = null;
            Object bField = f.getObject("json");

            if ( bField != null ){
                fromField = JsonParser.parseString(bField.toString());
            }
            if ( fromField != null ) {
                if (!fromField.isJsonNull()) {
                    if (fromField.isJsonObject()) {
                        return fromField.getAsJsonObject();
                    } else if (fromField.isJsonArray()) {
                        return fromField.getAsJsonArray();
                    } else if ( fromField.isJsonPrimitive()) {
                        return fromField.getAsJsonPrimitive();
                    }
                }
            }

            f.removeField("json");
            return new JsonObject();
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
        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            //noinspection EnhancedSwitchMigration
            switch (mode) {
                case ADD:
                case SET:
                case REMOVE:
                    return CollectionUtils.array(JsonElement.class);
            }

            return null;
        }

        @Override
        public void change(JsonElement[] what, Object[] delta, ChangeMode mode) {
            switch (mode) {
                case ADD -> {
                    try {
                        String[] i;
                        boolean AddCase = false;
                        JsonElement[] value = new JsonElement[]{JsonParser.parseString(String.valueOf(delta[0]))};
                        for (JsonElement object : what) {
                            for (JsonElement jsonElement : value) {
                                if (object.isJsonArray()) {
                                    object.getAsJsonArray().add(jsonElement);
                                } else {
                                    if (jsonElement.isJsonPrimitive()) {
                                        i = new Gson().toJson(jsonElement.getAsJsonPrimitive())
                                                .split(";");

                                        AddCase = i[0].endsWith("+");
                                        if (AddCase) {
                                            jsonElement = JsonParser.parseString(SanitizeString(
                                                    "[" + i[1] + "]")
                                                    .replaceAll("^(.*?):(.*)$", "$1,$2")
                                            );
                                        } else {
                                            jsonElement = JsonParser.parseString(SanitizeString(i[1]));
                                        }
                                    } else {
                                        i = new String[]{object.getAsJsonObject().entrySet().isEmpty() ?
                                                String.valueOf(0) :
                                                String.valueOf(object.getAsJsonObject().entrySet().toArray().length - 1)};
                                    }
                                    if (AddCase) {
                                        String Key = SanitizeString(i[0].replaceAll("\\+", ""));

                                        if (object.getAsJsonObject().get(Key).isJsonArray()) {
                                            SkriptGson.info(color("&b?&r &cWe're sorry!&cYou can't data to defined array at this moment!"));
                                            return;
                                        }

                                        String Value1 = jsonElement.getAsJsonArray()
                                                .size() > 1 ? gsonText(jsonElement.getAsJsonArray().get(0)).replaceAll("\"", "") :
                                                String.valueOf(object.getAsJsonObject().get(Key).getAsJsonObject().size() - 1);

                                        JsonElement Value2 = JsonParser.parseString(gsonText(jsonElement.getAsJsonArray()
                                                .get(jsonElement
                                                        .getAsJsonArray().size() > 1 ? 1 : 0)));

                                        if (jsonElement.isJsonArray()) {
                                            object.getAsJsonObject()
                                                    .get(Key)
                                                    .getAsJsonObject()
                                                    .add(Value1, Value2);
                                        } else {
                                            object.getAsJsonObject()
                                                    .get(Key)
                                                    .getAsJsonObject()
                                                    .add(Value1, Value2);
                                        }
                                    } else {
                                        object.getAsJsonObject().add(SanitizeString(i[0]), jsonElement);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
                case REMOVE -> {
                    try {
                        String value = String.valueOf(delta[0]).replaceAll("\"", "");
                        for (JsonElement object : what) {
                            List<String> values = JsonMap.getValues(object.getAsJsonObject());
                            for (int i = 0; values.size() > i; i++) {
                                if (value.contains(";")) {
                                    String[] s = value.split(";");
                                    JsonElement jsonElements = object.getAsJsonObject();
                                    for (String st : s) {
                                        if (jsonElements == null || jsonElements.isJsonNull()) {
                                            return;
                                        }
                                        if (jsonElements.isJsonObject()) {
                                            jsonElements.getAsJsonObject().remove(s[s.length - 1]);
                                            jsonElements = jsonElements.getAsJsonObject().get(st);
                                        } else {
                                            jsonElements.getAsJsonArray().remove(Integer.parseInt(s[s.length - 1]));
                                        }
                                    }
                                } else {
                                    if (value.equals(values.get(i))) {
                                        if (object.isJsonObject()) {
                                            object.getAsJsonObject().remove(value);
                                        } else {
                                            object.getAsJsonArray().remove(i);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException ignored) {
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
                .serializer(serializer)
        );
    }
}