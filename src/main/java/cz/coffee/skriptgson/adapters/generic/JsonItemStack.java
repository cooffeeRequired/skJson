package cz.coffee.skriptgson.adapters.generic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.adapters.Adapters;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static cz.coffee.skriptgson.SkriptGson.gsonAdapter;
import static cz.coffee.skriptgson.adapters.generic.JsonInventory.*;
import static cz.coffee.skriptgson.utils.GsonUtils.check;

public class JsonItemStack implements JsonGenericAdapter<ItemStack> {

    private final ItemStack itemStack;

    public JsonItemStack(ItemStack i) {
        this.itemStack = i;
    }

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

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public @NotNull JsonElement toJson(ItemStack object) {
        return gsonAdapter.toJsonTree(Adapters.toJson(object));
    }

    @Override
    public ItemStack fromJson(JsonElement json) {
        return (ItemStack) Adapters.fromJson(json);
    }

    @Override
    public Class<? extends ItemStack> typeOf(JsonElement json) {
        if (check(json, ItemStack.class.getSimpleName(), GsonUtils.Type.KEY)) {
            return ItemStack.class;
        }
        return null;
    }
}
