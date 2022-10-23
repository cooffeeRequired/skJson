package cz.coffee.skriptgson.skript;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GsonType {

    private static final Parser<JsonElement> parser = new Parser<JsonElement>() {
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

    private static final Changer<JsonElement> changer = new Changer<JsonElement>() {
        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            switch (mode) {
                case ADD:
                case REMOVE:
                    return CollectionUtils.array(JsonElement.class);
            }
            return null;
        }

        @Override
        public void change(JsonElement[] what, Object[] delta, ChangeMode mode) {
            switch (mode) {
                case ADD: {
                    for(JsonArray object : (JsonArray[]) what) { // TODO fix this cast shit
                        for(JsonElement jsonElement : (JsonElement[]) delta) {
                            object.add(jsonElement);
                        }
                    }
                }
                case REMOVE: {
                    for(JsonArray object : (JsonArray[]) what) {
                        for(JsonElement jsonElement : (JsonElement[]) delta) {
                            object.remove(jsonElement);
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