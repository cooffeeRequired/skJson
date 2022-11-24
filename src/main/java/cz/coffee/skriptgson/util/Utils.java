/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

@SuppressWarnings("unused")
public class Utils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    public static String SanitizeString(Object stringObject) {
        return String.valueOf(stringObject).replaceAll("[\"'][\\w\\s]+[\"']|\\w+[\"']\\w+", "").replaceAll("\"", "");
    }
    public static String SanitizeJson(Object stringObject) {
        return String.valueOf(stringObject).replaceAll("^[{}$]", "");
    }
    public static String gsonText(Object StringifyElement) {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(StringifyElement);
    }
    public static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

    public static Gson newGson() {
        GsonBuilder g = new GsonBuilder()
                .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitClassAdapt())
                .registerTypeHierarchyAdapter(YggdrasilSerializable.class, new SkriptClassAdapt());
       return g.disableHtmlEscaping().setPrettyPrinting().create();
    }

}
