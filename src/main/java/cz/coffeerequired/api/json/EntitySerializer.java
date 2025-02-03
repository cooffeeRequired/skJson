package cz.coffeerequired.api.json;

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

@SuppressWarnings("deprecation")
public class EntitySerializer implements JsonSerializer<Entity>, JsonDeserializer<Entity> {

    @Override
    public JsonElement serialize(Entity src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("uuid", src.getUniqueId().toString());
        obj.addProperty("type", src.getType().toString());
        obj.add("location", GsonParser.toJson(src.getLocation()));

        if (src.customName() != null) {
            obj.addProperty("customName", PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(src.customName())));
        } else if (src instanceof Player player) {
            obj.addProperty("name", player.getName());
        }

        if (Api.Records.PROJECT_ENABLED_NBT) {
            NBTEntity nbtEntity = new NBTEntity(src);
            JsonElement json = NBTConverter.toJson(new NBTContainer(nbtEntity.getCompound()));
            obj.add("nbt", json);
        }

        return obj;
    }

    @Override
    public Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        String uuidStr = obj.get("uuid").getAsString();
        UUID uuid = UUID.fromString(uuidStr);
        Location loc = GsonParser.fromJson(obj.getAsJsonObject("location"));

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

