package cz.coffee.skjson.utils;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.ErrorQuality;
import cz.coffee.skjson.api.ColorWrapper;
import cz.coffee.skjson.api.Config;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.coffee.skjson.api.Config.PATH_VARIABLE_DELIMITER;
import static cz.coffee.skjson.api.Config.REQUESTS_PREFIX;

/**
 * The type Util.
 */
public class Util {

    /**
     * Parse number int.
     *
     * @param potentialNumber the potential number
     * @return the int
     */
    public static int parseNumber(Object potentialNumber) {
        if (potentialNumber != null && potentialNumber.toString().matches("-?\\d+(\\.\\d+)?")) {
            return Integer.parseInt(potentialNumber.toString());
        }
        return -9999;
    }

    /**
     * Enchanted error.
     *
     * @param ex            the ex
     * @param traceElements the trace elements
     * @param errorID       the error id
     */
    public static void enchantedError(Exception ex, StackTraceElement[] traceElements, String errorID) {
        Util.error("&4--------------------------- &l&cSkJson error handling &4---------------------------");
        Util.error("          " + ex.getLocalizedMessage() + "          ");
        Util.error("          " + errorID + "          ");
        Util.error("&4------------------------------------------------------------------------------------");
        Util.errorWithoutPrefix("");
        int i = traceElements.length;
        for (StackTraceElement st : traceElements) {
            int lineNumber = st.getLineNumber();
            String clazz = st.getFileName();
            String mess = st.getMethodName();
            Util.errorWithoutPrefix(String.format("&e%s. &#eb6565%s &7(&f%s:%s&7)", i, mess, clazz, lineNumber));
            i--;
        }
        Util.error("&4--------------------------- &l&cEnd of error handling &4---------------------------");
    }

    private static String sanitizeDelimiter(String st) {
        if (st.contains(".")) st = st.replace(".", "\\" + ".");
        return st;
    }
    private static Map<String, Boolean> checkDelimiter(String st) {
        Map<String, Boolean> message = new HashMap<>();
        List<String> chars = new ArrayList<String>();

        if (!st.matches("[\\p{L}\\p{N}\\\\p{\\d.+}\\s_\\-<>]+")) {
            // contains special chars
            if (st.contains("::")) {
                message.put("::", true);
                return message;
            }

            String regex = "[^a-zA-Z0-9\\[\\]]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(st);
            while (matcher.find()) chars.add(matcher.group());

            for (String ch : chars) {
                if (!ch.equals(PATH_VARIABLE_DELIMITER)) {
                    message.put(ch, false);
                    return message;
                }
            }
        }

        message.put("", true);
        return message;
    }


    /**
     * Extract keys to list linked list.
     *
     * @param string    the string
     * @param delimiter the delimiter
     * @param rawAdding the raw adding
     * @return the linked list
     */
    public static LinkedList<String> extractKeysToList(String string, String delimiter, boolean ...rawAdding) {
        if (string == null) return null;
        try {
            String finalString = string;
            checkDelimiter(string).forEach((ch, b) -> {
                if (!b) throw new IllegalArgumentException("\n  \t\t\t&f- &cThe path-delimiter in the script is different from what is set in SkJson's config. \n  \t\t\t&f- Error node: &c" + finalString + " \n  \t\t\t&f- Wrong delimiter &c" + ch);
            });
        } catch (IllegalArgumentException ex) {
            Util.error("Configuration error!", true, ex.getLocalizedMessage());
        }

        boolean add = rawAdding != null && rawAdding.length > 0 && rawAdding[0];
        delimiter = sanitizeDelimiter(delimiter == null ? PATH_VARIABLE_DELIMITER + "(?![{}])" : delimiter);

        LinkedList<String> extractedKeys = new LinkedList<>();

        if (string.endsWith("*")) {
            string = string.replace("*", "");
        } else if (string.endsWith(PATH_VARIABLE_DELIMITER + "*")) {
            string = string.replace(PATH_VARIABLE_DELIMITER + "*", "");
        }

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
                        String n1 = number.group(1);
                        if (!(n1.isBlank() || n1.isEmpty())) {
                            if (Objects.equals(number.group(1), 0)) return null;
                            nestedIndex = String.valueOf(parseNumber(number.group(1)));
                            if (parseNumber(nestedIndex) < 0) nestedIndex = "0";
                        }
                    }
                }

                extractedKeys.add(add ? nestedKey + "->List" : nestedKey);
                if (nestedIndex != null) extractedKeys.add(nestedIndex);
            } else {
                extractedKeys.add(item);
            }
        }
        return extractedKeys;
    }

    /**
     * Log.
     *
     * @param msg the msg
     */
    public static void log(Object msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + msg));
    }

    /**
     * Is number boolean.
     *
     * @param obj the obj
     * @return the boolean
     */
    public static boolean isNumber(Object obj) {
        return obj != null && obj.toString().matches("-?\\d+(\\.\\d+)?");
    }


    /**
     * Is increment boolean.
     *
     * @param inputs the inputs
     * @return the boolean
     */
    public static boolean isIncrement(Object @NotNull [] inputs) {
        Integer[] numbers;
        ArrayList<Integer> intArray = new ArrayList<>();
        for (Object input : inputs) {
            if (isNumber(input)) {
                intArray.add(Integer.parseInt(input.toString()));
            }
        }
        numbers = intArray.toArray(new Integer[0]);

        for (int i = 0; i < numbers.length - 1; i++) {
            if (numbers[i + 1] != numbers[i] + 1) {
                return false;
            }
        }
        return true;
    }


    /**
     * Watcher log.
     *
     * @param msg the msg
     */
    public static void watcherLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + Config.WATCHER_PREFIX + msg));
    }

    /**
     * Webhook log.
     *
     * @param msg the msg
     */
    public static void webhookLog(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + Config.WEBHOOK_PREFIX + msg));
    }

    /**
     * Error.
     *
     * @param msg     the msg
     * @param quality the quality
     */
    public static void error(String msg, ErrorQuality ...quality) {

        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + Config.ERROR_PREFIX + "&l&c"+msg));
    }

    public static void error(String SkriptErrorMessage, boolean skript, String e) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + Config.ERROR_PREFIX + "&l&c"+e));
    }

    /**
     * Error without prefix.
     *
     * @param msg the msg
     */
    public static void errorWithoutPrefix(String msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&l&c"+msg));
    }

    /**
     * Error.
     *
     * @param msg     the msg
     * @param quality the quality
     * @param node    the node
     */
    public static void error(String msg, ErrorQuality quality, @Nullable Node node) {
        int line = node == null ? 0 : node.getLine();
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(Config.PLUGIN_PREFIX + "&c&lLine "+line + ":&8 ("+node.getConfig().getFileName() + ")"));
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate("&#f27813\t" + msg));
    }

    /**
     * Request log.
     *
     * @param msg the msg
     */
    public static void requestLog(Object msg) {
        Bukkit.getConsoleSender().sendMessage(ColorWrapper.translate(REQUESTS_PREFIX + "&#fc4103Warning: &l&c " + msg));
    }
}
