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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.adapters.JsonAdapter;
import cz.coffee.utils.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static cz.coffee.adapters.generic.JsonInventory.*;
import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.json.JsonUtils.check;


public class JsonItemStack implements JsonGenericAdapter<ItemStack> {

    private final ItemStack itemStack;

    public JsonItemStack(ItemStack i) {
        this.itemStack = i;
    }

    /**
     * This function will check if the enchants or Attributes in the Json ItemStack are not stacked, if so it is added to the deserialized ItemStack.
     *
     * @param element element = is a serialized ItemStack to Json.
     */


    @SuppressWarnings("deprecation")
    public void setOthers(JsonElement element) {
        if (itemStack != null) {
            if (element.getAsJsonObject().has(CONTENTS_KEY_META)) {
                boolean hasEnchants = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).has(CONTENTS_KEY_ENCHANTS);
                boolean hasModifiers = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).has(CONTENTS_KEY_MODIFIERS);

                if (hasEnchants) {
                    JsonObject jsonEnchantments = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).getAsJsonObject(CONTENTS_KEY_ENCHANTS);

                    for (Map.Entry<String, JsonElement> mapOfEnchantments : jsonEnchantments.entrySet()) {
                        int enchantmentPower = mapOfEnchantments.getValue().getAsInt();
                        String enchantmentName = mapOfEnchantments.getKey();

                        //noinspection deprecation
                        Enchantment enchantment = Enchantment.getByName(enchantmentName.toUpperCase());
                        if (enchantment != null) {
                            itemStack.addUnsafeEnchantment(enchantment, enchantmentPower);
                        }
                    }
                }
                if (hasModifiers) {
                    JsonObject jsonModifiers = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).getAsJsonObject(CONTENTS_KEY_MODIFIERS);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    for (Map.Entry<String, JsonElement> mapOfModifiers : jsonModifiers.entrySet()) {
                        Attribute attribute = Attribute.valueOf(mapOfModifiers.getKey().toUpperCase());
                        for (JsonElement modifier : mapOfModifiers.getValue().getAsJsonArray()) {
                            AttributeModifier attributeModifier = gsonAdapter.fromJson(modifier, AttributeModifier.class);
                            itemMeta.addAttributeModifier(attribute, attributeModifier);
                            itemStack.setItemMeta(itemMeta);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get ItemStack from JsonItemStack
     *
     * @return itemStack.
     */

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public @NotNull JsonElement toJson(ItemStack object) {
        return gsonAdapter.toJsonTree(JsonAdapter.toJson(object));
    }

    @Override
    public ItemStack fromJson(JsonElement json) {
        return (ItemStack) JsonAdapter.fromJson(json);
    }

    @Override
    public Class<? extends ItemStack> typeOf(JsonElement json) {
        if (check(json, ItemStack.class.getSimpleName(), Type.KEY)) {
            return ItemStack.class;
        }
        return null;
    }
}
