package json;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.BukkitSerializableAdapter;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.api.json.NBTFallBackItemStackAdapter;
import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.TestOnly;

import java.util.Arrays;
import java.util.Random;

import static cz.coffeerequired.SkJson.logger;

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

        logger().info("Serialized Items:");

        String jsonSword = gson.toJson(enchantedSword);
        logger().info("Enchanted Sword with Custom NBT:");
        logger().info(jsonSword);

        String jsonApple = gson.toJson(goldenApple);
        logger().info("Golden Apple:");
        logger().info(jsonApple);

        String jsonBook = gson.toJson(enchantedBook);
        logger().info("Enchanted Book with Custom NBT:");
        logger().info(jsonBook);

        logger().info("\nDeserialized Items:");

        ItemStack deserializedSword = gson.fromJson(jsonSword, ItemStack.class);
        logger().info("Deserialized Enchanted Sword: " + deserializedSword.getType() + " x " + deserializedSword.getAmount());

        ItemStack deserializedApple = gson.fromJson(jsonApple, ItemStack.class);
        logger().info("Deserialized Golden Apple: " + deserializedApple.getType() + " x " + deserializedApple.getAmount());

        ItemStack deserializedBook = gson.fromJson(jsonBook, ItemStack.class);
        logger().info("Deserialized Enchanted Book: " + deserializedBook.getType() + " x " + deserializedBook.getAmount());


        World w = Bukkit.getWorld("world_nether");

        Location location = new Location(w, 0, 0, 0);

        var t = GsonParser.toJson(location);
        logger().info(t.toString());
        var t2 = GsonParser.fromJson(t, Location.class);
        logger().info(t2.toString());

        var w1 = GsonParser.toJson(w);
        logger().info(w1.toString());
        var w2 = GsonParser.fromJson(w1, World.class);
        logger().info(w2.toString());


        var ch1 = GsonParser.toJson(w.getChunkAt(0, 0));
        logger().info(ch1.toString());
        var ch2 = GsonParser.fromJson(ch1, Chunk.class);
        logger().info(ch2.toString());

        ItemStack axolotlItem = new ItemStack(Material.AXOLOTL_BUCKET);
        ItemMeta meta = axolotlItem.getItemMeta();
        if (meta != null) {
            Component name = Component.text("Legendary Blue Axolotl").color(TextColor.color(0x1E90FF));
            meta.displayName(name);
            axolotlItem.setItemMeta(meta);
        }

        logger().info(axolotlItem.toString());
        var i1 = GsonParser.toJson(axolotlItem);
        logger().info(i1.toString());
        var i2 = GsonParser.fromJson(i1, ItemStack.class);
        logger().info(i2.toString());


        Location location2 = new Location(w, 0, 0, 30);
        Block block = w.getBlockAt(location2);
        block.setType(Material.DIAMOND_BLOCK);

        logger().info(block.toString());
        var b1 = GsonParser.toJson(block);
        logger().info(b1.toString());
        var b2 = GsonParser.fromJson(b1, Block.class);
        logger().info(b2.toString());


        Inventory inventory = Bukkit.createInventory(null, 9, Component.text("KOKOT"));
        Random random = new Random();
        Material[] materials = Material.values();

        for (int i = 0; i < 9; i++) {
            Material randomMaterial;
            do {
                randomMaterial = materials[random.nextInt(materials.length)];
            } while (!randomMaterial.isItem());

            ItemStack randomItem = new ItemStack(randomMaterial);
            inventory.setItem(i, randomItem);
        }

        logger().info(inventory.toString());
        var inv1 = GsonParser.toJson(inventory);
        logger().info(inv1.toString());
        var inv2 = GsonParser.fromJson(inv1, Inventory.class);
        logger().info(inv2.toString());
    }
}