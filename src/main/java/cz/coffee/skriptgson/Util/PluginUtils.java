package cz.coffee.skriptgson.Util;

import org.bukkit.ChatColor;

public class PluginUtils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
