/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils;

import ch.njol.skript.doc.Since;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.coffee.adapters.TypeAdapterBukkitClass;
import cz.coffee.adapters.TypeAdapterSkriptClass;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Since("2.2.0")

public class SimpleUtil {

    /**
     * <p>
     *     This function will translate alternative color code to actually colors.
     * </p>
     * @param input will take {@link Object}
     * @return will return transformed {@link String}.
     */
    public String color (Object input) {return ChatColor.translateAlternateColorCodes('&', String.valueOf(input));}


    /**
     * <p>
     *     This function will return true if input is any Number.
     * </p>
     * @param t  will take any {@link Object} and convert it to {@link String}
     * @return {@link Boolean}.
     */
    public static boolean isNumeric(Object t) {return t != null && t.toString().matches("[0-9]+");}


    /**
     * <p>
     *     This function will return true the number is continuously increasing.
     * </p>
     * @param inputs  will take any objects {@link Object}
     * @return {@link Boolean}.
     */
    public static boolean isIncrementNumber(Object @NotNull [] inputs) {
        int step = 1;
        int count = 1;
        for (Object k : inputs) {
            if (k instanceof Number) {
                count = ((Number) k).intValue();
            }
            else if (k instanceof String)  count = Integer.parseInt(((String) k));
            if (step != count) return false;
            step++;
        }
        return true;
    }

    /**
     * <p>
     *     This constant will return the new Gson() with a registerTypeHierarchyAdapter.
     * </p>
     */
    public static final Gson gsonAdapter = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new TypeAdapterBukkitClass())
            .registerTypeHierarchyAdapter(YggdrasilSerializable.YggdrasilExtendedSerializable.class, new TypeAdapterSkriptClass())
            .registerTypeHierarchyAdapter(YggdrasilSerializable.class, new TypeAdapterSkriptClass())
            .create();

    public static String hex(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
