package cz.coffeerequired.api.types;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.SerializedJson;
import cz.coffeerequired.api.json.SkriptJsonInputParser;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotNull;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

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
    public static Changer<JsonPath> changer = new Changer<>() {
        @Override
        public @Nullable Class<?>[] acceptChange(ChangeMode changeMode) {
            if (changeMode == ChangeMode.ADD) {
                return CollectionUtils.array(Object.class, Object[].class);
            }
            return null;
        }

        @Override
        public void change(JsonPath[] what, @Nullable Object[] delta, ChangeMode changeMode) {
            if (changeMode == ChangeMode.ADD) {
                if (delta == null || delta.length < 1) {
                    SkJson.warning("Module [Core]: delta need to be defined");
                    return;
                }

                JsonPath path = what[0];
                if (path == null) {
                    SkJson.warning("Module [Core]: json path is null");
                    return;
                }

                SerializedJson serializedJson = new SerializedJson(path.getInput());
                var converted = Arrays.stream(delta).filter(Objects::nonNull).map(GsonParser::toJson).toArray(JsonElement[]::new);

                IntStream.range(0, converted.length).forEach(idx -> {
                    var json = converted[idx];
                    var result = serializedJson.searcher.keyOrIndex(path.getKeys());
                    if (result == null) {
                        SkJson.severe("Module [Core]: result need to be defined");
                        return;
                    }
                    if (!(result instanceof JsonArray)) {
                        SkJson.severe("Module [Core]: additional can be used only for JSON arrays. | JSON array given " + result.getClass().getSimpleName());
                        return;
                    }
                    var keys = path.getKeys();
                    var key = Map.entry((((JsonArray) result).size()) + idx + "", SkriptJsonInputParser.Type.Index);
                    keys.add(key);
                    serializedJson.changer.value(keys, json);
                });
            }
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
