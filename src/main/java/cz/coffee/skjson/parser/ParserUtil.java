package cz.coffee.skjson.parser;

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.slot.Slot;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.shanebeestudios.skbee.api.nbt.NBTContainer;
import com.shanebeestudios.skbee.api.nbt.NBTCustom;
import cz.coffee.skjson.api.DynamicObjectSerializer;
import cz.coffee.skjson.skript.base.Converter;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static cz.coffee.skjson.api.ConfigRecords.PROJECT_DEBUG;
import static cz.coffee.skjson.skript.base.Converter.*;
import static cz.coffee.skjson.skript.base.SimpleConverter.SERIALIZED_JSON_TYPE_KEY;
import static cz.coffee.skjson.utils.Logger.error;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

/**
 * The type Parser util.
 */
@SuppressWarnings("all")
public abstract class ParserUtil {
    /**
     * The constant GsonConverter.
     */
    public static Gson GsonConverter = new GsonBuilder()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(Player.class, new DynamicObjectSerializer<Player>())
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new Converter.BukkitConverter())
            .create();

    /**
     * Fix quotes string.
     *
     * @param orig                the orig
     * @param finalParsingContext the finalParsingContext
     * @return the string
     */
    public static String parseExpressionContext(String orig, boolean finalParsingContext) {
        if (orig == null || orig.isEmpty()) {
            return null;
        }
        if (finalParsingContext) {
            orig = orig.replaceAll("(?<!\\\")\\\"(?!\\\")", "\"\"");
            orig = StringJsonParser.parseInput(orig, true);
        }
        return orig;
    }

    /**
     * Check values boolean.
     *
     * @param value the value
     * @param json  the json
     * @return the boolean
     */
    public static boolean checkValues(@NotNull JsonElement value, @NotNull JsonElement json) {
        boolean found = false;
        JsonElement jsonElement;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(json);

        while ((jsonElement = elements.pollFirst()) != null) {
            if (found) return true;
            if (jsonElement instanceof JsonArray) {
                for (JsonElement l : jsonElement.getAsJsonArray()) {
                    if (l.equals(value)) found = true;
                    elements.offerLast(l);
                }
            } else if (jsonElement instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                    if (entry.getValue().equals(value)) found = true;
                    if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                }
            }
        }
        return found;
    }

    /**
     * Check keys boolean.
     *
     * @param key  the key
     * @param json the json
     * @return the boolean
     */
    public static boolean checkKeys(@NotNull String key, @NotNull JsonElement json) {
        boolean found = false;
        JsonElement value;
        Deque<JsonElement> elements = new ConcurrentLinkedDeque<>();
        elements.add(json);

        while ((value = elements.pollFirst()) != null) {
            if (found) return true;
            if (value instanceof JsonArray) {
                for (JsonElement l : value.getAsJsonArray()) elements.offerLast(l);
            } else if (value instanceof JsonObject) {
                for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                    if (entry.getKey().equals(key)) found = true;
                    if (!entry.getValue().isJsonPrimitive()) elements.offerLast(entry.getValue());
                }
            }
        }
        return found;
    }

    /**
     * Json to type t.
     *
     * @param <T>  the type parameter
     * @param json the json
     * @return the t
     */
    public static <T> T jsonToType(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        if (json.isJsonArray() || json.isJsonObject()) return (T) json;
        else if (json.isJsonPrimitive()) {
            return (T) GsonConverter.fromJson(json.getAsJsonPrimitive(), Object.class);
        }
        return null;
    }

    /**
     * Is classic type boolean.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the boolean
     */
    public static <T> boolean isClassicType(T type) {
        Class<?> c = type.getClass();
        return
                (c.isAssignableFrom(String.class) ||
                        c.isAssignableFrom(Number.class) ||
                        c.isAssignableFrom(Boolean.class) || type instanceof Number
                        || c.isAssignableFrom(Integer.class)
                        || c.isAssignableFrom(Long.class));
    }

    /**
     * Default converter json element.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the json element
     */
    public static <T> JsonElement defaultConverter(T object) {
        try {
            if (object == null) return null;
            Class<?> clazz = object.getClass();
            if (clazz.equals(String.class)) {
                try {
                    return JsonParser.parseString((String) object);
                } catch (Exception e) {
                    return GsonConverter.toJsonTree(object, object.getClass());
                }
            }
            if (clazz.equals(Integer.class) || clazz.equals(LazilyParsedNumber.class)) {
                if (clazz.equals(LazilyParsedNumber.class)) {
                    return new JsonPrimitive(((LazilyParsedNumber) object).intValue());
                } else {
                    return new JsonPrimitive((Integer) object);
                }
            }
            if (clazz.equals(Boolean.class))
                return new JsonPrimitive((Boolean) object);
            if (clazz.equals(Double.class) || clazz.equals(Float.class))
                return new JsonPrimitive(((Number) object).doubleValue());
            if (clazz.equals(Long.class))
                return new JsonPrimitive((Long) object);
            if (clazz.equals(Byte.class))
                return new JsonPrimitive((Byte) object);
            if (clazz.equals(Short.class))
                return new JsonPrimitive((Short) object);
            if (clazz.equals(Character.class))
                return new JsonPrimitive((Character) object);
            if (object instanceof JsonElement)
                return (JsonElement) object;
            return null;
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    /**
     * Parse json element.
     *
     * @param <T> the type parameter
     * @param o   the o
     * @return the json element
     */
    public static <T> JsonElement parse(T o) {
        if (o == null || o instanceof JsonNull) return null;
        return parse(o, o.getClass());
    }

    /**
     * Parse json element.
     *
     * @param <T>       the type parameter
     * @param o         the o
     * @param canBeJson the can be json
     * @return the json element
     */
    public static <T> JsonElement parse(T o, boolean canBeJson) {
        if (canBeJson) {
            if (o == null || o instanceof JsonNull) return null;
            return parse(o, o.getClass());
        } else {
            if (o == null) return null;
            return parse(o, o.getClass());
        }
    }

    private static <T> JsonElement parse(T o, Class<?> clazz) {
        if (o == null) return null;
        if (o instanceof JsonElement json) return json;
        if (isClassicType(o)) return defaultConverter(o);

        final boolean isItem = o instanceof ItemStack || o instanceof Slot || o instanceof ItemType || o instanceof ItemData;

        if (isItem) {
            try {
                if (clazz.equals(ItemType.class) || ItemType.class.isAssignableFrom(clazz)) {
                    final ItemType item = (ItemType) o;
                    return ItemStackConverter.toJson(item.getRandom());
                } else if (clazz.equals(ItemStack.class) || ItemStack.class.isAssignableFrom(clazz)) {
                    final ItemStack stack = (ItemStack) o;
                    return ItemStackConverter.toJson(stack);
                } else if (clazz.equals(Slot.class) || Slot.class.isAssignableFrom(clazz)) {
                    final Slot slot = (Slot) o;
                    return ItemStackConverter.toJson(slot.getItem());
                } else if (clazz.equals(ItemData.class) || ItemData.class.isAssignableFrom(clazz)) {
                    final ItemData data = (ItemData) o;
                    return ItemStackConverter.toJson(data.getStack());
                }
            } catch (Exception ex) {
                if (PROJECT_DEBUG) error(ex);
            }
        } else {
            JsonElement e = assign(o);
            return e;
        }
        return null;
    }

    /**
     * Assign json element.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @return the json element
     */
    static <T> JsonElement assign(T object) {
        if (object == null) return JsonNull.INSTANCE;
        boolean isSerializable = (object instanceof YggdrasilSerializable || object instanceof ConfigurationSerializable);
        try {
            if (object instanceof World w) return WorldConverter.toJson(w);
            if (object instanceof Chunk c) ChunkConverter.toJson(c);
            if (object instanceof Block) {
                return BlockConverter.toJson((Block) object);
            }
            if (object instanceof ItemStack) {
                return ItemStackConverter.toJson((ItemStack) object);
            }
            if (object instanceof Inventory) {
                return InventoryConverter.toJson((Inventory) object);
            }
            if (object instanceof NBTContainer || object instanceof NBTCustom || object.getClass().getName().toString().contains("NBT")) {
                try {
                    return NBTContainerConverter.toJson((NBTContainer) object);
                } catch (Exception ex) {
                    try {
                        return NBTContainerConverter.toJson(new NBTContainer(object.toString()));
                    } catch (Exception ex_) {
                        error(ex);
                        error(ex_);
                    }
                }
            }
            if (isSerializable) {
                return GsonConverter.toJsonTree(object);
            }
        } catch (Exception exception) {
            if (PROJECT_DEBUG) exception.printStackTrace();
            return null;
        }
        try {
            return GsonConverter.toJsonTree(object);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * From t.
     *
     * @param <T>  the type parameter
     * @param json the json
     * @return the t
     */
    public static <T> T from(JsonElement json) {
        if (json == null || json.isJsonPrimitive() || json.isJsonArray() || json.isJsonNull()) return null;
        //if (json instanceof JsonElement) return ParserUtil.jsonToType(json);
        final JsonElement finalJson = json.deepCopy();
        Class<?> clazz = null;
        String potentialClass = null;
        if (json.getAsJsonObject().has(SERIALIZED_JSON_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get("..").getAsString();
        else if (json.getAsJsonObject().has(SERIALIZED_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get(SERIALIZED_TYPE_KEY).getAsString();
        try {
            if (potentialClass != null) clazz = Class.forName(potentialClass);
        } catch (ClassNotFoundException notFoundException) {
            if (PROJECT_DEBUG) error(notFoundException);
            return null;
        }

        if (clazz != null) {
            try {
                if (World.class.isAssignableFrom(clazz))
                    return (T) WorldConverter.fromJson(finalJson.getAsJsonObject());
                if (Chunk.class.isAssignableFrom(clazz))
                    return (T) ChunkConverter.fromJson(finalJson.getAsJsonObject());
                else if (ItemStack.class.isAssignableFrom(clazz))
                    return ((T) ItemStackConverter.fromJson(finalJson.getAsJsonObject()));
                else if (Inventory.class.isAssignableFrom(clazz))
                    return (T) InventoryConverter.fromJson(finalJson.getAsJsonObject());
                else if (Block.class.isAssignableFrom(clazz))
                    return (T) BlockConverter.fromJson(finalJson.getAsJsonObject());
                else if (NBTContainer.class.isAssignableFrom(clazz))
                    return (T) NBTContainerConverter.fromJson(finalJson.getAsJsonObject());
                else if (ConfigurationSerializable.class.isAssignableFrom(clazz))
                    return (T) GsonConverter.fromJson(finalJson, clazz);
                else {
                    T o = (T) GsonConverter.fromJson(finalJson, clazz);
                    if (o == null) return null;
                    else return o;
                }
            } catch (Exception ex) {
                if (PROJECT_DEBUG) error(ex);
                return null;
            }
        }
        return null;
    }
}
