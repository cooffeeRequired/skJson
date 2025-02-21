package cz.coffeerequired.support;

import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiColorConverter {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";

    private static final Map<Character, String> ANSI_CODES = Map.ofEntries(
            new AbstractMap.SimpleEntry<>('0', "\u001B[30m"),
            new AbstractMap.SimpleEntry<>('1', "\u001B[34m"),
            new AbstractMap.SimpleEntry<>('2', "\u001B[32m"),
            new AbstractMap.SimpleEntry<>('3', "\u001B[36m"),
            new AbstractMap.SimpleEntry<>('4', "\u001B[31m"),
            new AbstractMap.SimpleEntry<>('5', "\u001B[35m"),
            new AbstractMap.SimpleEntry<>('6', "\u001B[33m"),
            new AbstractMap.SimpleEntry<>('7', "\u001B[37m"),
            new AbstractMap.SimpleEntry<>('8', "\u001B[90m"),
            new AbstractMap.SimpleEntry<>('9', "\u001B[94m"),
            new AbstractMap.SimpleEntry<>('a', "\u001B[92m"),
            new AbstractMap.SimpleEntry<>('b', "\u001B[96m"),
            new AbstractMap.SimpleEntry<>('c', "\u001B[91m"),
            new AbstractMap.SimpleEntry<>('d', "\u001B[95m"),
            new AbstractMap.SimpleEntry<>('e', "\u001B[93m"),
            new AbstractMap.SimpleEntry<>('f', "\u001B[97m"),
            new AbstractMap.SimpleEntry<>('r', "\u001B[0m"), // Reset
            new AbstractMap.SimpleEntry<>('l', "\u001B[1m"), // Bold
            new AbstractMap.SimpleEntry<>('o', "\u001B[3m"), // Italic
            new AbstractMap.SimpleEntry<>('n', "\u001B[4m"), // Underline
            new AbstractMap.SimpleEntry<>('m', "\u001B[9m"), // Strikethrough
            new AbstractMap.SimpleEntry<>('k', "\u001B[8m")  // Obfuscated
    );

    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fl-r])|&#([0-9a-fA-F]{6})");

    public static String convertToAnsi(String text) {
        Matcher matcher = COLOR_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String replacement;
            if (matcher.group(1) != null) { // Minecraft color code (&)
                char colorCode = matcher.group(1).charAt(0);
                replacement = ANSI_CODES.getOrDefault(colorCode, "");
            } else if (matcher.group(2) != null) { // Hex color code (&#)
                String hexColor = matcher.group(2);
                replacement = hexToAnsi(hexColor);
            } else {
                replacement = ""; // Should not happen, but just in case
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString() + RESET; // Ensure reset at the end
    }


    // Method to convert a hex color code to an ANSI escape code (24-bit color)
    public static String hexToAnsi(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return String.format("\u001B[38;2;%d;%d;%dm", r, g, b);
        } catch (NumberFormatException e) {
            return ""; // Invalid hex code, return empty string
        }
    }


    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

}