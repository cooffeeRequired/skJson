package cz.coffee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.Node;
import ch.njol.skript.util.Version;
import com.google.gson.JsonElement;
import cz.coffee.core.CacheMap;
import cz.coffee.core.Updater;
import cz.coffee.core.cache.JsonWatcher;
import de.tr7zw.nbtapi.NBTContainer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import static cz.coffee.core.Util.color;
import static cz.coffee.core.Util.hex;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Saturday (3/4/2023)
 */
@SuppressWarnings({"unused", "deprecation"})
public final class SkJson extends JavaPlugin {
    public static final boolean PROJECT_DEBUG = true;
    public static final CacheMap<String, JsonElement, File> JSON_STORAGE = new CacheMap<>();
    public static final WeakHashMap<File, JsonWatcher> WATCHERS = new WeakHashMap<>();
    private static final String[] dependencies = {"Skript"};
    private static final Version version = Skript.getMinecraftVersion();
    private static Logger logger;
    private static PluginManager pluginManager;
    private static SkJson instance;
    private static PluginDescriptionFile descriptionFile;
    static final boolean legacy = version.isSmallerThan(new Version(1,16,5));
    static final String prefix = legacy ? color("&7[&ask&2Json&7]") : "&7[" + hex("#B6E69Cs#9BD97Ek#80CC61J#65BF43s#4AB226o#2FA508n") + "&7]";

    public static SkJson getInstance() {
        if (instance == null) throw new IllegalStateException("SkJson is not initialized!");
        return instance;
    }

    public static PluginDescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    public static void error(@NotNull Object message) {
        logger.info(color("&c"+message));
    }

    public static void error(String message, Node node) {
        logger.info(color(node.toString()));
        logger.info(color("&c" + message));
    }

    public static void severe(Object o) {
        logger.severe(o.toString());
    }

    public static void console(String string) {
        Bukkit.getServer().getConsoleSender().sendMessage(color(prefix + " " + (legacy ? color(string) : hex(string))));
    }

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
        console(JsonWatcher.getLogger().getName() +  " was &ainitialized");
        new Updater();
        console("&aFinished loading.");
    }
    @Override
    public void onDisable() {
        console(JsonWatcher.getLogger().getName() +  " &7trying unload a JsonWatchers!");
        JsonWatcher.shutdown();
        console("&7Unload &aDone");
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
        //new Preload();
        return canContinue;
    }

    public PluginManager getManager() {
        return instance.getPluginManager();
    }
}
