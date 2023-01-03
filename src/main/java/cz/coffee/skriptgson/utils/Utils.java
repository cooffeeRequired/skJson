/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.utils;

import org.bukkit.ChatColor;

public class Utils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("[0-9.]+");
    }


    public static boolean isIncrementing(Object[] indexes) {
        int step = 1;
        int count = 1;
        for (Object o : indexes) {
            if (o instanceof String iStr) {
                count = Integer.parseInt(iStr);
            } else if (o instanceof Number number) {
                count = number.intValue();
            }
            if (step != count) return false;
            step++;
        }
        return true;
    }
}
