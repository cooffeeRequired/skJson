package cz.coffeerequired.api.nbts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

/**
 * A facade class that ties together NBT -> JSON and JSON -> NBT operations.
 * It also includes an example method for merging JSON data into an ItemStack's NBT.
 */
@SuppressWarnings({"deprecation", "unused"})
public class NBTConverter {

    /**
     * Serializes the entire NBTCompound of an ItemStack to a JSON object
     * with "type" and "value" fields. (If you'd like to do so.)
     */
    public static JsonElement toJson(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        return NBTToJsonConverter.toJson(nbtItem);
    }

    /**
     * Serializes the entire NBTCompound (root) to a typed JSON structure.
     */
    public static JsonElement toJson(NBTContainer container) {
        // Reuse the same logic from NBTToJsonConverter
        return NBTToJsonConverter.toJson(container);
    }

    /**
     * Deserializes the given JSON object back to an NBTContainer.
     */
    public static NBTContainer fromJson(JsonObject json) {
        // Use the JsonToNBTConverter logic
        return JsonToNBTConverter.fromJson(json);
    }

    /**
     * Merges JSON data into an existing ItemStack's NBT. The JSON should be
     * in the typed format, e.g.:
     * {
     *   "type": "NBTTagCompound",
     *   "value": {
     *     "CustomName": {
     *       "type": "NBTTagString",
     *       "value": "MyCoolItem"
     *     },
     *     ...
     *   }
     * }
     */
    public static ItemStack parseFromJson(ItemStack item, JsonObject tags) {
        NBTContainer parsed = JsonToNBTConverter.fromJson(tags);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.mergeCompound(parsed);
        return nbtItem.getItem();
    }
}
