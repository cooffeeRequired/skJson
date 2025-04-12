package cz.coffeerequired.api.nbts;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;

/**
 * Converts an NBTCompound into a plain JSON representation
 * WITHOUT storing any "type" metadata
 * Example output:
 * {
 * "Air": 300,
 * "Rotation": [ 56.827896, -40.0 ],
 * "CustomName": "MojeSuperZombie",
 * "IsBaby": false,
 * "ArmorDropChances": [0.085, 0.085, 0.085, 0.085],
 * "HandItems": [
 * {},
 * {}
 * ],
 * ...
 * }
 */
public class NBTToJsonConverter {

    /**
     * Converts an entire NBTCompound to a pure JSON element (likely a JsonObject).
     */
    public static JsonElement toJson(NBTCompound compound) {
        if (compound == null) {
            return JsonNull.INSTANCE;
        }
        // We'll build up a JSON object
        JsonObject obj = new JsonObject();
        for (String key : compound.getKeys()) {
            try {
                obj.add(key, parseKey(key, compound));
            } catch (Exception ex) {
                SkJson.exception(ex, "Failed to parse NBT key: " + key);
                obj.add(key, JsonNull.INSTANCE);
            }
        }
        return obj;
    }

    /**
     * Parses a single key from the NBT and returns a plain JSON element (primitive, array, or object).
     */
    private static JsonElement parseKey(String key, NBTCompound cmp) {
        NBTType type = cmp.getType(key);
        if (type == null) {
            return JsonNull.INSTANCE;
        }

        return switch (type) {
            case NBTTagByte ->
                // Byte might be used as a boolean (0/1) or a numeric byte
                    parseByte(cmp.getByte(key));
            case NBTTagShort -> new JsonPrimitive(cmp.getShort(key));
            case NBTTagInt -> new JsonPrimitive(cmp.getInteger(key));
            case NBTTagLong -> new JsonPrimitive(cmp.getLong(key));
            case NBTTagFloat -> new JsonPrimitive(cmp.getFloat(key));
            case NBTTagDouble -> new JsonPrimitive(cmp.getDouble(key));
            case NBTTagString -> new JsonPrimitive(cmp.getString(key));
            case NBTTagList -> parseList(key, cmp);
            case NBTTagCompound -> parseCompound(key, cmp);
            case NBTTagByteArray -> parseByteArray(cmp.getByteArray(key));
            case NBTTagIntArray -> parseIntArray(cmp.getIntArray(key));
            default -> JsonNull.INSTANCE;
        };
    }

    /**
     * Converts a byte to a JSON boolean if it's 0 or 1, otherwise returns a numeric byte.
     */
    private static JsonElement parseByte(Byte value) {
        if (value == 0) return new JsonPrimitive(false);
        if (value == 1) return new JsonPrimitive(true);
        return new JsonPrimitive(value);
    }

    /**
     * Parses an NBT list and returns a plain JSON array (numbers, strings, or nested objects).
     */
    private static JsonElement parseList(String key, NBTCompound cmp) {
        NBTCustom customType = NBTCustom.parseList(cmp, key);
        if (customType == null) {
            return JsonNull.INSTANCE;
        }

        JsonArray array = new JsonArray();
        switch (customType) {
            case NBTTagLongList -> cmp.getLongList(key).forEach(array::add);
            case NBTTagDoubleList -> cmp.getDoubleList(key).forEach(array::add);
            case NBTTagFloatList -> cmp.getFloatList(key).forEach(array::add);
            case NBTTagIntList -> cmp.getIntegerList(key).forEach(array::add);
            case NBTTagStringList -> cmp.getStringList(key).forEach(array::add);
            case NBTTagCompoundList -> {
                for (ReadWriteNBT nested : cmp.getCompoundList(key)) {
                    // Convert each nested compound to an object
                    if (nested instanceof NBTCompound nestedCmp) {
                        array.add(toJson(nestedCmp));
                    } else {
                        array.add(new JsonObject());
                    }
                }
            }
            default -> {
            }
        }
        return array;
    }

    /**
     * Parses an NBTCompound (nested) and returns a plain JSON object.
     */
    private static JsonElement parseCompound(String key, NBTCompound parent) {
        NBTCompound inner = parent.getCompound(key);
        if (inner == null) {
            return JsonNull.INSTANCE;
        }
        return toJson(inner);
    }

    /**
     * Converts a byte[] array to a JSON array of numbers.
     */
    private static JsonElement parseByteArray(byte[] data) {
        if (data == null) {
            return JsonNull.INSTANCE;
        }
        JsonArray arr = new JsonArray();
        for (byte b : data) {
            arr.add(b);
        }
        return arr;
    }

    /**
     * Converts an int[] array to a JSON array of numbers.
     */
    private static JsonElement parseIntArray(int[] data) {
        if (data == null) {
            return JsonNull.INSTANCE;
        }
        JsonArray arr = new JsonArray();
        for (int i : data) {
            arr.add(i);
        }
        return arr;
    }
}
