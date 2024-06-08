package cz.coffee.skjson.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cz.coffee.skjson.api.ConfigRecords.PATH_VARIABLE_DELIMITER;

public abstract class PatternUtil {

    public enum KeyType {LIST, KEY, DELIMITER, INDEX}

    public record keyStruct(String key, KeyType type) {
        public boolean isList() {
            return this.type == KeyType.LIST;
        }
    }

    private static final List<String> banned = new ArrayList<>(List.of("$", "#", "^", "\\/", "[", "]", "{", "}", "_", "-"));

    public static void remap(List<keyStruct> keys, boolean withDelim) {
        for (var e = 0; e < keys.size(); e++) {
            var struct = keys.get(e);
            if (withDelim ? (e - 2) > 0 : (e - 1) > 0) {
                var pre = keys.get(withDelim ? (e - 2) : (e - 1));
                if (pre != null) {
                    try {
                        Integer.parseInt(struct.key());
                        keys.set(withDelim ? (e - 2) : (e - 1), new keyStruct(pre.key(), KeyType.LIST));
                    } catch (NumberFormatException ignored) {
                    }
                }
            } else if (withDelim ? (e - 2) < 0 : (e - 1) < 0) {
                if (withDelim ? (e + 2) < keys.size() : (e + 1) < keys.size()) {
                    var next = keys.get(withDelim ? (e + 2) : (e + 1));
                    struct = keys.get(e);
                    if (!(next.type().equals(KeyType.DELIMITER) && struct.type().equals(KeyType.DELIMITER))) {
                        try {
                            Integer.parseInt(next.key());
                            keys.set(e, new keyStruct(struct.key(), KeyType.LIST));
                            keys.set(withDelim ? (e + 2) : (e + 1), new keyStruct(next.key(), KeyType.INDEX));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }
    }

    public static LinkedList<keyStruct> convertStringToKeys(final String input) {
        return convertStringToKeys(input, null, false);
    }

    public static LinkedList<keyStruct> convertStringToKeys(final String input, boolean add) {
        return convertStringToKeys(input, null, add);
    }

    public static LinkedList<keyStruct> convertStringToKeys(final String input, String delimiter) {
        return convertStringToKeys(input, delimiter, false);
    }

    public static LinkedList<keyStruct> convertStringToKeys(final String input, String delimiter, boolean add) {
        if (delimiter == null) delimiter = PATH_VARIABLE_DELIMITER;
        if (banned.contains(delimiter)) {
            Logger.simpleError("&cYou using not allowed delimiter.. Banned delimiters are &e'%s'", String.join(" ,", banned));
            return new LinkedList<>();
        }
        return tokenize(input, false, add, delimiter);
    }

    private static LinkedList<keyStruct> tokenize(String input, boolean withDelim, boolean add, String delimiter) {
        LinkedList<keyStruct> keyStructs = new LinkedList<>();
        StringBuilder currentToken = new StringBuilder();
        if (input == null) return keyStructs;
        boolean inBrackets = false;
        char[] inputChars = input.toCharArray();
        for (int i = 0; i < inputChars.length; i++) {
            char currentChar = inputChars[i];
            if (currentChar == '[') {
                keyStructs.add(new keyStruct(currentToken.toString(), KeyType.KEY));
                currentToken.setLength(0);
                inBrackets = true;
                currentToken.append(currentChar);
            } else if (currentChar == ']') {
                if (inBrackets) {
                    currentToken.append(currentChar);
                    inBrackets = false;
                    if (currentToken.toString().startsWith("[") && currentToken.toString().endsWith("]")) {
                        String token = currentToken.toString();
                        currentToken.setLength(0);
                        currentToken.append(token, 1, token.length() - 1);
                        if (withDelim) keyStructs.add(new keyStruct(delimiter, KeyType.DELIMITER));
                    } else {
                        if (!currentToken.isEmpty()) {
                            String value = currentToken.toString();
                            keyStructs.add(new keyStruct(value, KeyType.KEY));
                            currentToken.setLength(0);
                            if (add) {
                                remap(keyStructs, withDelim);
                            }
                        }
                    }
                }
            } else if (currentChar == delimiter.charAt(0)) {
                if (input.startsWith(delimiter, i)) {
                    if (!currentToken.isEmpty()) {
                        String value = currentToken.toString();
                        keyStructs.add(new keyStruct(value, KeyType.KEY));
                        currentToken.setLength(0);
                        i += delimiter.length() - 1; // Skip the rest of the delimiter
                    } else {
                        currentToken.append(currentChar);
                    }
                } else {
                    currentToken.append(currentChar);
                }
            } else {
                currentToken.append(currentChar);
            }
        }
        if (!currentToken.isEmpty()) {
            keyStructs.add(new keyStruct(currentToken.toString(), KeyType.KEY));
        }
        return keyStructs;
    }
}
