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

    /**
     * Create new pluginLogger.
     */
    private static Logger logger = null;

    private static SkriptGson instance;
    private SkriptAddon addon;

    @Override
    public void onEnable() {
        if (!canLoadPlugin()) {
            getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        try {
            addon = Skript.registerAddon(this);
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (Exception ex) {
            severe("Unable to register " + getDescription().getName() + " syntax's:\n- " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        info("&aFinished loading.");

    }

    // Plugin preload checks
    private boolean canLoadPlugin() {
        logger = getLogger();
        boolean canLoad = true;
        String reason = null;
        Plugin skriptPlugin = getPluginManager().getPlugin("Skript");
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

    public static SkriptGson getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public PluginManager getPluginManager() {
        return this.getServer().getPluginManager();
    }

    @Override
    public void onDisable() {
        info("&eDisabling... good bye!");
    }

    // Utilities
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
