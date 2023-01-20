package cz.coffee.adapters.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.utils.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.json.JsonUtils.check;


public class JsonEntity implements JsonGenericAdapter<Entity> {
    @Override
    public @NotNull JsonElement toJson(Entity object) {
        JsonObject entityJson = new JsonObject();
        entityJson.addProperty("??", Entity.class.getSimpleName());
        JsonObject entityData = new JsonObject();
        entityData.addProperty("Entity-ID", object.getEntityId());
        entityData.addProperty("UUID", object.getUniqueId().toString());
        entityData.add("Chunk", new JsonChunk().toJson(object.getChunk()));
        entityData.add("Location", gsonAdapter.toJsonTree(object.getLocation()));
        entityData.add("World", new JsonWorld().toJson(object.getWorld()));
        entityData.add("entity-properties", new JsonObject());
        entityData.getAsJsonObject("entity-properties").addProperty("fall-distance", object.getFallDistance());
        entityData.getAsJsonObject("entity-properties").addProperty("name", object.getName());
        entityData.getAsJsonObject("entity-properties").addProperty("Pose", object.getPose().name());
        entityData.getAsJsonObject("entity-properties").addProperty("custom-name", object.getCustomName());
        entityData.getAsJsonObject("entity-properties").addProperty("type", object.getType().name());
        entityData.getAsJsonObject("entity-properties").addProperty("width", object.getWidth());
        entityData.getAsJsonObject("entity-properties").addProperty("height", object.getHeight());
        entityData.getAsJsonObject("entity-properties").addProperty("Fire-max-ticks", object.getMaxFireTicks());
        entityData.getAsJsonObject("entity-properties").addProperty("Fire-ticks", object.getFireTicks());
        entityData.getAsJsonObject("entity-properties").addProperty("ticks-Lived", object.getTicksLived());
        entityData.getAsJsonObject("entity-properties").addProperty("facing", object.getFacing().ordinal());
        entityData.getAsJsonObject("entity-properties").addProperty("onGround", object.isOnGround());
        entityData.getAsJsonObject("entity-properties").addProperty("inWater", object.isInWater());
        entityData.getAsJsonObject("entity-properties").addProperty("isDead", object.isDead());
        entityData.getAsJsonObject("entity-properties").addProperty("isValid", object.isValid());

        entityData.getAsJsonObject("entity-properties").add("velocity", new JsonObject());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("velocity").addProperty("X", object.getVelocity().getX());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("velocity").addProperty("Block-X", object.getVelocity().getBlockX());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("velocity").addProperty("Y", object.getVelocity().getY());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("velocity").addProperty("Block-Y", object.getVelocity().getBlockY());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("velocity").addProperty("Z", object.getVelocity().getZ());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("velocity").addProperty("Block-Z", object.getVelocity().getBlockZ());

        entityData.getAsJsonObject("entity-properties").add("bounded-box", new JsonObject());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("bounded-box").addProperty("Min-X", object.getBoundingBox().getMinX());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("bounded-box").addProperty("Max-X", object.getBoundingBox().getMaxX());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("bounded-box").addProperty("Min-Y", object.getBoundingBox().getMinY());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("bounded-box").addProperty("Max-Y", object.getBoundingBox().getMaxY());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("bounded-box").addProperty("Min-Z", object.getBoundingBox().getMinZ());
        entityData.getAsJsonObject("entity-properties").getAsJsonObject("bounded-box").addProperty("Max-Z", object.getBoundingBox().getMaxZ());


        entityJson.add("data", entityData);
        return entityJson;
    }

    @Override
    public Entity fromJson(JsonElement json) {

        final JsonObject data = json.getAsJsonObject().getAsJsonObject("data");
        final JsonObject properties = json.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entity-properties");

        Entity entity = Bukkit.getEntity(UUID.fromString(data.get("UUID").getAsString()));
        assert entity != null;
        entity.setCustomName(properties.get("name").getAsString());
        entity.setFallDistance(properties.get("fall-distance").getAsInt());
        entity.setCustomName((properties.get("custom-name") == JsonNull.INSTANCE ? "" : properties.get("custom-name").getAsString()));
        return entity;
    }

    @Override
    public Class<? extends Entity> typeOf(JsonElement json) {
        if (check(json, Entity.class.getSimpleName(), Type.KEY)) {
            return Entity.class;
        }
        return null;
    }
}
