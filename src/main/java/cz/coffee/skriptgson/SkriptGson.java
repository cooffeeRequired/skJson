/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

import static cz.coffee.skriptgson.utils.Utils.color;
import static cz.coffee.skriptgson.utils.Utils.getGitVersion;


@SuppressWarnings({"unused", "FieldCanBeLocal"})

public class SkriptGson extends JavaPlugin {

    public static HashMap<String, Object> JSON_HASHMAP = new HashMap<>();
    public static HashMap<String, File> FILE_JSON_HASHMAP = new HashMap<>();

    private static Logger logger;
    private static PluginManager pm;
    private static Metrics metrics;

    private static SkriptGson instance;
    private static PluginDescriptionFile pdf;
    private SkriptAddon addon;

    public static SkriptGson getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

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

    @Override
    public void onEnable() {
        pm = getPluginManager();
        pdf = this.getDescription();

        if (!canLoadPlugin()) {
            pm.disablePlugin(this);
            return;
        }
        instance = this;
        addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (Exception ex) {
            severe("Unable to register " + getDescription().getName() + " syntaxes:\n- " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // gitHub
        githubChecker();
        // metrics
        loadMetrics();

        info("&aFinished loading.");
    }

    // Plugins preload checks
    private boolean canLoadPlugin() {
        boolean canLoad = true;
        String reason = null;
        Plugin skriptPlugin = pm.getPlugin("Skript");
        if (skriptPlugin == null) {
            reason = "Plugin 'Skript' is not found!";
            canLoad = false;
        } else if (!skriptPlugin.isEnabled()) {
            reason = "Plugin 'Skript' is not enabled!";
            canLoad = false;
        }
        if (!canLoad) {
            severe("Could not load " + pdf.getName() + ":\n- " + reason);
        }
        logger = getLogger();
        return canLoad;
    }

    private void loadMetrics() {
        metrics = new Metrics(this, 16953);
        metrics.addCustomChart(new Metrics.SimplePie("skript_version", () -> Skript.getVersion().toString()));
        bukkitOut("&fMetrics&r: Loaded metrics&a successfully!");
    }

    private void githubChecker() {
        String gitVersion = getGitVersion();

        if (Objects.equals(gitVersion, pdf.getVersion())) {
            bukkitOut("You're currently running the &flatest&r stable version of skript-gson");
        } else if (gitVersion != null) {
            warning("You're running on outdated version&c " + pdf.getVersion() + "&e!");
            warning("Download the latest version from Github");
            warning("Link: " + pdf.getWebsite() + "releases/tag/" + gitVersion);
        }
    }

    public PluginManager getPluginManager() {
        return this.getServer().getPluginManager();
    }

    @Override
    public void onDisable() {
        info("&eDisabling... good bye!");
    }

    // Simple loggers
    public void bukkitOut(String string) {
        Bukkit.getServer().getConsoleSender().sendMessage((color("[&askript-gson&r] " + string)));
    }

}
