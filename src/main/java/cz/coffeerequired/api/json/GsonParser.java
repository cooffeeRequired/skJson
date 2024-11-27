package cz.coffeerequired.api.json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
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

import java.util.Objects;

import static cz.coffeerequired.api.Api.SERIALIZED_TYPE_KEY;

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

    public static String toPrettyPrintString(JsonElement json) {
        return GsonParser.gson.toJson(json);
    }

    public static <T> JsonElement toJson(T object) {
        switch (object) {
            case World w -> {
                return JsonParser.parseString(String.format("{\"%s\": \"%s\", \"worldName\": \"%s\"}", SERIALIZED_TYPE_KEY, w.getClass().getName(), w.getName()));
            }
            case Chunk chunk -> {
                return JsonParser.parseString(String.format("{\"%s\": \"%s\", \"worldName\": \"%s\", \"x\": %d, \"z\": %d}", SERIALIZED_TYPE_KEY, chunk.getClass().getName(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
            }
            case Block block -> {
                var o = JsonParser.parseString(String.format(
                        "{\"%s\": \"%s\", \"worldName\": \"%s\", \"x\": %d, \"y\": %d, \"z\": %d}",
                        SERIALIZED_TYPE_KEY,
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
                        "{\"%s\": \"%s\", \"title\": \"%s\", \"holder\": %s, \"size\": %d}",
                        SERIALIZED_TYPE_KEY,
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
            case null -> {
                return JsonNull.INSTANCE;
            }
            default -> {
                JsonElement serialized = SerializedJsonUtils.lazyObjectConverter(object);
                if (serialized != null && !serialized.isJsonNull()) return serialized;
                return gson.toJsonTree(object);
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        try {
            if (json.isJsonObject() && json.getAsJsonObject().has(SERIALIZED_TYPE_KEY)) {
                String potentialClass  = json.getAsJsonObject().get(SERIALIZED_TYPE_KEY).getAsString();
                Class<?> clazz = Class.forName(potentialClass);
                return (T) fromJson(json, clazz);
            }
        } catch (ClassNotFoundException notFoundException) {
            throw new JsonParseException("Class not found");
        }
        return (T) fromJson(json, Object.class);
    }





    @SuppressWarnings("unchecked")
    public static <T> T fromJson(JsonElement json, Class<? extends T> clazz) {
        SkJson.logger().info(String.format("Parsing %s to %s : %s", json.toString(), clazz.getName(), World.class.isAssignableFrom(clazz)));
        if (json.isJsonPrimitive()) {
            return gson.fromJson(json, clazz);
        } else if (json.isJsonObject()) {
            if (json.getAsJsonObject().has(SERIALIZED_TYPE_KEY)) {
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
                    return gson.fromJson(json, clazz);
                }
            }
        }
        return (T) json;
    }
}
