/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.util;

import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;

@SuppressWarnings("unused")
public class PluginUtils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    public static String SanitizeString(Object strObj) {
        return String.valueOf(strObj).replaceAll("[\"'][\\w\\s]+[\"']|\\w+[\"']\\w+", "").replaceAll("\"", "");
    }
    public static String gsonText(Object StringifyElement) {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(StringifyElement);
    }
}
