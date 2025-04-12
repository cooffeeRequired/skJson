package cz.coffeerequired.api.types;

import ch.njol.skript.classes.Serializer;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffeerequired.api.json.GsonParser;

import javax.validation.constraints.NotNull;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

public class JSONTypeSerializer extends Serializer<JsonElement> {
    @Override
    public Fields serialize(JsonElement o) throws NotSerializableException {
        Fields fields = new Fields();
        fields.putObject("json", o.toString());
        return fields;
    }

    @Override
    public void deserialize(JsonElement o, @NotNull Fields f) {
        assert false;
    }

    @Override
    public JsonElement deserialize(@NotNull Fields fields) throws StreamCorruptedException {
        Object field = fields.getObject("json");
        if (field == null) return JsonNull.INSTANCE;
        fields.removeField("json");
        return GsonParser.toJson(field);
    }

    @Override
    public boolean mustSyncDeserialization() {
        return false;
    }

    @Override
    protected boolean canBeInstantiated() {
        return false;
    }
}
