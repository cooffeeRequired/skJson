package cz.coffee.skriptgson.adapters;

import ch.njol.skript.doc.Since;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.Objects;

import static cz.coffee.skriptgson.utils.GsonUtils.check;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

@SuppressWarnings("deprecation")
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
                    /*
                    Meta of Inventory, Size,Type,Holder
                     */
                    JsonObject meta = new JsonObject();


                    meta.addProperty("INVENTORY_TYPE", inventory.getType().name());
                    meta.add("INVENTORY_HOLDER", new JsonObject());
                    meta.get("INVENTORY_HOLDER").getAsJsonObject().add(SERIALIZED_TYPE_KEY, new JsonPrimitive("org.bukkit.inventory"));

                    if (inventory.getHolder() instanceof Player) {
                        meta.get("INVENTORY_HOLDER").getAsJsonObject().add("InventoryHolder", hierarchyAdapter().toJsonTree(inventory.getHolder()));
                    } else {
                        meta.get("INVENTORY_HOLDER").getAsJsonObject().add("InventoryHolder", null);
                    }
                    meta.add("INVENTORY_SIZE", hierarchyAdapter().toJsonTree(inventory.getSize()));
                    JsonObject inventoryObject = new JsonObject();
                    JsonArray inventoryContents = new JsonArray();

                    inventoryObject.addProperty(SERIALIZED_TYPE_KEY, GSON_INVENTORY_TOKEN);
                    for (ItemStack item : inventory.getContents()) {
                        JsonElement jsonItem = hierarchyAdapter().toJsonTree(item);
                        inventoryContents.add(jsonItem);
                    }
                    inventoryObject.add("INVENTORY_META", meta);
                    inventoryObject.add("contents", inventoryContents);
                    return inventoryObject;
                } else
                    return object;
            }
        }
        return null;
    }

    public static Object fromJson(JsonElement json) {
        if (check(json, GSON_INVENTORY_TOKEN, GsonUtils.Type.VALUE)) {
            /*
            Getting all values from json and serialize it to new InventoriesObject
             */
            JsonObject inventoryMeta = json.getAsJsonObject().get("INVENTORY_META").getAsJsonObject();
            JsonObject jsonInvHolderObject = inventoryMeta.getAsJsonObject("INVENTORY_HOLDER");

            String jsonInvType = inventoryMeta.get("INVENTORY_TYPE").getAsString();
            Player invHolder = null;
            if (Objects.equals(jsonInvType, "PLAYER")) {
                invHolder = Bukkit.getPlayer(jsonInvHolderObject.getAsJsonObject("InventoryHolder").get("name").getAsString());
            }
            int jsonInvSize = inventoryMeta.get("INVENTORY_SIZE").getAsInt();

            InventoryType invType = InventoryType.valueOf(jsonInvType);

            ItemStack[] finalInventory = new ItemStack[jsonInvSize];
            Inventory inv = Bukkit.createInventory(invHolder, invType, "gson inventory of " + (invHolder == null ? jsonInvType : invHolder));

            int i = 0;
            Enchantment enchantmentValue;

            for (JsonElement v : json.getAsJsonObject().get("contents").getAsJsonArray()) {
                ItemStack item = (ItemStack) hierarchyAdapter().fromJson(v, ConfigurationSerializable.class);
                if (!(v instanceof JsonNull)) {
                    if (v.getAsJsonObject().has("meta")) {
                        JsonObject jsonMeta = v.getAsJsonObject().getAsJsonObject("meta");
                        if (jsonMeta.has("enchants")) {
                            for (Map.Entry<String, JsonElement> map : jsonMeta.get("enchants").getAsJsonObject().entrySet()) {
                                int enchantmentNumber = (map.getValue().getAsInt());
                                String enchamentString = map.getKey();
                                enchantmentValue = Enchantment.getByName(enchamentString);
                                if (enchantmentValue != null) {
                                    item.addUnsafeEnchantment(enchantmentValue, enchantmentNumber);
                                }
                            }
                        }

                        if (jsonMeta.has("attribute-modifiers")) {
                            ItemMeta m = item.getItemMeta();
                            for (Map.Entry<String, JsonElement> map : jsonMeta.getAsJsonObject("attribute-modifiers").entrySet()) {
                                Attribute attribute = Attribute.valueOf(map.getKey());
                                for (JsonElement element : map.getValue().getAsJsonArray()) {
                                    AttributeModifier attributeModifier = hierarchyAdapter().fromJson(element, AttributeModifier.class);
                                    m.addAttributeModifier(attribute, attributeModifier);
                                    item.setItemMeta(m);
                                }
                            }
                        }
                    }
                }
                finalInventory[i] = item;
                i++;
            }
            inv.setContents(finalInventory);
            return inv;
        }

        try {
            return hierarchyAdapter().fromJson(json, ConfigurationSerializable.class);
        } catch (Exception exception) {
            SkriptGson.warning("you this is not a skript-type");
            return null;
        }
    }
}
