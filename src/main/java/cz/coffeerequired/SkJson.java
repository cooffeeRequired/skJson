package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.Commands;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.SkJsonLogger;
import cz.coffeerequired.api.cache.CacheStorageWatcher;
import cz.coffeerequired.support.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static cz.coffeerequired.api.Api.Records.*;
import static cz.coffeerequired.api.SkJsonCommands.*;


@Slf4j
public final class SkJson extends JavaPlugin {
    @Getter
    static SkJson instance;
    @Getter
    static Configuration configuration;
    @Getter
    private static YamlConfiguration pluginConfig;
    private final Register register = new Register();

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
        if (PROJECT_DEBUG) SkJsonLogger.log(Level.INFO, "&8DEBUG -> &r" + message, args);
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
            Class.forName("cz.coffeerequired.api.cache.CacheStorageWatcher");
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
            try {
                register.registerNewHook(Skript.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Commands.setMainCommand("skjson");
            Commands.add("about|?", aboutAddon(this.getPluginMeta(), pluginConfig), Commands.emptyCompleter());
            Commands.add("reload", reloadAddon(configuration), Commands.emptyCompleter());
            Commands.add("config|configuration", configuration(), Commands.emptyCompleter());
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
        info("Disabling SkJson.");
        CacheStorageWatcher.Extern.unregisterAll();
        Api.getCache().free();
        info("Cache storage was freed.");

        super.onDisable();
    }


    @SuppressWarnings("UnstableApiUsage")
    public void setupMetrics(int id) {
        Metrics metrics = new Metrics(this, id);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));

        var core = Register.registers.getFirst().getLoadedElements();
        var http = Register.registers.getLast().getLoadedElements();

        metrics.addCustomChart(new AdvancedPie("features_core", () -> Map.of(
                "Expressions",          core.get("Expressions").size(),
                "Effects",              core.get("Effects").size(),
                "Sections",             core.get("Sections").size(),
                "Conditions",           core.get("Conditions").size(),
                "Functions",            core.get("Functions").size(),
                "Structures",           core.get("Structures").size(),
                "Types",                core.get("Types").size(),
                "Event Expressions",    core.get("Event Expressions").size()
        )));

        metrics.addCustomChart(new AdvancedPie("features_http", () -> Map.of(
                "Expressions",          http.get("Expressions").size(),
                "Effects",              http.get("Effects").size(),
                "Sections",             http.get("Sections").size(),
                "Conditions",           http.get("Conditions").size(),
                "Functions",            http.get("Functions").size(),
                "Structures",           http.get("Structures").size(),
                "Types",                http.get("Types").size(),
                "Event Expressions",    http.get("Event Expressions").size()
        )));
    }

    @SuppressWarnings("unused")
    public void warning(CommandSender sender, String message, Object... args) {
        SkJsonLogger.warning(sender, message, args);
    }
}
