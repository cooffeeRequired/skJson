package cz.coffee.core.utils;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static cz.coffee.core.adapters.Adapter.SERIALIZED_JSON_TYPE_KEY;
import static cz.coffee.core.adapters.Adapters.*;
import static cz.coffee.core.utils.Util.GSON_ADAPTER;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

public class AdapterUtils {

    public static <T> JsonElement parseItem(T item, Class<?> clazz) {
        boolean isItem = item instanceof ItemType || item instanceof ItemStack || item instanceof Slot;
        if (item == null) return null;
        if (item instanceof JsonElement) {
            return (JsonElement) item;
        } else if (isClassic(item)) {
            return JsonUtils.convert(item);
        } else if (isItem) {
            if (clazz == ItemType.class) {
                return parseItem(item, null, null, ItemType.class);
            } else if (clazz == Slot.class) {
                return parseItem(item, null, null, Slot.class);
            } else {
                return parseItem(item, null, null);
            }
        }
        return assignTo(item);
    }

    public static <T> JsonElement parseItem(T item, Expression<?> expression, Event event, Class<?>... clazzInput) {
        boolean isDefined = (clazzInput.length > 0 && (clazzInput[0] == ItemType.class || clazzInput[0] == Slot.class));
        boolean isItem = item instanceof ItemType || item instanceof ItemStack || item instanceof Slot;
        if (item instanceof JsonElement) {
            return (JsonElement) item;
        }
        if (item == null) return null;
        if (isClassic(item))
            return JsonUtils.convert(item);
        Class<?> clazz = item.getClass();
        if (isItem) {
            if (!isDefined) {
                if (expression == null) return null;
                if (clazz == ItemType.class) {
                    return parseItemType(expression, event);
                } else if (clazz == ItemStack.class) {
                    return parseItemStack((ItemStack) item);
                } else if (item instanceof Slot) {
                    return parseSlot(expression, event);
                }
            } else {
                if (clazzInput[0].equals(ItemType.class)) {
                    assert item instanceof ItemType;
                    return ItemStackAdapter.toJson(((ItemType) item).getRandom());
                } else {
                    assert item instanceof Slot;
                    return ItemStackAdapter.toJson(((Slot) item).getItem());
                }
            }
        } else {
            return assignTo(item);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static JsonElement parseItemType(Expression<?> expression, Event event) {
        Expression<?> emptyExpression = expression.getConvertedExpression(ItemType.class);
        if (emptyExpression == null) {
            return null;
        }
        ItemType randomItemType = (ItemType) emptyExpression.getSingle(event);
        if (randomItemType == null) {
            return null;
        }
        ItemStack itemStack = randomItemType.getRandom();
        return ItemStackAdapter.toJson(itemStack);
    }

    private static JsonElement parseItemStack(ItemStack itemStack) {
        return ItemStackAdapter.toJson(itemStack);
    }

    @SuppressWarnings("unchecked")
    private static JsonElement parseSlot(Expression<?> expression, Event event) {
        Expression<?> emptyExpression = expression.getConvertedExpression(Slot.class);
        if (emptyExpression == null) {
            return null;
        }
        Slot filledSlot = (Slot) emptyExpression.getSingle(event);
        if (filledSlot == null) {
            return null;
        }
        ItemStack itemStack = filledSlot.getItem();
        return ItemStackAdapter.toJson(itemStack);
    }


    public static <T> boolean isClassic(T item) {
        return item instanceof String || item instanceof Number || item instanceof Boolean;
    }

    @SuppressWarnings("unchecked")
    public static <T> T assignFrom(JsonElement json) {
        if (json == null || json.isJsonArray() || json.isJsonNull() || json.isJsonPrimitive()) return null;
        Class<?> clazz = null;
        String potentialClass = null;
        if (json.getAsJsonObject().has(SERIALIZED_JSON_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get("..").getAsString();
        else if (json.getAsJsonObject().has(SERIALIZED_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get(SERIALIZED_TYPE_KEY).getAsString();
        try {
            if (potentialClass != null) {
                clazz = Class.forName(potentialClass);
            }
        } catch (ClassNotFoundException notFoundException) {
            notFoundException.printStackTrace();
        }
        if (clazz != null) {
            try {
                if (World.class.isAssignableFrom(clazz))
                    return (T) WorldAdapter.fromJson(json.getAsJsonObject());
                else if (Chunk.class.isAssignableFrom(clazz))
                    return (T) ChunkAdapter.fromJson(json.getAsJsonObject());
                else if (ItemStack.class.isAssignableFrom(clazz))
                    return ((T) ItemStackAdapter.fromJson(json.getAsJsonObject()));
                else if (Inventory.class.isAssignableFrom(clazz))
                    return (T) InventoryAdapter.fromJson(json.getAsJsonObject());
                else if (ConfigurationSerializable.class.isAssignableFrom(clazz))
                    return (T) GSON_ADAPTER.fromJson(json, clazz);
                else if (Block.class.isAssignableFrom(clazz))
                    return (T) BlockAdapter.fromJson(json.getAsJsonObject());
                else if (NBTContainer.class.isAssignableFrom(clazz))
                    return (T) NBTContainerAdapter.fromJson(json.getAsJsonObject());
                else return null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }


    static <T> JsonElement assignTo(T item) {
        if (item == null) return JsonNull.INSTANCE;
        boolean isSerializable = (item instanceof YggdrasilSerializable || item instanceof ConfigurationSerializable);

        try {
            if (item instanceof World) {
                return WorldAdapter.toJson((World) item);
            }
            if (item instanceof Chunk) {
                return ChunkAdapter.toJson((Chunk) item);
            }
            if (item instanceof Block) {
                return BlockAdapter.toJson((Block) item);
            }
            if (item instanceof ItemStack) {
                return ItemStackAdapter.toJson((ItemStack) item);
            }
            if (item instanceof Inventory) {
                return InventoryAdapter.toJson((Inventory) item);
            }
            if (item instanceof NBTContainer) {
                return NBTContainerAdapter.toJson((NBTContainer) item);
            }
            if (isSerializable) {
                return GSON_ADAPTER.toJsonTree(item, ConfigurationSerializable.class);
            }
        } catch (Exception exception) {
            return null;
        }
        return null;
    }
}
