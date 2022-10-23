package cz.coffee.skriptgson;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.registrations.Classes;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.Util.PluginUtils;
import cz.coffee.skriptgson.skript.GsonType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;


@SuppressWarnings("unused")

public class SkriptGson extends JavaPlugin {

    private static Logger logger;

    private static SkriptGson instance;
    public static final String PREFIX = "&7[&6skript-gson&7] ";
    public static final String docSince = "2.6.3";
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
            new GsonType();
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (Exception ex) {
            SkriptGson.severe("Unable to register " + getDescription().getName() + " synttaxes:\n- " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        if (Classes.getExactClassInfo(JsonElement.class) != null)
            info("DEBUG &7Registered skript type 'jsonelement'!");
        else
            severe("DEBUG Failed to register skript type 'jsonelement");
        info("&aFinished loading.");
    }

    // Plugin pre-load checks
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
        Bukkit.getConsoleSender().sendMessage(PluginUtils.color(PREFIX + string));
    }

    public static void warning(String string) {
        Bukkit.getConsoleSender().sendMessage(PluginUtils.color(PREFIX + "&e" + string));
        logger.warning(string);
    }

    public static void severe(String string) {
        Bukkit.getConsoleSender().sendMessage(PluginUtils.color(PREFIX + "&c" + string));
        logger.severe(string);
    }

}