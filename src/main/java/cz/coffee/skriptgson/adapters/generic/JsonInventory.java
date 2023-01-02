package cz.coffee.skriptgson.adapters.generic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.adapters.Adapters;
import cz.coffee.skriptgson.utils.GsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static cz.coffee.skriptgson.SkriptGson.gsonAdapter;
import static cz.coffee.skriptgson.utils.GsonUtils.check;
import static org.bukkit.Bukkit.createInventory;

public class JsonInventory implements JsonGenericAdapter<Inventory> {

    private final static String INVENTORY_META_KEY = "__inventory-meta__";
    final static String CONTENTS_KEY_META = "meta";
    final static String CONTENTS_KEY_ENCHANTS = "enchants";
    final static String CONTENTS_KEY_MODIFIERS = "attribute-modifiers";


    @Override
    public @NotNull JsonElement toJson(Inventory inv) {
        /*
         Meta of Inventory, Size,Type,Holder
         */
        final JsonObject inventoryMeta = new JsonObject();
        final JsonObject deserializedInventory = new JsonObject();
        final JsonArray deserializedItems = new JsonArray();


        inventoryMeta.addProperty("inventory-type", inv.getType().name());
        inventoryMeta.addProperty("inventory-title", GsonComponentSerializer.gson().serialize(inv.getType().defaultTitle()));
        inventoryMeta.add("inventory-holder", new JsonObject());

        // defined inventory-holder in the inventory Meta
        final JsonObject inventoryHolder = inventoryMeta.getAsJsonObject("inventory-holder");

        InventoryHolder holder = null;
        InventoryHolder nonClassifiedHolder = inv.getHolder(true);
        if (nonClassifiedHolder instanceof Player) {
            holder = nonClassifiedHolder;
        }

        inventoryHolder.addProperty("Type", holder != null ? holder.getClass().getSimpleName() : null);
        inventoryHolder.add("holder", gsonAdapter.toJsonTree(holder));

        inventoryMeta.addProperty("inventory-size", inv.getSize());

        deserializedInventory.addProperty(GSON_GENERIC_ADAPTER_KEY, inv.getClass().getName());
        for (ItemStack item : inv.getContents()) {
            deserializedItems.add(gsonAdapter.toJsonTree(item));
        }

        deserializedInventory.add(INVENTORY_META_KEY, inventoryMeta);
        deserializedInventory.add("contents", deserializedItems);

        return deserializedInventory;
    }

    @Override
    public @NotNull Inventory fromJson(JsonElement json) {
        boolean hasModifiers, hasEnchants;
        Enchantment enchantment;

        // contents
        final JsonArray inventoryContents = json.getAsJsonObject().getAsJsonArray("contents");
        final JsonObject inventoryMeta = json.getAsJsonObject().getAsJsonObject(INVENTORY_META_KEY);

        final InventoryType inventoryType = InventoryType.valueOf(inventoryMeta.get("inventory-type").getAsString());
        final Component inventoryTitle = GsonComponentSerializer.gson().deserializeFromTree(inventoryMeta.get("inventory-title"));

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
            ItemStack item = (ItemStack) Adapters.fromJson(itemFromJson);
            listOfItems.add(item);
        }
        inventory.setContents(listOfItems.toArray(new ItemStack[0]));
        return inventory;
    }

    @Override
    public Class<Inventory> typeOf(JsonElement json) {
        if (check(json, Inventory.class.getSimpleName(), GsonUtils.Type.KEY)) {
            return Inventory.class;
        }
        return null;
    }
}
