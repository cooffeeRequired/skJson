package cz.coffee.adapters;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;

public class TypeAdapterSkriptClass implements JsonSerializer<YggdrasilSerializable>, JsonDeserializer<YggdrasilSerializable> {
    @Override
    public YggdrasilSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(YggdrasilSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        try {
            Fields fields = new Fields(src);
            fields.forEach(fieldContext -> {
                if (fieldContext.isPrimitive()) {
                    try {
                        object.addProperty(fieldContext.getID(), new Gson().toJson(fieldContext.getPrimitive()));
                    } catch (StreamCorruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        object.add(fieldContext.getID(), new Gson().toJsonTree(fieldContext.getObject()));
                    } catch (StreamCorruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (NotSerializableException e) {
            throw new RuntimeException(e);
        }
        return object;
    }
}