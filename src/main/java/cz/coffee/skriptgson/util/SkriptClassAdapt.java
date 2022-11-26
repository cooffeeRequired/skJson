package cz.coffee.skriptgson.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;

import java.lang.reflect.Type;

import static cz.coffee.skriptgson.util.Utils.newGson;

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
            JsonSerializationContext context) {
        /*
        JsonElement json = new Gson().toJsonTree(new Fields(src));
         */
        //return JsonParser.parseString(json.toString().replaceAll("\\\\\"", ""));
        // TODO -> Make better serializer
        return newGson().toJsonTree(src.toString());
    }
}