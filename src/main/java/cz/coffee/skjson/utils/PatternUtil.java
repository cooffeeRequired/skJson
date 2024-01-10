package cz.coffee.skjson.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffee.skjson.api.ConfigRecords.PATH_VARIABLE_DELIMITER;
import static cz.coffee.skjson.utils.Logger.error;
import static cz.coffee.skjson.utils.Logger.warn;
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
            warn("\n  \t\t\t&f- &cThe path-delimiter in the script is different from what is set in SkJson's config. \n  \t\t\t&f- Error node: &c" + inputString + " \n  \t\t\t&f- Wrong delimiter &c" + false);
            return false;
        }
        return true;
    }

    public enum KeyType {LIST, KEY}

    public record keyStruct(String key, KeyType type) {
        public boolean isList() {
            return this.type == KeyType.LIST;
        }
    }

    public static LinkedList<keyStruct> convertStringToKeys(String inputString) {
        return convertStringToKeys(inputString, PATH_VARIABLE_DELIMITER + "(?![{}])");
    }

    public static LinkedList<keyStruct> convertStringToKeys(String inputString, String inputDelimiter) {
        return convertStringToKeys(inputString, inputDelimiter, false);
    }

    public static LinkedList<keyStruct> convertStringToKeys(String inputString, String inputDelimiter, boolean add) {
        final LinkedList<keyStruct> keys = new LinkedList<>();
        if (inputString == null) return keys;
        if (!controlDelimiter(inputString)) return keys;

        String string = sanitizeString(inputString);
        String delimiter = sanitizeDelimiter(inputDelimiter == null ? PATH_VARIABLE_DELIMITER + "(?![{}])" : inputDelimiter);

        try {
            String _key = null, _index = null;
            for (String item : string.split(delimiter)) {
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
                    keys.add(add ? new keyStruct(_key, KeyType.LIST) : new keyStruct(_key, KeyType.KEY));
                    if (_index != null) keys.add(new keyStruct(_index, KeyType.KEY));
                } else {
                    keys.add(new keyStruct(item, KeyType.KEY));
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
        return keys;
    }
}
