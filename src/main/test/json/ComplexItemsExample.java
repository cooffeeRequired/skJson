package json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.BukkitSerializableAdapter;
import cz.coffeerequired.api.json.NBTFallBackItemStackAdapter;
import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.TestOnly;

import java.util.Arrays;

@SuppressWarnings("all")
@TestOnly
public class ComplexItemsExample {

    public static void test() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ItemStack.class, new NBTFallBackItemStackAdapter())
                .registerTypeAdapter(BukkitSerializableAdapter.class, new BukkitSerializableAdapter())
                .setPrettyPrinting()
                .create();

        ItemStack enchantedSword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta swordMeta = enchantedSword.getItemMeta();
        if (swordMeta != null) {
            swordMeta.displayName(Component.text("§6Legendary Diamond Sword"));
            swordMeta.addEnchant(Enchantment.UNBREAKING, 5, true);
            swordMeta.addEnchant(Enchantment.SHARPNESS, 3, true);
            swordMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            enchantedSword.setItemMeta(swordMeta);
        }

        NBT.modify(enchantedSword, nbt -> {
            nbt.setString("CustomNBT", "LegendaryWeapon");
            nbt.setInteger("DamageBoost", 25);
        });

        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 5);
        ItemMeta appleMeta = goldenApple.getItemMeta();
        if (appleMeta != null) {
            appleMeta.displayName(Component.text("§eMystic Golden Apple"));
            appleMeta.lore(Arrays.asList(Component.text("§7Grants special abilities"), Component.text("§7Use wisely!")));
            goldenApple.setItemMeta(appleMeta);
        }

        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
        ItemMeta bookMeta = enchantedBook.getItemMeta();
        if (bookMeta != null) {
            bookMeta.displayName(Component.text("§bEnchanted Tome of Knowledge"));
            bookMeta.addEnchant(Enchantment.FORTUNE, 3, true);
            bookMeta.lore(Arrays.asList(Component.text("§9Contains hidden secrets"), Component.text("§9Power level: 9000")));
            enchantedBook.setItemMeta(bookMeta);
        }

        NBT.modify(enchantedBook, nbt -> {
            nbt.setString("MagicLevel", "High");
            nbt.setInteger("KnowledgePoints", 50);
        });

        SkJson.logger().info("Serialized Items:");

        String jsonSword = gson.toJson(enchantedSword);
        SkJson.logger().info("Enchanted Sword with Custom NBT:");
        SkJson.logger().info(jsonSword);

        String jsonApple = gson.toJson(goldenApple);
        SkJson.logger().info("Golden Apple:");
        SkJson.logger().info(jsonApple);

        String jsonBook = gson.toJson(enchantedBook);
        SkJson.logger().info("Enchanted Book with Custom NBT:");
        SkJson.logger().info(jsonBook);

        SkJson.logger().info("\nDeserialized Items:");

        ItemStack deserializedSword = gson.fromJson(jsonSword, ItemStack.class);
        SkJson.logger().info("Deserialized Enchanted Sword: " + deserializedSword.getType() + " x " + deserializedSword.getAmount());

        ItemStack deserializedApple = gson.fromJson(jsonApple, ItemStack.class);
        SkJson.logger().info("Deserialized Golden Apple: " + deserializedApple.getType() + " x " + deserializedApple.getAmount());

        ItemStack deserializedBook = gson.fromJson(jsonBook, ItemStack.class);
        SkJson.logger().info("Deserialized Enchanted Book: " + deserializedBook.getType() + " x " + deserializedBook.getAmount());
    }
}