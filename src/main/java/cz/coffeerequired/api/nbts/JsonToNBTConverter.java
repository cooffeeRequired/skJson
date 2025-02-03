package cz.coffeerequired.api.nbts;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTList;

import java.util.ArrayList;
import java.util.List;

/**
 * Example minimal converter from plain JSON back to NBT.
 * This is highly heuristic and may not always match original types.
 */
@SuppressWarnings("deprecation")
public class JsonToNBTConverter {

    /**
     * Converts a plain JSON element (expected to be an object) to NBTContainer.
     */
    public static NBTContainer fromJson(JsonElement element) {
        NBTContainer container = new NBTContainer();
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            fromObject(obj, container);
        }
        return container;
    }

    /**
     * Recursively processes a JSON object and sets values into the given NBTCompound.
     */
    private static void fromObject(JsonObject obj, NBTCompound target) {
        for (String key : obj.keySet()) {
            JsonElement child = obj.get(key);
            if (child.isJsonNull()) {
                // No action or remove the key if needed
                continue;
            }
            else if (child.isJsonObject()) {
                // It's a nested object -> treat as an NBTCompound
                target.addCompound(key);
                fromObject(child.getAsJsonObject(), target.getCompound(key));
            }
            else if (child.isJsonArray()) {
                // We treat it as an NBTList, but we must guess the type
                processJsonArray(key, child.getAsJsonArray(), target);
            }
            else if (child.isJsonPrimitive()) {
                // Could be boolean, int, float, etc.
                processPrimitive(key, child.getAsJsonPrimitive(), target);
            }
        }
    }

    /**
     * Converts a JSON array to the best guess of an NBT list (or specialized array).
     */
    private static void processJsonArray(String key, JsonArray arr, NBTCompound parent) {
        if (arr.isEmpty()) {
            // Just create an empty integer list by default, or store nothing
            parent.getIntegerList(key); // empty
            return;
        }

        // Peek the first element to guess type
        JsonElement first = arr.get(0);
        if (first.isJsonObject()) {
            // So it's presumably a list of compounds
            var compoundList = parent.getCompoundList(key);
            for (JsonElement e : arr) {
                if (!e.isJsonObject()) {
                    continue;
                }
                NBTContainer sub = fromJson(e.getAsJsonObject());
                compoundList.addCompound(sub);
            }
        }
        else if (first.isJsonPrimitive()) {
            // Could be numeric or string
            JsonPrimitive prim = first.getAsJsonPrimitive();
            if (prim.isNumber()) {
                // Let's see if they are all integers or all floats
                boolean allIntegers = true;
                List<Number> numbers = new ArrayList<>();
                for (JsonElement e : arr) {
                    if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) {
                        allIntegers = false;
                        break;
                    }
                    // Collect them
                    Number num = e.getAsNumber();
                    numbers.add(num);
                }
                if (allIntegers) {
                    // Decide if we can store them as an int array or byte array
                    // For simplicity, store everything in an int list
                    NBTList<Integer> intList = parent.getIntegerList(key);
                    for (Number n : numbers) {
                        intList.add(n.intValue());
                    }
                } else {
                    // We fallback to a double list
                    NBTList<Double> doubleList = parent.getDoubleList(key);
                    for (JsonElement e : arr) {
                        doubleList.add(e.getAsDouble());
                    }
                }
            }
            else if (prim.isString()) {
                // It's a list of strings
                NBTList<String> stringList = parent.getStringList(key);
                for (JsonElement e : arr) {
                    stringList.add(e.getAsString());
                }
            }
            else if (prim.isBoolean()) {
                // It's a list of booleans, store as a byte array for example
                byte[] booleans = new byte[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    boolean val = arr.get(i).getAsBoolean();
                    booleans[i] = (byte) (val ? 1 : 0);
                }
                parent.setByteArray(key, booleans);
            }
        }
        else if (first.isJsonNull()) {
            // Possibly do nothing or skip
        }
    }

    /**
     * Processes a primitive. We guess the correct type:
     * - Boolean => store as boolean
     * - Integral => store as int if it fits, otherwise long
     * - Decimal => store as double
     * - String => store as string
     */
    private static void processPrimitive(String key, JsonPrimitive prim, NBTCompound parent) {
        if (prim.isBoolean()) {
            parent.setBoolean(key, prim.getAsBoolean());
        }
        else if (prim.isNumber()) {
            Number num = prim.getAsNumber();
            // We'll see if it has a decimal point
            String asStr = prim.getAsString();
            if (asStr.contains(".") || asStr.contains("e") || asStr.contains("E")) {
                // treat as double
                parent.setDouble(key, num.doubleValue());
            } else {
                // treat as integer or long
                long l = num.longValue();
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    parent.setInteger(key, (int) l);
                } else {
                    parent.setLong(key, l);
                }
            }
        }
        else if (prim.isString()) {
            parent.setString(key, prim.getAsString());
        }
    }
}
