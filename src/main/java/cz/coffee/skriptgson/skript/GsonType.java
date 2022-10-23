package cz.coffee.skriptgson.skript;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

public class GsonType {

    public GsonType() {}

    static {
        Classes.registerClass(new ClassInfo<>(JsonElement.class, "jsonelement")
                .user("json[[ ]element]")
                .name("Json Element")
                .description("Representing a JSON element")
                .since("1.0")
                .parser(new Parser<JsonElement>() {
                    @Override
                    @Nullable
                    public JsonElement parse(final String s, final ParseContext context) {
                        return null;
                    }

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
                        return json.getAsString();
                    }
                })
        );
    }

}
