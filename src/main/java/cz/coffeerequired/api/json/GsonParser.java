package cz.coffeerequired.api.json;

import com.google.gson.*;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GsonParser {
    @Getter
    final static Gson gson = new GsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(ItemStack.class, new NBTFallBackItemStackAdapter())
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .registerTypeHierarchyAdapter(Object.class, new GenericFlatObjectAdapter<>())
            .setPrettyPrinting()
            .create();

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
            case null, default -> {
                return gson.toJsonTree(object);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(JsonElement json, Class<? extends T> clazz) {
        if (clazz == World.class || clazz.isAssignableFrom(World.class)) {
            return (T) Bukkit.getWorld(json.getAsJsonObject().get("worldName").getAsString());
        } else if (clazz == Chunk.class || clazz.isAssignableFrom(Chunk.class)) {
            JsonObject jsonObject = json.getAsJsonObject();
            World world = Bukkit.getWorld(jsonObject.get("worldName").getAsString());
            int x = jsonObject.get("x").getAsInt();
            int z = jsonObject.get("z").getAsInt();
            assert world != null;
            return (T) world.getChunkAt(x, z);
        } else if (clazz == Block.class || clazz.isAssignableFrom(Block.class)) {
            JsonObject jsonObject = json.getAsJsonObject();
            World world = Bukkit.getWorld(jsonObject.get("worldName").getAsString());
            assert world != null;
            Block block = world.getBlockAt(jsonObject.get("x").getAsInt(), jsonObject.get("y").getAsInt(), jsonObject.get("z").getAsInt());
            block.setType(Material.valueOf(jsonObject.get("type").getAsString()));
            return (T) block;
        } else if (clazz == Inventory.class || clazz.isAssignableFrom(Inventory.class)) {
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
            return gson.fromJson(json, clazz);
        }
    }
}
