/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.adapters.generic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.adapters.JsonAdapter;
import cz.coffee.utils.SimpleUtil;
import cz.coffee.utils.Type;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static cz.coffee.utils.json.JsonUtils.check;
import static org.bukkit.Bukkit.createInventory;

public class JsonInventory implements JsonGenericAdapter<Inventory> {

    final static String CONTENTS_KEY_META = "meta";
    final static String CONTENTS_KEY_ENCHANTS = "enchants";
    final static String CONTENTS_KEY_MODIFIERS = "attribute-modifiers";
    private final static String INVENTORY_META_KEY = "__inventory-meta__";

    @Override
    public @NotNull JsonElement toJson(Inventory inv) {
        /*
         Meta of Inventory, Size,Type,Holder
         */
        final JsonObject inventoryMeta = new JsonObject();
        final JsonObject deserializedInventory = new JsonObject();
        final JsonArray deserializedItems = new JsonArray();
        inventoryMeta.addProperty("inventory-type", inv.getType().name());
        String invTitle = "Unknown";
        inventoryMeta.add("inventory-holder", new JsonObject());

        // defined inventory-holder in the inventory Meta
        final JsonObject inventoryHolder = inventoryMeta.getAsJsonObject("inventory-holder");

        InventoryHolder holder = null;
        InventoryHolder nonClassifiedHolder = inv.getHolder(true);
        if (nonClassifiedHolder instanceof Player) {
            holder = nonClassifiedHolder;
            invTitle = "Inventory of " + ((Player) nonClassifiedHolder).getName();
        }
        inventoryMeta.addProperty("inventory-title", invTitle);

        inventoryHolder.addProperty("Type", holder != null ? holder.getClass().getSimpleName() : null);
        inventoryHolder.add("holder", SimpleUtil.gsonAdapter.toJsonTree(holder));

        inventoryMeta.addProperty("inventory-size", inv.getSize());

        deserializedInventory.addProperty(GSON_GENERIC_ADAPTER_KEY, inv.getClass().getName());
        for (ItemStack item : inv.getContents()) {
            deserializedItems.add(SimpleUtil.gsonAdapter.toJsonTree(item));
        }

        deserializedInventory.add(INVENTORY_META_KEY, inventoryMeta);
        deserializedInventory.add("contents", deserializedItems);

        return deserializedInventory;
    }

    @Override
    public @NotNull Inventory fromJson(JsonElement json) {
        // contents
        final JsonArray inventoryContents = json.getAsJsonObject().getAsJsonArray("contents");
        final JsonObject inventoryMeta = json.getAsJsonObject().getAsJsonObject(INVENTORY_META_KEY);

        final InventoryType inventoryType = InventoryType.valueOf(inventoryMeta.get("inventory-type").getAsString());
        final Component inventoryTitle = Component.text(inventoryMeta.get("inventory-title").getAsString());

        final InventoryHolder inventoryHolder = inventoryType == InventoryType.PLAYER ? Bukkit.getPlayer(inventoryMeta.getAsJsonObject("inventory-holder").getAsJsonObject("holder").get("name").getAsString()) : null;

        final int inventorySize = inventoryMeta.get("inventory-size").getAsInt();

        final List<ItemStack> listOfItems = new ArrayList<>();


        final Inventory inventory = inventoryType == InventoryType.PLAYER ? createInventory(
                inventoryHolder,
                inventoryType,
                inventoryTitle
        ) : createInventory(
                null,
                inventorySize,
                inventoryTitle
        );

        for (JsonElement itemFromJson : inventoryContents) {
            ItemStack item = (ItemStack) JsonAdapter.fromJson(itemFromJson);
            listOfItems.add(item);
        }
        inventory.setContents(listOfItems.toArray(new ItemStack[0]));
        return inventory;
    }

    @Override
    public Class<Inventory> typeOf(JsonElement json) {
        if (check(json, Inventory.class.getSimpleName(), Type.KEY)) {
            return Inventory.class;
        }
        return null;
    }
}
