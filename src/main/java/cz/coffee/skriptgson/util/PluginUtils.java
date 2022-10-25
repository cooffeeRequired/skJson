package cz.coffee.skriptgson.util;

import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public class PluginUtils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    public static String SanitizeString(Object strObj) {
        return String.valueOf(strObj).replaceAll("[\"'][\\w\\s]+[\"']|\\w+[\"']\\w+", "");
    }
}
