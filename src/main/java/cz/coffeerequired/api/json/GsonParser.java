package cz.coffeerequired.api.json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unchecked")
public class GsonParser {
    @Getter
    final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .registerTypeAdapter(ItemStack.class, new NBTFallBackItemStackAdapter())
            .registerTypeHierarchyAdapter(Entity.class, new EntitySerializer())
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .setLenient()
            .setPrettyPrinting()
            .create();

    public static String toPrettyPrintString(JsonElement json) {
        return GsonParser.gson.toJson(json);
    }

    public static <T> JsonElement toJson(T object) {
        switch (object) {
            case World w -> {
                return JsonParser.parseString(String.format("{\"class\": \"%s\", \"worldName\": \"%s\"}", w.getClass().getName(), w.getName()));
            }
            case Chunk chunk -> {
                return JsonParser.parseString(String.format("{\"class\": \"%s\", \"worldName\": \"%s\", \"x\": %d, \"z\": %d}", chunk.getClass().getName(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
            }
            case Block block -> {
                var o = JsonParser.parseString(String.format(
                        "{\"class\": \"%s\", \"worldName\": \"%s\", \"x\": %d, \"y\": %d, \"z\": %d}",
                        block.getClass().getName(),
                        block.getWorld().getName(),
                        block.getX(),
                        block.getY(),
                        block.getZ())
                );
                o.getAsJsonObject().addProperty("type", block.getType().toString());
                o.getAsJsonObject().addProperty("data", block.getBlockData().getAsString());
                return o;
            }
            case Inventory inventory -> {
                final String sourceType = inventory.getType().toString();
                String invJsonTitle;
                String stringifyInventoryHolder;

                if (inventory.getHolder() == null) {
                    invJsonTitle = "null";
                    stringifyInventoryHolder = "null";
                } else {
                    invJsonTitle = inventory.getHolder().getClass().getName();
                    stringifyInventoryHolder = gson.toJson(inventory.getHolder());
                }
                var o = JsonParser.parseString(String.format(
                        "{\"class\": \"%s\", \"title\": \"%s\", \"holder\": %s, \"size\": %d}",
                        sourceType,
                        invJsonTitle,
                        stringifyInventoryHolder,
                        inventory.getSize()));

                o.getAsJsonObject().add("slots", new JsonArray());

                for (ItemStack itemStack : inventory.getContents()) {
                    o.getAsJsonObject().getAsJsonArray("slots").add(gson.toJsonTree(itemStack));
                }
                return o;
            }
            default -> {
                JsonElement serialized = SerializedJsonUtils.lazyObjectConverter(object);
                if (serialized != null && !serialized.isJsonNull()) return serialized;
                var t = gson.toJsonTree(object);

                SkJson.debug("Serialized object: %s, class: %s, isEntity: %s", t, object.getClass(), object instanceof Entity);

                return t;
            }
        }
    }

    public static <T> T fromJson(JsonElement json) {

        if (json == null) {
            SkJson.warning("Depth error, json cannot be null, check variable path or input source");
            return null;
        }

        if (json.isJsonPrimitive()) return SerializedJsonUtils.lazyJsonConverter(json);

        Class<?> clazz = null;

        try {
            if (json.isJsonObject()) {
                if (json.getAsJsonObject().has("class")) {
                    String className = json.getAsJsonObject().get("class").getAsString();
                    clazz = Class.forName(className);
                }
            }
        } catch (Exception e) {
            throw new JsonParseException("Could not deserialize class");
        }

        if (clazz != null) {
            if (clazz == World.class || World.class.isAssignableFrom(clazz)) {
                return (T) Bukkit.getWorld(json.getAsJsonObject().get("worldName").getAsString());
            } else if (clazz == Chunk.class || Chunk.class.isAssignableFrom(clazz)) {
                JsonObject jsonObject = json.getAsJsonObject();
                World world = Bukkit.getWorld(jsonObject.get("worldName").getAsString());
                int x = jsonObject.get("x").getAsInt();
                int z = jsonObject.get("z").getAsInt();
                assert world != null;
                return (T) world.getChunkAt(x, z);
            } else if (clazz == Block.class || Block.class.isAssignableFrom(clazz)) {
                JsonObject jsonObject = json.getAsJsonObject();
                World world = Bukkit.getWorld(jsonObject.get("worldName").getAsString());
                assert world != null;
                Block block = world.getBlockAt(jsonObject.get("x").getAsInt(), jsonObject.get("y").getAsInt(), jsonObject.get("z").getAsInt());
                block.setType(Material.valueOf(jsonObject.get("type").getAsString()));
                return (T) block;
            } else if (clazz == Inventory.class || Inventory.class.isAssignableFrom(clazz)) {
                JsonObject jsonObject = json.getAsJsonObject();
                int size = jsonObject.get("size").getAsInt();
                String title = jsonObject.get("title").getAsString();
                Inventory inventory = Bukkit.createInventory(null, size, Component.text(title));

                JsonArray itemsArray = jsonObject.getAsJsonArray("slots");
                for (int i = 0; i < itemsArray.size(); i++) {
                    JsonElement itemElement = itemsArray.get(i);
                    if (!itemElement.isJsonNull()) {
                        ItemStack item = gson.fromJson(itemElement, ItemStack.class);
                        inventory.setItem(i, item);
                    }
                }
                return (T) inventory;
            } else {
                return (T) gson.fromJson(json, clazz);
            }
        }
        return (T) json;
    }
}
