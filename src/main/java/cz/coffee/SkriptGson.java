package cz.coffee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;


@SuppressWarnings("unused")

public class SkriptGson extends JavaPlugin {

    private static Logger logger;

    public static final String docSince = "2.6.3";
    public static SkriptGson instance;
    SkriptAddon addon;

    @Override
    public void onEnable() {
        instance = this;
        try {
            addon = Skript.registerAddon(this);
        } catch ( IllegalAccessError e) {
            e.printStackTrace();
        }

        try {
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (IOException IOe) {
            IOe.printStackTrace();
        } finally {
            System.out.println(ChatColor.translateAlternateColorCodes('&', "&7Loaded &aSuccessfully"));
        }
    }

    @Override
    public void onDisable() {
        System.out.println(ChatColor.translateAlternateColorCodes('&', "&cDisabled"));
    }

    public void info(String string) {
        logger.info(string);
    }

    public void warning(String string) {
        logger.warning(string);
    }

    public void severe(String string) {
        logger.severe(string);
    }

}
