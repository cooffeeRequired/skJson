package cz.coffee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;


@SuppressWarnings("unused")

public class SkriptJson extends JavaPlugin {

    final public static String docSince = "2.6.3";
    public static SkriptJson instance;
    SkriptAddon addon;

    @Override
    public void onEnable() {
        instance = this;
        addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.gson.skript");
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
}
