package cz.coffee.skriptgson.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;

import java.lang.reflect.Type;

public class SkriptClassAdapt implements JsonSerializer<YggdrasilSerializable>, JsonDeserializer<YggdrasilSerializable> {

    @Override
    public YggdrasilSerializable deserialize(JsonElement json,
                                             Type typeOfT,
                                             JsonDeserializationContext context) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(
            YggdrasilSerializable src,
            Type typeOfSrc,
            JsonSerializationContext context)
    {
        return new Gson().toJsonTree(src.toString());
    }
}
