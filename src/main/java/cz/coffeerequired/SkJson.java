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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;


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
            Class.forName("cz.coffeerequired.api.json.CacheStorageWatcher");
            logger.info("Json watchers found & enabled.");
        } catch (ClassNotFoundException e) {
            logger.info("Unable to find Json watchers.");
            logger.exception(e.getMessage(), e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private BiConsumer<CommandSender, String[]> aboutAddon() {
        return (sender, _) -> {
            var plMeta = this.getPluginMeta();

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
        };
    }

    @Override
    public void onEnable() {
        logger.info("Enabling...");
        configuration.copySkriptTests();
        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);

            Commands.setMainCommand("skjson");
            Commands.add("about|?", aboutAddon(), Commands.emptyCompleter());
            Commands.add(
                    "reload",
                    (sender, _) -> {
                        sender.sendMessage(logger().colorize(CustomLogger.getGRADIENT_PREFIX() + " 🟠 &econfig reloading..."));
                        try {
                            final WeakHashMap<String, ?> before = new WeakHashMap<>(Map.ofEntries(
                               Map.entry("PROJECT_DEBUG", Api.Records.PROJECT_DEBUG),
                               Map.entry("PROJECT_DELIM", Api.Records.PROJECT_DELIM),
                               Map.entry("PROJECT_PERMISSION", Api.Records.PROJECT_PERMISSION),
                                Map.entry("PROJECT_ENABLED_HTTP", Api.Records.PROJECT_ENABLED_HTTP),
                                Map.entry("PROJECT_ENABLED_NBT", Api.Records.PROJECT_ENABLED_NBT),
                                Map.entry("WATCHER_INTERVAL", Api.Records.WATCHER_INTERVAL),
                                Map.entry("WATCHER_REFRESH_RATE", Api.Records.WATCHER_REFRESH_RATE)
                            ));
                            configuration.getHandler().reloadConfig();
                            Boolean[] changed = new Boolean[]{false};
                            before.forEach((key, value) -> {
                                try {
                                    var field = Api.Records.class.getDeclaredField(key);
                                    field.setAccessible(true);
                                    var fieldValue = field.get(null);

                                    if (!value.equals(fieldValue)) {
                                        if (!changed[0]) changed[0] = true;
                                        logger().info("&7The field " + Configuration.getMapping(key) + " has been changed from &e" + value + "&7 to &f" + fieldValue);
                                    }
                                } catch (NoSuchFieldException | IllegalAccessException e) {
                                    logger().exception("Cannot handle that field " + key, e);
                                }
                            });
                        } catch (Exception ex) {
                            logger().exception("", ex);
                        }
            }, Commands.emptyCompleter());
            Commands.add("status", Commands.emptyCommand(), Commands.emptyCompleter());
            Commands.add("debug", (sender, args) -> {
                if (args.length == 2) {
                    if (args[1].equals("off") || args[1].equals("false")) {
                        Api.Records.PROJECT_DEBUG = false;
                        sender.sendMessage(logger().colorize("&7Debug mode was &cdisabled!"));
                    } else if (args[1].equals("on") || args[1].equals("true")) {
                        Api.Records.PROJECT_DEBUG = true;
                        sender.sendMessage(logger().colorize("&7Debug mode was &aenabled!"));
                    } else {
                        sender.sendMessage(logger().colorize("&7Incorrect usage: /skjson debug (on|off)"));
                    }
                }
            }, Commands.emptyCompleter());
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
