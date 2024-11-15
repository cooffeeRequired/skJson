package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.Commands;
import cz.coffeerequired.api.CustomLogger;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.json.GsonParser;
import cz.coffeerequired.support.Configuration;
import de.tr7zw.changeme.nbtapi.NBT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


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

        try {
            Class.forName("cz.coffeerequired.api.json.JsonFileWatcher");
            logger.info("Json watchers found & enabled.");
        } catch (ClassNotFoundException e) {
            logger.info("Unable to find Json watchers.");
            logger.exception(e.getMessage(), e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        logger.info("Enabling...");

        var plMeta = this.getPluginMeta();

        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);

            Commands.setMainCommand("skjson");
            Commands.add(
                    "hello",
                    (sender, _) -> sender.sendMessage("Hello, world!"),
                    (_, _) -> List.of("world", "there", "player")
            );

            Commands.add(
                    "about",
                    (sender, _) -> {
                        if (! sender.hasPermission("skjson.use")) {
                            sender.sendMessage(logger().colorize("&cYou don't have permission to use this command."));
                        } else {
                            sender.sendMessage(logger().colorize("&aVersion: &f" + plMeta.getVersion()));
                            sender.sendMessage(logger().colorize("&aWebsite: &f" + plMeta.getWebsite()));
                            sender.sendMessage(logger().colorize("&aAuthor: &c" + plMeta.getAuthors()));
                            sender.sendMessage(logger().colorize(String.format("&aRevision version: &f%s", this.getConfig().get("revision-version"))));
                            sender.sendMessage(logger().colorize("&aDescription: &fSkript JSON library"));
                            sender.sendMessage(logger().colorize(String.format("&aDependencies: &f%s", plMeta.getPluginDependencies())));
                        }
                    },
                    (_, _) -> List.of()
            );

            Commands.registerCommand(this);
        }
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
