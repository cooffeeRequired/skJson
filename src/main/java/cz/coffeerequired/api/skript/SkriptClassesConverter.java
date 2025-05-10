package cz.coffeerequired.api.skript;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;
import ch.njol.skript.util.slot.Slot;
import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.Parser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public abstract class SkriptClassesConverter {

    public static final String SERIALIZED_TYPE_KEY = "..";

    public static JsonObject generateObject(Object o) {
        JsonObject obj = new JsonObject();
        obj.addProperty(SERIALIZED_TYPE_KEY, o.getClass().getName());
        obj.add("_data", new JsonObject());
        return obj;
    }

    private static JsonObject convertConfigurationSerializable(Object o) {
        JsonObject obj = new JsonObject();
        var serializable = (ConfigurationSerializable)o;
        obj.addProperty(
            ConfigurationSerialization.SERIALIZED_TYPE_KEY, 
            ConfigurationSerialization.getAlias(serializable.getClass())
        );
        Map<String, Object> values = serializable.serialize();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            obj.add(entry.getKey(), Parser .getGson().toJsonTree(entry.getValue()));
        }
        return obj;
    }

    public static class ItemTypeAdapter implements JsonSerializer<ItemType>, JsonDeserializer<ItemType> {
        @SuppressWarnings("ConstantValue")
        @Override
        public JsonElement serialize(ItemType src, Type typeOfSrc, JsonSerializationContext context) {
            ItemStack stack = null;
            var item = src.addTo(stack);
            return Parser.getGson().toJsonTree(item);
        }

        @Override
        public ItemType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            SkJson.exception(new JsonParseException("Failed to deserialize ItemType. ItemTypes cannot be deserialized from JSON without ItemStack"), "ItemType", json);
            return null;
        }
    }

    public static class SlotAdapter implements JsonSerializer<Slot>, JsonDeserializer<Slot> {
        @Override
        public JsonElement serialize(Slot src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null)
                return JsonNull.INSTANCE;
            return convertConfigurationSerializable(src.getItem());
        }

        @Override
        public Slot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            SkJson.exception(new JsonParseException("Failed to deserialize Slot. Slots cannot be deserialized from JSON without ItemStack"), "Slot", json);
            return null;
        }
    }

    public static class DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("time", src.getTime());
            return generic;
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("time"))
                throw new JsonParseException("Invalid Date format");
            return new Date(generic.getAsJsonObject("_data").get("time").getAsLong());
        }
    }

    public static class TimeAdapter implements JsonSerializer<Time>, JsonDeserializer<Time> {
        @Override
        public JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("ticks", src.getTicks());
            return generic;
        }

        @Override
        public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("ticks"))
                throw new JsonParseException("Invalid Time format");
            return new Time(generic.getAsJsonObject("_data").get("ticks").getAsInt());
        }
    }

    public static class TimespanAdapter implements JsonSerializer<Timespan>, JsonDeserializer<Timespan> {
        @Override
        public JsonElement serialize(Timespan src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            var data = generic.getAsJsonObject("_data");
            long totalMillis = src.getDuration().toMillis();
            
            long years = totalMillis / (365L * 24 * 60 * 60 * 1000);
            totalMillis %= (365L * 24 * 60 * 60 * 1000);
            
            long months = totalMillis / (30L * 24 * 60 * 60 * 1000);
            totalMillis %= (30L * 24 * 60 * 60 * 1000);
            
            long days = totalMillis / (24L * 60 * 60 * 1000);
            totalMillis %= (24L * 60 * 60 * 1000);
            
            long hours = totalMillis / (60L * 60 * 1000);
            totalMillis %= (60L * 60 * 1000);
            
            long minutes = totalMillis / (60L * 1000);
            totalMillis %= (60L * 1000);
            
            long seconds = totalMillis / 1000;
            
            data.addProperty("years", years);
            data.addProperty("months", months);
            data.addProperty("days", days);
            data.addProperty("hours", hours);
            data.addProperty("minutes", minutes);
            data.addProperty("seconds", seconds);
            return generic;
        }

        @Override
        public Timespan deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.has("_data"))
                throw new JsonParseException("Invalid Timespan format");

            var data = generic.getAsJsonObject("_data");
            long totalMillis = 0;
            
            totalMillis += data.get("years").getAsLong() * 365 * 24 * 60 * 60 * 1000L;
            totalMillis += data.get("months").getAsLong() * 30 * 24 * 60 * 60 * 1000L;
            totalMillis += data.get("days").getAsLong() * 24 * 60 * 60 * 1000L;
            totalMillis += data.get("hours").getAsLong() * 60 * 60 * 1000L;
            totalMillis += data.get("minutes").getAsLong() * 60 * 1000L;
            totalMillis += data.get("seconds").getAsLong() * 1000L;
            
            return new Timespan(totalMillis);
        }
    }

    public static class WeatherTypeAdapter implements JsonSerializer<WeatherType>, JsonDeserializer<WeatherType> {
        @Override
        public JsonElement serialize(WeatherType src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("weather", src.name());
            return generic;
        }

        @Override
        public WeatherType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("weather"))
                throw new JsonParseException("Invalid WeatherType format");
            return WeatherType.valueOf(generic.getAsJsonObject("_data").get("weather").getAsString());
        }
    }

    public static class VectorAdapter implements JsonSerializer<Vector>, JsonDeserializer<Vector> {
        @Override
        public JsonElement serialize(Vector src, Type typeOfSrc, JsonSerializationContext context) {
            SkJson.debug("serialize Vector", src);
            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("x", src.getX());
            generic.getAsJsonObject("_data").addProperty("y", src.getY());
            generic.getAsJsonObject("_data").addProperty("z", src.getZ());
            return generic;
        }

        @Override
        public Vector deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("x") || !generic.getAsJsonObject("_data").has("y") || !generic.getAsJsonObject("_data").has("z"))
                throw new JsonParseException("Invalid Vector format");
            return new Vector(
                    generic.getAsJsonObject("_data").get("x").getAsDouble(),
                    generic.getAsJsonObject("_data").get("y").getAsDouble(),
                    generic.getAsJsonObject("_data").get("z").getAsDouble());
        }
    }

    public static class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null)
                return JsonNull.INSTANCE;

            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("world", src.getWorld().getName());
            generic.getAsJsonObject("_data").addProperty("x", src.getX());
            generic.getAsJsonObject("_data").addProperty("y", src.getY());
            generic.getAsJsonObject("_data").addProperty("z", src.getZ());
            generic.getAsJsonObject("_data").addProperty("pitch", src.getPitch());
            generic.getAsJsonObject("_data").addProperty("yaw", src.getYaw());
            return generic;
        }

        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            var _data = generic.getAsJsonObject("_data");
            
            return new Location(
                    Bukkit.getWorld(_data.get("world").getAsString()),
                    _data.get("x").getAsDouble(),
                    _data.get("y").getAsDouble(),
                    _data.get("z").getAsDouble(),
                    _data.get("yaw").getAsFloat(),
                    _data.get("pitch").getAsFloat());
        }
    }

    public static class BlockDataAdapter implements JsonSerializer<BlockData>, JsonDeserializer<BlockData> {
        @Override
        public JsonElement serialize(BlockData src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("blockData", src.getAsString());
            return generic;
        }

        @Override
        public BlockData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("blockData"))
                throw new JsonParseException("Invalid BlockData format");
            return org.bukkit.Bukkit.createBlockData(generic.getAsJsonObject("_data").get("blockData").getAsString());
        }
    }

    public static class PlayerAdapter implements JsonSerializer<Player>, JsonDeserializer<Player> {
        @Override
        public JsonElement serialize(Player src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            generic.getAsJsonObject("_data").addProperty("uuid", src.getUniqueId().toString());
            generic.getAsJsonObject("_data").addProperty("name", src.getName());
            generic.getAsJsonObject("_data").addProperty("type", "player");
            return generic;
        }

        @Override
        public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("uuid"))
                throw new JsonParseException("Invalid Player format");
            return Bukkit.getPlayer(UUID.fromString(generic.getAsJsonObject("_data").get("uuid").getAsString()));
        }
    }

    @SuppressWarnings("ConstantValue")
    public static String getInventoryTitle(Inventory inventory) {
        try {
            Method getTitleMethod = inventory.getClass().getMethod("getTitle");
            if (getTitleMethod != null) {
                return (String) getTitleMethod.invoke(inventory);
            }
            Field titleField = inventory.getClass().getDeclaredField("title");
            titleField.setAccessible(true);
            Object title = titleField.get(inventory);
            if (title instanceof Component component) {
                return PlainTextComponentSerializer.plainText().serialize(component);
            } else if (title != null) {
                return title.toString();
            }
        } catch (Exception ignored) {

        }
        return "inventory of " + inventory.getType().toString().toLowerCase();
    }

    /**
     * Sets the title of an inventory using reflection.
     * @param inventory The inventory to set the title for
     * @param title The new title to set
     * @return true if successful, false otherwise
     */
    @SuppressWarnings({"ConstantValue", "unused"})
    public static boolean setInventoryTitle(Inventory inventory, String title) {
        try {
            try {
                Method setTitleMethod = inventory.getClass().getMethod("setTitle", String.class);
                if (setTitleMethod != null) {
                    setTitleMethod.invoke(inventory, title);
                    return true;
                }
            } catch (NoSuchMethodException e) {
                try {
                    Method setTitleMethod = inventory.getClass().getMethod("setTitle", Component.class);
                    if (setTitleMethod != null) {
                        Component component = LegacyComponentSerializer.legacySection().deserialize(title);
                        setTitleMethod.invoke(inventory, component);
                        return true;
                    }
                } catch (NoSuchMethodException ex) {
                    // No setTitle method exists, try field access
                }
            }
            Field titleField = inventory.getClass().getDeclaredField("title");
            titleField.setAccessible(true);

            Class<?> fieldType = titleField.getType();
            if (Component.class.isAssignableFrom(fieldType)) {
                Component component = LegacyComponentSerializer.legacySection().deserialize(title);
                titleField.set(inventory, component);
            } else if (Component[].class.isAssignableFrom(fieldType)) {
                // For older versions that use BungeeCord's BaseComponent
                TextComponent textComponent = LegacyComponentSerializer.legacySection().deserialize(title);
                titleField.set(inventory, new Component[] { textComponent });
            } else {
                // Assume String or Object
                titleField.set(inventory, title);
            }
            return true;
        } catch (Exception e) {
            // Fallback failed
            return false;
        }
    }

    public static class InventoryAdapter implements JsonSerializer<Inventory>, JsonDeserializer<Inventory> {
        @Override
        public JsonElement serialize(Inventory src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            generic.addProperty(SERIALIZED_TYPE_KEY, Inventory.class.getName());
            var type = src.getType();
            final String sourceType = type.toString();

            String invJsonTitle;
            JsonElement stringifyInventoryHolder;

            var potentialHolder = src.getHolder();

            if (potentialHolder instanceof Entity e) {
                invJsonTitle = e.getName();
                stringifyInventoryHolder = context.serialize(potentialHolder);
            } else if (type == InventoryType.CHEST ||
                    type== InventoryType.HOPPER ||
                    type== InventoryType.SHULKER_BOX ||
                    type== InventoryType.BARREL ||
                    type== InventoryType.DROPPER ||
                    type== InventoryType.DISPENSER ||
                    type== InventoryType.FURNACE ||
                    type== InventoryType.WORKBENCH
            ) {
                invJsonTitle = sourceType.toLowerCase();
                stringifyInventoryHolder = null;
            } else {
                invJsonTitle = "unknown";
                stringifyInventoryHolder = null;
            }
            var _data = generic.getAsJsonObject("_data");
            _data.addProperty("type", sourceType);
            _data.addProperty("title", invJsonTitle);
            _data.add("holder", stringifyInventoryHolder);
            _data.add("contents", context.serialize(src.getContents()));
            _data.addProperty("customTitle", getInventoryTitle(src));
            return generic;
        }

        @Override
        public Inventory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.has("_data"))
                throw new JsonParseException("Invalid Inventory format");

            Inventory inventory;

            var _data = generic.getAsJsonObject("_data");
            String title = _data.get("title").getAsString();
            var holder = _data.get("holder");
            String type = _data.get("type").getAsString();
            InventoryType inventoryType = InventoryType.valueOf(type.toUpperCase());

            var customTitle = _data.get("customTitle").getAsString() == null ? "inventory of " + title : _data.get("customTitle").getAsString();
            Component component = LegacyComponentSerializer.legacySection().deserialize(customTitle);

            if (holder.isJsonNull()) {
                inventory = Bukkit.createInventory(null, inventoryType, component);
            } else {
                try {
                    InventoryHolder inventoryHolder = context.deserialize(holder, InventoryHolder.class);
                    inventory = Bukkit.createInventory(inventoryHolder, inventoryType, Component.text("inventory of " + ((Player) inventoryHolder).getName()));
                } catch (Exception e) {
                    inventory = Bukkit.createInventory(null, inventoryType, component);
                }
            }

            inventory.setContents(context.deserialize(_data.get("contents"), inventory.getContents().getClass()));
            return inventory;
        }
    }

    public static class InventoryHolderAdapter implements JsonSerializer<InventoryHolder>, JsonDeserializer<InventoryHolder> {
        @Override
        public JsonElement serialize(InventoryHolder src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }
            
            var generic = generateObject(src);
            var _data = generic.getAsJsonObject("_data");
            
            if (src instanceof Player player) {
                _data.addProperty("type", "player");
                _data.addProperty("uuid", player.getUniqueId().toString());
                _data.addProperty("name", player.getName());
            } else {
                _data.addProperty("type", "unknown");
                _data.addProperty("class", src.getClass().getName());
            }
            
            return generic;
        }
        
        @Override
        public InventoryHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            if (json.isJsonNull()) {
                return null;
            }
            
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.has("_data")) {
                throw new JsonParseException("Invalid InventoryHolder format");
            }
            
            var _data = generic.getAsJsonObject("_data");
            String type = _data.get("type").getAsString();
            
            if ("player".equals(type)) {
                String uuid = _data.get("uuid").getAsString();
                return Bukkit.getPlayer(UUID.fromString(uuid));
            }
            
            // Return null for unknown types, let the caller handle it
            return null;
        }
    }
    
    public static class WorldAdapter implements JsonSerializer<World>, JsonDeserializer<World> {
        @Override
        public JsonElement serialize(World src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            var _data = generic.getAsJsonObject("_data");
            _data.addProperty("name", src.getName());
            return generic;
        }

        @Override
        public World deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            var _data = generic.getAsJsonObject("_data");
            String name = _data.get("name").getAsString();
            return Bukkit.getWorld(name);
        }
    }

    public static class BlockAdapter implements JsonSerializer<Block>, JsonDeserializer<Block> {
        @Override
        public JsonElement serialize(Block src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            var _data = generic.getAsJsonObject("_data");
            _data.add("world", context.serialize(src.getWorld(), World.class));
            _data.addProperty("x", src.getX());
            _data.addProperty("y", src.getY());
            _data.addProperty("z", src.getZ());
            _data.add("blockData", context.serialize(src.getBlockData(), BlockData.class));
            return generic;
        }

        @Override
        public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            var generic = json.getAsJsonObject();
            var _data = generic.getAsJsonObject("_data");
            World world = context.deserialize(_data.get("world"), World.class);
            int x = _data.get("x").getAsInt();
            int y = _data.get("y").getAsInt();
            int z = _data.get("z").getAsInt();
            return world.getBlockAt(x, y, z);
        }
    }

    public static class ChunkAdapter implements JsonSerializer<Chunk>, JsonDeserializer<Chunk> {
        @Override
        public Chunk deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var generic = json.getAsJsonObject();
            if (!generic.has(SERIALIZED_TYPE_KEY) || !generic.getAsJsonObject("_data").has("x") || !generic.getAsJsonObject("_data").has("z") || !generic.getAsJsonObject("_data").has("world"))
                throw new JsonParseException("Invalid Chunk format");
            World world = context.deserialize(generic.getAsJsonObject("_data").get("world"), World.class);
            int x = generic.getAsJsonObject("_data").get("x").getAsInt();
            int z = generic.getAsJsonObject("_data").get("z").getAsInt();
            return world.getChunkAt(x, z);
        }

        @Override
        public JsonElement serialize(Chunk src, Type typeOfSrc, JsonSerializationContext context) {
            var generic = generateObject(src);
            var _data = generic.getAsJsonObject("_data");
            _data.addProperty("x", src.getX());
            _data.addProperty("z", src.getZ());
            _data.add("world", context.serialize(src.getWorld(), World.class));
            return generic;
        }
    }

    public static class SkriptClassAdapter implements JsonSerializer<SkriptClass>, JsonDeserializer<SkriptClass> {
        @Override
        public JsonElement serialize(SkriptClass src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty(SkriptClassesConverter.SERIALIZED_TYPE_KEY, src.getType());
            obj.addProperty("data", src.getData());
            return obj;
        }

        @Override
        public SkriptClass deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            String type = obj.get(SkriptClassesConverter.SERIALIZED_TYPE_KEY).getAsString();
            String data = obj.get("data").getAsString();

            try {
                byte[] decodedData = Base64.getDecoder().decode(data);
                return new SkriptClass(type, decodedData);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Failed to decode Base64 data", e);
            }
        }
    }
}
