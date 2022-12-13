package cz.coffee.skriptgson.adapters;

import ch.njol.skript.doc.Since;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Since("2.0.0")
public class SimpleAdapter {
    public static Object adapter(Object object) {
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
                    JsonArray object1 = new JsonArray();
                    for (ItemStack item : inv) {
                        object1.add(hierarchyAdapter().toJsonTree(item));
                    }
                    return object1;
                } else
                    return object;
            }
        }
        return null;
    }
}
