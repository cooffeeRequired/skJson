package cz.coffeerequired.support;

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
}
