package cz.coffee.core.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cz.coffee.core.adapters.Adapters;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffee.core.utils.NumberUtils.parsedNumber;

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
public class Util {

    private static long START_TIME;

    public static final Gson GSON_ADAPTER = new GsonBuilder().serializeNulls().enableComplexMapKeySerialization().disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new Adapters.TypeAdapter.BukkitAdapter()).create();

    public static String color(Object message) {
        return ChatColor.translateAlternateColorCodes('&', message.toString());
    }

    public static String codeRunTime(boolean start) {
        long endTime = 0L;
        if (start) {
            START_TIME = System.currentTimeMillis();
        } else {
            endTime = System.currentTimeMillis();
        }
        NumberFormat formatter = new DecimalFormat("#0.000");
        return formatter.format((endTime - START_TIME) / 1000d);
    }


    public static Object jsonToObject(JsonElement json) {
        if (json == null || json.isJsonNull()) return null;
        if (json.isJsonArray() || json.isJsonObject()) return json;
        else if (json.isJsonPrimitive()) {
            return new Gson().fromJson(json.getAsJsonPrimitive(), Object.class);
        }
        return null;
    }

    public static String hex(Object object) {
        String message = object.toString();
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

    public static LinkedList<String> extractKeys(String string, String delimiter, boolean ...rawAdd) {
        boolean add = rawAdd != null && rawAdd.length > 0 && rawAdd[0];
        delimiter = delimiter == null ? ":(?![{}])" : delimiter;
        if (string == null) return null;
        LinkedList<String> extractedKeys = new LinkedList<>();
        final Pattern squareBrackets = Pattern.compile(".*\\[((\\d+|)])");
        final Pattern subPattern = Pattern.compile("^([^\\[.*]+)");
        final Pattern insideSquareBrackets = Pattern.compile("\\[(.*?)]");
        String nestedKey = null, nestedIndex = null;

        for (String item : string.split(delimiter)) {
            Matcher squares = subPattern.matcher(item);
            Matcher number = insideSquareBrackets.matcher(item);
            if (squareBrackets.matcher(item).find()) {
                while (squares.find()) if (squares.group(1) != null) nestedKey = squares.group(1);
                while (number.find()) {
                    if (number.group(1) != null) {
                        String num1 = number.group(1);
                        if (!(num1.isEmpty())) {
                            if (Objects.equals(number.group(1), "0")) return null;
                            nestedIndex = String.valueOf(parsedNumber(number.group(1)) -1);
                            if (parsedNumber(nestedIndex) < 0) nestedIndex = "0";
                        }
                    }
                }
                extractedKeys.add(add ? nestedKey + "->List" : nestedKey);
                if (nestedIndex != null)  extractedKeys.add(nestedIndex);
            } else {
                extractedKeys.add(item);
            }
        }
        return extractedKeys;
    }

    public static boolean arrayIsSafe(JsonArray array, int index) {
        try {
            if (array.size() <= index) return false;
            array.get(index);
        } catch (IndexOutOfBoundsException exception) {
            return false;
        }
        return true;
    }

}
