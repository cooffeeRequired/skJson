package cz.coffeerequired.api.json;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Map;

@Data
public class JsonPath {

    public static Parser<JsonPath> parser = new Parser<>() {

        @Override
        public String toString(JsonPath jsonPath, int i) {
            return jsonPath.toString();
        }


        @Override
        public @NotNull String toVariableNameString(JsonPath o) {
            return toString(o, 1);
        }

        @Override
        public boolean canParse(@NotNull ParseContext context) {
            return false;
        }
    };
    public static Serializer<JsonPath> serializer = new Serializer<>() {

        @Override
        public Fields serialize(JsonPath jsonPath) throws NotSerializableException {
            Fields fields = new Fields();
            fields.putObject("json path", jsonPath.toString());
            return fields;
        }

        @Override
        public void deserialize(JsonPath o, @NotNull Fields f) {
            assert false;
        }

        @Override
        public JsonPath deserialize(@NotNull Fields fields) throws StreamCorruptedException {
            Object field = fields.getObject("json path");
            if (field == null) return null;
            fields.removeField("json path");
            return (JsonPath) field;
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
    private JsonElement input;
    private String path;
    private ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> keys;

    public JsonPath(JsonElement input, String path, ArrayList<Map.Entry<String, SkriptJsonInputParser.Type>> keys) {
        this.input = input;
        this.path = path;
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "json path of '" + path + "' in " + input;
    }
}
