package cz.coffeerequired.api.adapters;

import com.google.gson.*;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class NBTFallBackItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        ReadWriteNBT nbtContainer = NBT.itemStackToNBT(src);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", src.getType().toString());
        jsonObject.addProperty("amount", src.getAmount());
        jsonObject.addProperty("nbt", nbtContainer.toString());
        return jsonObject;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        var pNBT = jsonObject.get("nbt");
        var pAmount = jsonObject.get("amount");
        var pType = jsonObject.get("type");

        ItemStack it;

        if (pNBT != null && !pNBT.isJsonNull()) {
            ReadWriteNBT nbtContainer = NBT.parseNBT(pNBT.getAsString());
            it = NBT.itemStackFromNBT(nbtContainer);
        } else {
            it = ItemStack.of(Material.valueOf(pType.getAsString()));
            if (!pType.isJsonNull()) it = it.withType(Material.valueOf(pType.getAsString()));
        }

        assert it != null;
        if (pAmount != null && !pAmount.isJsonNull()) it.setAmount(pAmount.getAsInt());
        if (!pType.isJsonNull()) it = it.withType(Material.valueOf(pType.getAsString()));
        return it;
    }
}
