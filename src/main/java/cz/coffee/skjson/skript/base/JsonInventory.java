package cz.coffee.skjson.skript.base;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: neděle (16.07.2023)
 */
public record JsonInventory(HumanEntity player, Inventory inv) {
    public static Inventory newInventory(String holderName, InventoryType type, String title) {
        Player p = null;
        try {
            p = Bukkit.getPlayer(holderName);
        } catch (Exception ignored) {
        }

        return Bukkit.createInventory(p, type, Component.text(title));
    }
}
