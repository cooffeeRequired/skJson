package cz.coffee.skriptgson.adapters;

import ch.njol.skript.doc.Since;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

import static cz.coffee.skriptgson.utils.GsonUtils.check;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

@Since("2.0.0")
public class SimpleAdapter {

    private final static String GSON_INVENTORY_TOKEN = "cz.coffee.gson.Inventory";

    public static Object toJson(Object object) {
        if (object != null) {
            boolean isSerializable = (object instanceof YggdrasilSerializable || object instanceof ConfigurationSerializable);
            if (isSerializable)
                return hierarchyAdapter().toJsonTree(object);
            else {
                if (object instanceof World) {
                    JsonObject object1 = new JsonObject();
                    object1.addProperty("world", ((World) object).getName());
                    return object1;
                } else if (object instanceof Inventory inventory) {
                    String data = ConvertInventories.toBase64(inventory);
                    ItemStack[] inv = new ItemStack[0];
                    try {
                        inv = ConvertInventories.itemStackArrayFromBase64(data);
                    } catch (IOException exception) {
                        SkriptGson.warning(exception.getMessage());
                    }
                    JsonObject object0 = new JsonObject();
                    JsonArray object1 = new JsonArray();
                    object0.addProperty(SERIALIZED_TYPE_KEY, GSON_INVENTORY_TOKEN);
                    for (ItemStack item : inv) {
                        object1.add(hierarchyAdapter().toJsonTree(item));
                    }
                    object0.add("Inventory", object1);
                    return object0;
                } else
                    return object;
            }
        }
        return null;
    }

    public static Object fromJson(JsonElement json) {
        if (check(json, SERIALIZED_TYPE_KEY, GsonUtils.Type.KEY)) {
            if (check(json, GSON_INVENTORY_TOKEN, GsonUtils.Type.VALUE)) {
                JsonArray unparsedJsonObjects = json.getAsJsonObject().get("Inventory").getAsJsonArray();
                ItemStack[] finalInventory = new ItemStack[54];
                Inventory inv = Bukkit.createInventory(null, 54, "JsonParsed  Inventory");
                int i = 0;
                for (JsonElement v : unparsedJsonObjects) {
                    ItemStack item = (ItemStack) hierarchyAdapter().fromJson(v, ConfigurationSerializable.class);
                    finalInventory[i] = item;
                    i++;
                }
                inv.setContents(finalInventory);
                return inv;
            }
        }

        try {
            return hierarchyAdapter().fromJson(json, ConfigurationSerializable.class);
        } catch (Exception exception) {
            SkriptGson.warning("you this is not a skript-type");
            return null;
        }
    }
}
