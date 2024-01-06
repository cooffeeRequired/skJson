package cz.coffee.skjson.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffee.skjson.api.ConfigRecords.*;
import static cz.coffee.skjson.utils.Util.parseNumber;

public abstract class PatternUtil {
    static final Pattern squareBrackets = Pattern.compile(".*\\[((\\d+|)])");
    static final Pattern subPattern = Pattern.compile("^([^\\[.*]+)");
    static final Pattern insideSquareBrackets = Pattern.compile("\\[(.*?)]");

    private static String sanitizeDelimiter(String st) {
        if (st == null) return st;
        if (st.contains(".")) st = st.replace(".", "\\" + ".");
        return st;
    }

    public static String sanitizeString(String st) {
        if (st == null) return null;
        if (st.endsWith("*")) {
            st = (st.replace("*", ""));
        } else if (st.endsWith(PATH_VARIABLE_DELIMITER + "*")) {
            st = st.replace(PATH_VARIABLE_DELIMITER + "*", "");
        }
        return st;
    }

    private static Object[] checkDelimiter(String st) {
        Object[] message = new Object[2];
        List<String> chars = new ArrayList<>();

        if (!st.matches("[\\p{L}\\p{N}\\\\p{\\d.+}\\s_\\-<>]+")) {
            // contains special chars
            if (st.contains("::")) {
                message[0] = "::";
                message[1] = true;
                return message;
            }

            String regex = "[^a-zA-Z0-9\\[\\]]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(st);
            while (matcher.find()) chars.add(matcher.group());

            for (String ch : chars) {
                if (!ch.equals(PATH_VARIABLE_DELIMITER)) {
                    message[0] = ch;
                    message[1] = true;
                    return message;
                }
            }
        }
        message[0] = "";
        message[1] = true;
        return message;
    }

    private static boolean controlDelimiter(final String inputString) {
        var controlledString = checkDelimiter(inputString);
        if (!((boolean) controlledString[1])) {
            LoggingUtil.warn("\n  \t\t\t&f- &cThe path-delimiter in the script is different from what is set in SkJson's config. \n  \t\t\t&f- Error node: &c" + inputString + " \n  \t\t\t&f- Wrong delimiter &c" + false);
            return false;
        }
        return true;
    }

    /**
     * Extract keys to list linked list.
     *
     * @param inputString    the string
     * @param inputDelimiter the delimiter
     * @param add            the raw adding
     * @return Queue<String>
     */

    public static LinkedList<String> extractKeysToList(String inputString, String inputDelimiter, boolean add) {
        final LinkedList<String> extractedKeys = new LinkedList<>();

        if (inputString == null) return extractedKeys;

        if (controlDelimiter(inputString)) {
            String sanitizedString = sanitizeString(inputString);
            String sanitizedDelimiter = sanitizeDelimiter(inputDelimiter == null ? PATH_VARIABLE_DELIMITER + "(?![{}])" : inputDelimiter);

            try {
                String _key = null, _index = null;
                for (String item : sanitizedString.split(sanitizedDelimiter)) {
                    Matcher squares = subPattern.matcher(item);
                    Matcher number = insideSquareBrackets.matcher(item);

                    if (squareBrackets.matcher(item).find()) {
                        while (squares.find()) if (squares.group(1) != null) _key = squares.group(1);
                        while (number.find()) {
                            if (number.group(1) != null) {
                                String n1 = number.group(1);
                                if (!(n1.isBlank() || n1.isEmpty())) {
                                    _index = String.valueOf(parseNumber(number.group(1)));
                                    if (parseNumber(_index) < 0) _index = "0";
                                }
                            }
                        }

                        extractedKeys.add(add ? _key + "->List" : _key);
                        if (_index != null) extractedKeys.add(_index);
                    } else {
                        extractedKeys.add(item);
                    }
                }
            } catch (Exception ex) {
                LoggingUtil.enchantedError(ex, ex.getStackTrace(), "Utils:177");
            } finally {
                if (LOGGING_LEVEL > 3 && PROJECT_DEBUG)
                    LoggingUtil.log("Before transform " + inputString + " \nAfter transform " + extractedKeys);
            }
        }
        return extractedKeys;
    }

    public static LinkedList<String> extractKeysToList(String inputString, String inputDelimiter) {
        return PatternUtil.extractKeysToList(inputString, inputDelimiter, false);
    }
}
