package cz.coffeerequired.api.types;

import com.google.gson.*;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.nbts.NBTConverter;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

import static cz.coffeerequired.api.skript.SkriptClassesConverter.generateObject;

@SuppressWarnings("deprecation")
public class EntitySerializer implements JsonSerializer<Entity>, JsonDeserializer<Entity> {

    @Override
    public JsonElement serialize(Entity src, Type typeOfSrc, JsonSerializationContext context) {
        var generic = generateObject(src);
        var _data = generic.getAsJsonObject("_data");
        _data.addProperty("uuid", src.getUniqueId().toString());
        _data.addProperty("type", src.getType().toString());
        _data.add("location", context.serialize(src.getLocation()));

        if (src.customName() != null) {
            _data.addProperty("customName", PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(src.customName())));
        } else if (src instanceof Player player) {
            _data.addProperty("name", player.getName());
        }

        if (Api.Records.PROJECT_ENABLED_NBT) {
            NBTEntity nbtEntity = new NBTEntity(src);
            JsonElement json = NBTConverter.toJson(new NBTContainer(nbtEntity.getCompound()));
            _data.add("nbt", json);
        }

        return generic;
    }

    @Override
    public Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject _json = json.getAsJsonObject();
        var obj = _json.getAsJsonObject("_data");

        String uuidStr = obj.get("uuid").getAsString();
        UUID uuid = UUID.fromString(uuidStr);
        Location loc = context.deserialize(obj.get("location"), Location.class);

        assert loc != null;
        World world = loc.getWorld();
        Entity entity = world.getEntity(uuid);

        if (entity == null) {
            throw new JsonParseException("Entity with UUID " + uuid + " could not be found");
        }
        if (obj.has("customName")) {
            entity.setCustomName(obj.get("customName").getAsString());
        }
        if (obj.has("nbt")) {
            if (Api.Records.PROJECT_ENABLED_NBT) {
                NBTContainer cont = NBTConverter.fromJson(obj.getAsJsonObject("nbt"));
                NBT.modify(entity, nbt -> {
                    nbt.clearNBT();
                    nbt.mergeCompound(cont);
                });
            }
        }

        return entity;
    }
}

