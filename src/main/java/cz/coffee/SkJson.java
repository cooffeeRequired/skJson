package cz.coffee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.Node;
import ch.njol.skript.util.Version;
import com.google.gson.JsonElement;
import cz.coffee.core.Updater;
import cz.coffee.core.cache.CacheMap;
import cz.coffee.core.cache.JsonWatcher;
import cz.coffee.core.requests.HttpHandler.Response;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static cz.coffee.core.utils.Util.color;
import static cz.coffee.core.utils.Util.hex;

@SuppressWarnings({"deprecation"})
public final class SkJson extends JavaPlugin {
    public static final boolean PROJECT_DEBUG = true;
    public static final CacheMap<String, JsonElement, File> JSON_STORAGE = new CacheMap<>();
    public static final WeakHashMap<File, JsonWatcher> WATCHERS = new WeakHashMap<>();
    public static final List<CompletableFuture<Response>> RESPONSES = new ArrayList<>(2);
    private static final String[] dependencies = {"Skript"};
    private static final Version version = Skript.getMinecraftVersion();
    static final boolean legacy = version.isSmallerThan(new Version(1, 16, 5));
    static final String prefix = legacy ? color("&7[&ask&2Json&7]") : "&7[" + hex("#B6E69Cs#9BD97Ek#80CC61J#65BF43s#4AB226o#2FA508n") + "&7]";
    private static Logger logger;
    private static PluginManager pluginManager;
    private static SkJson instance;
    private static PluginDescriptionFile descriptionFile;

    public static SkJson getInstance() {
        if (instance == null) throw new IllegalStateException("SkJson is not initialized!");
        return instance;
    }

    public static PluginDescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    public static void error(@NotNull Object message) {
        logger.info(color("&c" + message));
    }

    public static void error(String message, Node node) {
        logger.info(color(node.toString()));
        logger.info(color("&c" + message));
    }

    @SuppressWarnings("unused")
    public static void warning(@NotNull Object message) {
        logger.warning(color(message));
    }

    public static void severe(Object o) {
        logger.severe(o.toString());
    }

    public static void console(String string) {
        Bukkit.getServer().getConsoleSender().sendMessage(color(prefix + " " + (legacy ? color(string) : hex(string))));
    }

    @SuppressWarnings("unused")
    public @NotNull Logger logger() {
        if (logger == null) throw new IllegalStateException("The logger has not been initialized!");
        return logger;
    }

    public @NotNull PluginManager getPluginManager() {
        return this.getServer().getPluginManager();
    }

    @Override
    public void onEnable() {
        try {
            new NBTContainer();
            console("NBT-API &aRegistered successfully");
        } catch (Exception ex) {
            console("NBT-API &cCannot be registered!");
        }

        instance = this;
        if (!pluginCanBeLoad())
            pluginManager.disablePlugin(this);

        SkriptAddon addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.skript");
        } catch (Exception exception) {
            severe("Unable to register " + descriptionFile.getName() + " syntax's:\n- " + exception.getMessage());
            pluginManager.disablePlugin(this);
        }
        JsonWatcher.init();
        console(JsonWatcher.getLogger().getName() + " was &ainitialized");
        new Updater();
        loadMetrics();
        console("&aFinished loading.");
    }

    @Override
    public void onDisable() {
        console(JsonWatcher.getLogger().getName() + " &7trying unload a JsonWatchers!");
        JsonWatcher.shutdown();
        console("&7Unload &aDone");
    }

    private void loadMetrics() {
        Metrics metrics = new Metrics(this, 17374);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));
        console("&fMetrics&r: Loaded metrics&a successfully!");
    }

    private boolean pluginCanBeLoad() {
        logger = getLogger();
        pluginManager = getPluginManager();
        descriptionFile = this.getDescription();
        boolean canContinue = true;
        String reason = "";
        Plugin skriptPlugin = pluginManager.getPlugin(dependencies[0]);
        if (skriptPlugin == null || !skriptPlugin.isEnabled()) {
            reason = "Plugin '" + dependencies[0] + "'" + (skriptPlugin == null ? " isn't found." : " isn't enabled.");
            canContinue = false;
        }
        if (!canContinue)
            severe("Couldn't load " + descriptionFile.getName() + ":\n- " + reason);
        return canContinue;
    }

    @SuppressWarnings("unused")
    public PluginManager getManager() {
        return instance.getPluginManager();
    }
}
