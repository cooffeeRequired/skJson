package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.Commands;
import cz.coffeerequired.api.CustomLogger;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.support.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public final class SkJson extends JavaPlugin {
    @Getter
    static SkJson instance;
    @Getter
    static Configuration configuration;
    static CustomLogger logger;
    @Getter
    private static YamlConfiguration pluginConfig;
    private final Register register = new Register();

    public static @NotNull CustomLogger logger() {
        return logger;
    }

    @Override
    public void onLoad() {
        instance = this;
        logger = new CustomLogger(this.getName());

        Configuration.applyScheduledUpdate();
        pluginConfig = Configuration.getPluginConfig();

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

        configuration.copySkriptTests();

        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);

            Commands.setMainCommand("skjson");
            Commands.add(
                    "about",
                    (sender, _) -> {

                        @SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) pluginConfig.get("soft-depend");

                        if (!sender.hasPermission("skjson.use")) {
                            sender.sendMessage(logger().colorize("&cYou don't have permission to use this command."));
                        } else {
                            sender.sendMessage(logger().colorize(String.format("&aVersion: &f%s", plMeta.getVersion())));
                            sender.sendMessage(logger().colorize(String.format("&aWebsite: &9%s", plMeta.getWebsite())));
                            sender.sendMessage(logger().colorize(String.format("&aAuthor: &c%s", plMeta.getAuthors())));
                            sender.sendMessage(logger().colorize(String.format("&aRevision: &6%s", pluginConfig.get("revision-version"))));
                            sender.sendMessage(logger().colorize(String.format("&aDescription: &e%s", plMeta.getDescription())));
                            sender.sendMessage(logger().colorize(String.format("&aDependencies: &3%s", plMeta.getPluginDependencies())));
                            sender.sendMessage(logger().colorize(String.format("&6Soft-dependencies: &7%s", list)));
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
