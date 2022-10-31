package cz.coffee.skriptgson.skript;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.util.JsonMap;

import java.util.List;

import static cz.coffee.skriptgson.util.PluginUtils.SanitizeString;

@SuppressWarnings({"unused", "NullableProblems"})
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
                    String[] i;
                    JsonElement[] value = new JsonElement[]{JsonParser.parseString(String.valueOf(delta[0]))};
                    for (JsonElement object : what) {
                        for (JsonElement jsonElement : value) {
                            if (object.isJsonArray()) {
                                object.getAsJsonArray().add(jsonElement);
                            } else{
                                if (jsonElement.isJsonPrimitive()){
                                    i = new Gson().toJson(jsonElement.getAsJsonPrimitive())
                                            .split(":");
                                    jsonElement = JsonParser.parseString(SanitizeString(i[1]));
                                } else {
                                    i = new String[]{object.getAsJsonObject().entrySet().isEmpty() ?
                                            String.valueOf(0) :
                                            String.valueOf(object.getAsJsonObject().entrySet().toArray().length - 1)};
                                }
                                object.getAsJsonObject().add(SanitizeString(i[0]), jsonElement);
                            }
                        }
                    }
                }
                case REMOVE -> {
                    try {
                        String value = String.valueOf(delta[0]).replaceAll("\"", "");
                        for ( JsonElement object : what) {
                            List<String> values = JsonMap.getValues(object.getAsJsonObject());
                            for (int i = 0; values.size() > i; i++) {
                                if (value.contains(";")) {
                                    String[] s = value.split(";");
                                    JsonElement jsonElements = object.getAsJsonObject();
                                    for (String st : s) {
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
                    } catch (IndexOutOfBoundsException ignored) {}
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