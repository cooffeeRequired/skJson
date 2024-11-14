package json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.coffeerequired.api.json.BukkitSerializableAdapter;
import cz.coffeerequired.api.json.NBTFallBackItemStackAdapter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.TestOnly;

@SuppressWarnings("all")
@TestOnly
public class Main {

    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new NBTFallBackItemStackAdapter())
            .registerTypeAdapter(BukkitSerializableAdapter.class, new BukkitSerializableAdapter())
            .setPrettyPrinting()
            .create();

    public static void main(String[] args) {
        ItemStack itemStack = new ItemStack(org.bukkit.Material.DIAMOND_SWORD, 1);

        String json = gson.toJson(itemStack);
        System.out.println("Serialized ItemStack:");
        System.out.println(json);

        ItemStack deserializedItemStack = gson.fromJson(json, ItemStack.class);
        System.out.println("Deserialized ItemStack:");
        System.out.println(deserializedItemStack.getType() + " x " + deserializedItemStack.getAmount());
    }
}
