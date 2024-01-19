package cz.coffee.skjson.adapters;

import cz.coffee.skjson.utils.Logger;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
@SerializableAs("Inventory")
public class InventoryAdapter implements ConfigurationSerializable {
    @Override
    public @NotNull Map<String, Object> serialize() {
        Logger.info("here?");
        return Map.of();
    }

    @SuppressWarnings("unused")
    public static Inventory deserialize(Map<String, Object> args) {
        return null;
    }
}
