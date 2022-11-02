/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import cz.coffee.skriptgson.util.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


@SuppressWarnings({"unused", "FieldCanBeLocal"})

public class SkriptGson extends JavaPlugin {

    private static Logger logger;

    private static SkriptGson instance;
    public static final String PREFIX = "&7[&e&lskript-gson&7] ";
    private SkriptAddon addon;
    private final PluginManager pluginManager = this.getServer().getPluginManager();

    @Override
    public void onEnable() {
        if (!canLoadPlugin()) {
            pluginManager.disablePlugin(this);
            return;
        }
        instance = this;
        try {
            addon = Skript.registerAddon(this);
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (Exception ex) {
            SkriptGson.severe("Unable to register " + getDescription().getName() + " syntax's:\n- " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        info("&aFinished loading.");
        info("Build: 1.0.3.3");

    }

    // Plugin preload checks
    private boolean canLoadPlugin() {
        boolean canLoad = true;
        String reason = null;
        Plugin skriptPlugin = pluginManager.getPlugin("Skript");
        if (skriptPlugin == null) {
            reason = "Plugin 'Skript' is not found!";
            canLoad = false;
        }
        else if (!skriptPlugin.isEnabled()) {
            reason = "Plugin 'Skript' is not enabled!";
            canLoad = false;
            }

        if (!canLoad) {
            SkriptGson.severe("Could not load " + getDescription().getName() + ":\n- " + reason);
        }
        return canLoad;
    }

    public static SkriptGson getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    @Override
    public void onDisable() {
        SkriptGson.info("&eDisabling... good bye!");
    }

    // Utilities
    public static void info(String string) {
        Bukkit.getLogger().info(PluginUtils.color(PREFIX + PluginUtils.color(string)));
    }

    public static void warning(String string) {
        Bukkit.getLogger().warning(PluginUtils.color(PREFIX + "&e" + PluginUtils.color(string)));
    }

    public static void severe(String string) {
        Bukkit.getLogger().severe(PluginUtils.color(PREFIX + "&c" + PluginUtils.color(string)));
    }

}
