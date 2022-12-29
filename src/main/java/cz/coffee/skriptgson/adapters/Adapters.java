package cz.coffee.skriptgson.adapters;

import ch.njol.skript.doc.Since;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.skriptgson.adapters.generic.JsonGeneric;
import cz.coffee.skriptgson.adapters.generic.JsonInventory;
import cz.coffee.skriptgson.adapters.generic.JsonWorld;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;

import static cz.coffee.skriptgson.SkriptGson.gsonAdapter;


@Since("2.0.0")


public class Adapters {

    public static Object toJson(Object input) {
        if (input != null) {
            boolean isSerializable = (input instanceof YggdrasilSerializable || input instanceof ConfigurationSerializable);
            if (isSerializable)
                return gsonAdapter.toJsonTree(input);
            else {
                if (input instanceof World world) {
                    return new JsonWorld().toJson(world);
                } else if (input instanceof Inventory inventory) {
                    return new JsonInventory().toJson(inventory);
                } else
                    return input;
            }
        }
        return null;
    }

    public static Object fromJson(JsonElement json) {
        if (json instanceof JsonNull) return null;
        Class<?> clazz = new JsonGeneric().typeOf(json);

        if (Inventory.class.isAssignableFrom(clazz)) {
            return new JsonInventory().fromJson(json);
        } else if (World.class.isAssignableFrom(clazz)) {
            return new JsonWorld().fromJson(json);
        } else {
            return gsonAdapter.fromJson(json, ConfigurationSerializable.class);
        }
    }
}
