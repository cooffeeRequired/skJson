/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static cz.coffee.skriptgson.util.Utils.color;


@SuppressWarnings({"unused", "FieldCanBeLocal"})

public class SkriptGson extends JavaPlugin {
    private static Logger logger;
    private static PluginManager pm;
    private static Metrics metrics;

    private static SkriptGson instance;
    private SkriptAddon addon;

    @Override
    public void onEnable() {
        pm = getPluginManager();
        if (!canLoadPlugin()) {
            pm.disablePlugin(this);
            return;
        }
        instance = this;
        addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (Exception ex) {
            severe("Unable to register " + getDescription().getName() + " syntaxes:\n" + ex.getMessage());
            ex.printStackTrace();
            severe(getDescription().getName() + " might not work properly!");
            return;
        }
        loadMetrics();
        info("&aFinished loading.");
    }

    // Plugin preload checks
    private boolean canLoadPlugin() {
        boolean canLoad = true;
        String reason = null;
        Plugin skriptPlugin = pm.getPlugin("Skript");
        if (skriptPlugin == null) {
            reason = "Plugin 'Skript' is not found!";
            canLoad = false;
        }
        else if (!skriptPlugin.isEnabled()) {
            reason = "Plugin 'Skript' is not enabled!";
            canLoad = false;
            }

        if (!canLoad) {
            severe("Could not load " + getDescription().getName() + ":\n- " + reason);
        }
        return canLoad;
    }

    // Metrics loader
    private void loadMetrics() {
        metrics = new Metrics(this, 16943);
        metrics.addCustomChart(new Metrics.SimplePie("skript_version", () -> Skript.getVersion().toString()));
        info("Loaded metrics!");
    }

    public static SkriptGson getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public PluginManager getPluginManager() {
        return pm;
    }

    @Override
    public void onDisable() {
        info("&eDisabling... good bye!");
    }

    // Simple loggers
    public static void info(String string) {
        logger.info(color(string));
    }

    public static void warning(String string) {
        logger.warning(color("&e" + string));
    }

    public static void severe(String string) {
        logger.severe(color("&c" + string));
    }

    public static void debug(Object str) {
        logger.severe(color("DEBUG! " + "&r" + str));
    }

}
