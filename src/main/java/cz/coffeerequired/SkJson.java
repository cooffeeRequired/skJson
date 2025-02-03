package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.Commands;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.SkJsonLogger;
import cz.coffeerequired.support.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import static cz.coffeerequired.api.Api.Records.PROJECT_DEBUG;


@Slf4j
public final class SkJson extends JavaPlugin {
    @Getter
    static SkJson instance;
    @Getter
    static Configuration configuration;
    @Getter
    private static YamlConfiguration pluginConfig;
    private final Register register = new Register();

    @SuppressWarnings("UnstableApiUsage")
    private BiConsumer<CommandSender, String[]> aboutAddon() {
        return (sender, s) -> {
            var plMeta = this.getPluginMeta();

            @SuppressWarnings("unchecked") ArrayList<String> list = (ArrayList<String>) pluginConfig.get("soft-depend");

            if (!sender.hasPermission("skjson.use")) {
                error(sender, "&cYou don't have permission to use this command.");
            } else {
                info(sender, "&aVersion: &f%s", plMeta.getVersion());
                info(sender, "&aWebsite: &9%s", plMeta.getWebsite());
                info(sender, "&aRevision: &6%s", pluginConfig.get("revision-version"));
                info(sender, "&aDescription: &e%s", plMeta.getDescription());
                info(sender, "&aDependencies: &3%s", plMeta.getPluginDependencies());
                info(sender, "&6Soft-dependencies: &7%s", list);
            }
        };
    }

    @Override
    public void onLoad() {
        instance = this;

        Configuration.applyScheduledUpdate();
        pluginConfig = Configuration.getPluginConfig();

        configuration = new Configuration(this);
        configuration.checkForUpdate();

        setupMetrics(17374);
        info("bStats metrics enabled.");

        try {
            Class.forName("cz.coffeerequired.api.json.CacheStorageWatcher");
            info("Json watchers found & enabled.");
        } catch (ClassNotFoundException e) {
            info("Unable to find Json watchers.");
            exception(e, e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        info("Enabling SkJson.");

        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);

            Commands.setMainCommand("skjson");
            Commands.add("about|?", aboutAddon(), Commands.emptyCompleter());
            Commands.add(
                    "reload",
                    (sender, s) -> {
                        info(sender, " 🟠 &econfig reloading...");
                        try {
                            final WeakHashMap<String, ?> before = new WeakHashMap<>(Map.ofEntries(
                                    Map.entry("PROJECT_DEBUG", PROJECT_DEBUG),
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
                                        info("&7The field %s has been changed from &e %s &7 to &f %s", Configuration.getMapping(key), value, fieldValue);
                                    }
                                } catch (NoSuchFieldException | IllegalAccessException e) {
                                    exception(e, "Cannot handle that field %s", key);
                                }
                            });
                        } catch (Exception ex) {
                            exception(ex, "An error occurred while reloading configuration");
                        }
                    }, Commands.emptyCompleter());
            Commands.add("status", Commands.emptyCommand(), Commands.emptyCompleter());
            Commands.add("debug", (sender, args) -> {
                if (args.length == 2) {
                    if (args[1].equals("off") || args[1].equals("false")) {
                        PROJECT_DEBUG = false;
                        info(sender, "&7Debug mode was &cdisabled!");
                    } else if (args[1].equals("on") || args[1].equals("true")) {
                        PROJECT_DEBUG = true;
                        info(sender, "&7Debug mode was &aenabled!");
                    } else {
                        info(sender, "&7Incorrect usage: /skjson debug (on|off)");
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
    public void setupMetrics(int id) {
        Metrics metrics = new Metrics(this, id);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));
        metrics.addCustomChart(new SimplePie("skjson_version", () -> this.getPluginMeta().getVersion()));
    }

    @SuppressWarnings("unused")
    public void warning(CommandSender sender, String message, Object... args) {
        SkJsonLogger.warning(sender, message, args);
    }
    public static void info(Object message, Object... args) {
        SkJsonLogger.log(Level.INFO, message, args);
    }

    public static void info(CommandSender sender, String message, Object... args) {
        SkJsonLogger.info(sender, message, args);
    }

    public static void warning(Object message, Object... args) {
        SkJsonLogger.log(Level.WARNING, message, args);
    }

    public static void severe(Object message, Object... args) {
        SkJsonLogger.log(Level.INFO, "&c" + message, args);
    }

    public static void error(CommandSender sender, String message, Object... args) {
        SkJsonLogger.error(sender, message, args);
    }

    public static void exception(Throwable e, Object message, Object... args) {
        SkJsonLogger.ex(e, message, args);
    }

    public static void debug(Object message, Object... args) {
        if (PROJECT_DEBUG) SkJsonLogger.log(Level.INFO, "&8DEBUG ->&r" + message, args);
    }
}
