package cz.coffeerequired.api.json;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.slot.Slot;
import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.adapters.BukkitSerializableAdapter;
import cz.coffeerequired.api.adapters.LocationAdapter;
import cz.coffeerequired.api.adapters.NBTFallBackItemStackAdapter;
import cz.coffeerequired.api.types.EntitySerializer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
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
            .setStrictness(Strictness.LENIENT)
            .setPrettyPrinting()
            .create();

    public static String toPrettyPrintString(JsonElement json) {
        return GsonParser.gson.toJson(json);
    }

    public static <T> JsonElement toJson(T object) {

        final boolean isItem = object instanceof ItemStack || object instanceof Slot || object instanceof ItemType || object instanceof ItemData;

        if (isItem) {
            Class<?> cls = object.getClass();
            if (cls.equals(ItemType.class) || ItemType.class.isAssignableFrom(cls)) {
                assert object instanceof ItemType;
                return gson.toJsonTree(((ItemType) object).getAll());
            } else if (cls.equals(ItemStack.class) || ItemStack.class.isAssignableFrom(cls)) {
                assert object instanceof ItemStack;
                return gson.toJsonTree(object);
            } else if (Slot.class.isAssignableFrom(cls)) {
                assert object instanceof Slot;
                return gson.toJsonTree(((Slot) object).getItem());
            } else if (cls.equals(ItemData.class) || ItemData.class.isAssignableFrom(cls)) {
                assert object instanceof ItemData;
                return gson.toJsonTree((((ItemData) object).getStack()));
            }
        }

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

                if (inventory.getHolder() instanceof Entity e) {
                    invJsonTitle = e.getName();
                    stringifyInventoryHolder = gson.toJson(inventory.getHolder());
                } else if (inventory.getType() == InventoryType.CHEST ||
                        inventory.getType() == InventoryType.HOPPER ||
                        inventory.getType() == InventoryType.SHULKER_BOX ||
                        inventory.getType() == InventoryType.BARREL ||
                        inventory.getType() == InventoryType.DROPPER ||
                        inventory.getType() == InventoryType.DISPENSER ||
                        inventory.getType() == InventoryType.FURNACE ||
                        inventory.getType() == InventoryType.WORKBENCH
                ) {
                    invJsonTitle = "inventory from " + inventory.getType().toString().toLowerCase();
                    stringifyInventoryHolder = null;
                } else {
                    invJsonTitle = "unknown";
                    stringifyInventoryHolder = null;
                }
                var o = JsonParser.parseString(String.format(
                        "{\"type\": \"%s\", \"class\": \"%s\", \"title\": \"%s\", \"holder\": %s, \"size\": %d}",
                        sourceType,
                        inventory.getClass().getName(),
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
                return gson.toJsonTree(object);
            }
        }
    }

    public static <T> T fromJson(JsonElement json) {
        if (json == null) {
            SkJson.severe("Depth error, json cannot be null, check variable path or input source");
            return null;
        }

        Class<?> clazz = null;

        if (json.isJsonPrimitive()) return SerializedJsonUtils.lazyJsonConverter(json);
        else if (json.isJsonObject() && json.getAsJsonObject().has("class")) {
            try {
                String className = json.getAsJsonObject().get("class").getAsString();
                clazz = Class.forName(className);
            } catch (Exception e) {
                SkJson.exception(e, "Could not deserialize class");
            }
        }

        SkJson.debug("Converting >> from: %s [found class: %s] to %s", json.getClass(), json.getAsJsonObject().get("class"), clazz == null ? "null" : clazz.getName() );

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
                String title = jsonObject.get("title").getAsString();

                Inventory inventory;

                if (jsonObject.has("holder") && !jsonObject.get("holder").isJsonNull()) {
                    inventory = Bukkit.createInventory(null, InventoryType.valueOf(jsonObject.get("type").getAsString().toUpperCase()), Component.text("inventory of "+  title));
                } else {
                    inventory = Bukkit.createInventory(null, InventoryType.valueOf(jsonObject.get("type").getAsString().toUpperCase()), Component.text(title));
                }

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
