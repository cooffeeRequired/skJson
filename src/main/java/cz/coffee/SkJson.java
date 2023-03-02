package cz.coffee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import cz.coffee.core.Preload;
import cz.coffee.core.Version;
import cz.coffee.core.annotation.Used;
import cz.coffee.core.cache.Cache;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static cz.coffee.core.Utils.color;
import static cz.coffee.core.Utils.hex;

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
 */

@Used
public final class SkJson extends JavaPlugin {

    private static final String[] dependency = {"Skript"};
    private static final Version version = new Version(Bukkit.getBukkitVersion());
    private static Logger logger;
    private static PluginManager pluginManager;
    private static SkJson instance;
    private static PluginDescriptionFile descriptionFile;

    public static SkJson getInstance() {
        if (instance == null) throw new IllegalStateException("instance can't be a null");
        return instance;
    }

    public static Logger logger() {
        if (logger == null) throw new RuntimeException("The logger is null or empty");
        return logger;
    }

    public static PluginManager pluginManager() {
        return instance.getPluginManager();
    }

    public static PluginDescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    public static void severe(Object o) {
        logger.severe(o.toString());
    }

    public static void console(String string) {
        String prefix = version.isLegacy() ? color("&7[&ask&2Json&7]") : "&7[" + hex("#B6E69Cs#9BD97Ek#80CC61J#65BF43s#4AB226o#2FA508n") + "&7]";
        Bukkit.getServer().getConsoleSender().sendMessage(color(prefix + " " + (version.isLegacy() ? color(string) : hex(string))));
    }

    @Override
    public void onEnable()
    {
        new Cache();

        instance = this;
        if (!pluginCanBeLoad())
            pluginManager.disablePlugin(this);

        SkriptAddon addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.skript");
        } catch (Exception exception) {
            severe("Unable to register " + descriptionFile.getName() + " syntaxes:\n- " + exception.getMessage());
            pluginManager.disablePlugin(this);
        }








    }

    @Override
    public void onDisable()
    {

    }

    public PluginManager getPluginManager() {
        return this.getServer().getPluginManager();
    }

    @Used
    public Version getVersion() {
        return version;
    }

    @SuppressWarnings("deprecation")
    private boolean pluginCanBeLoad() {
        logger = getLogger();
        pluginManager = getPluginManager();
        descriptionFile = this.getDescription();
        boolean canContinue = true;
        String reason = "";
        Plugin skriptPlugin = pluginManager.getPlugin(dependency[0]);
        if (skriptPlugin == null || !skriptPlugin.isEnabled()) {
            reason = "Plugin '"+dependency[0]+"'" + (skriptPlugin == null ? " isn't found." : " isn't enabled.");
            canContinue = false;
        }
        if (!canContinue)
            severe("Couldn't load " + descriptionFile.getName() + ":\n- " + reason);
        new Preload();
        return canContinue;
    }



}
