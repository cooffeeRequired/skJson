package cz.coffeerequired.support;

import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class AnsiColorConverter {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
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
            new AbstractMap.SimpleEntry<>('r', "\u001B[0m")
    );
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fr])");

    // Method to convert a string to an ANSI colorized version
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

    // Method to convert a hex color code to an ANSI escape code (basic mapping for simplicity)
    public static String hexToAnsi(String hex) {
        // Parse the hex color
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);

        // Return ANSI 24-bit color escape code
        return String.format("\u001B[38;2;%d;%d;%dm", r, g, b);
    }

    public static String convertToAnsi(String text) {
        Matcher matcher = COLOR_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            char colorCode = matcher.group(1).charAt(0);
            String ansiCode = ANSI_CODES.getOrDefault(colorCode, "");
            matcher.appendReplacement(result, ansiCode);
        }
        matcher.appendTail(result);
        return result + "\u001B[0m";
    }
}
