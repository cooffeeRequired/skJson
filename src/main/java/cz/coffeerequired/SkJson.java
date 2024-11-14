package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.CustomLogger;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.support.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Slf4j
public final class SkJson extends JavaPlugin {
    @Getter
    static SkJson instance;
    @Getter
    static Configuration configuration;

    static CustomLogger logger;
    private final Register register = new Register();

    public static @NotNull CustomLogger logger() {
        return logger;
    }

    @Override
    public void onLoad() {

        instance = this;
        logger = new CustomLogger(this.getName());

        Configuration.applyScheduledUpdate();

        configuration = new Configuration(this);
        configuration.checkForUpdate();

        setupMetrics(17374, this);
        logger.info("bStats metrics enabled.");

        // TODO could add JSON watcher registration here

    }

    @Override
    public void onEnable() {
        logger.info("Enabling...");
        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);
        }

        World w = Bukkit.getWorld("world_nether");

        Location location = new Location(w, 0, 0, 0);

        var t = GsonParser.toJson(location);
        logger.info(t.toString());
        var t2 = GsonParser.fromJson(t, Location.class);
        logger.info(t2.toString());

        var w1 = GsonParser.toJson(w);
        logger.info(w1.toString());
        var w2 = GsonParser.fromJson(w1, World.class);
        logger.info(w2.toString());


        var ch1 = GsonParser.toJson(w.getChunkAt(0, 0));
        logger.info(ch1.toString());
        var ch2 = GsonParser.fromJson(ch1, Chunk.class);
        logger.info(ch2.toString());

        ItemStack axolotlItem = new ItemStack(Material.AXOLOTL_BUCKET);
        ItemMeta meta = axolotlItem.getItemMeta();
        if (meta != null) {
            // Nastavení názvu s barvou
            Component name = Component.text("Legendary Blue Axolotl").color(TextColor.color(0x1E90FF));
            meta.displayName(name);
            axolotlItem.setItemMeta(meta);
        }

        logger.info(axolotlItem.toString());
        var i1 = GsonParser.toJson(axolotlItem);
        logger.info(i1.toString());
        var i2 = GsonParser.fromJson(i1, ItemStack.class);
        logger.info(i2.toString());


        Location location2 = new Location(w, 0, 0, 30);
        Block block = w.getBlockAt(location2);
        block.setType(Material.DIAMOND_BLOCK);

        logger.info(block.toString());
        var b1 = GsonParser.toJson(block);
        logger.info(b1.toString());
        var b2 = GsonParser.fromJson(b1, Block.class);
        logger.info(b2.toString());


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

        logger.info(inventory.toString());
        var inv1 = GsonParser.toJson(inventory);
        logger.info(inv1.toString());
        var inv2 = GsonParser.fromJson(inv1, Inventory.class);
        logger.info(inv2.toString());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }


    @SuppressWarnings("UnstableApiUsage")
    public void setupMetrics(int id, JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, id);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));
        metrics.addCustomChart(new SimplePie("skjson_version", () -> plugin.getPluginMeta().getVersion()));
    }
}
