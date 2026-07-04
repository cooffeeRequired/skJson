package cz.coffeerequired.api.json;

import java.util.regex.Pattern;

/**
 * Strips {@code .jsonc} comments and trailing commas before Gson parsing.
 */
public final class JsonComments {

    private static final Pattern TRAILING_COMMA = Pattern.compile(",\\s*([}\\]])");

    private JsonComments() {
    }

    public static String prepare(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return TRAILING_COMMA.matcher(stripComments(input)).replaceAll("$1");
    }

    static String stripComments(String input) {
        StringBuilder out = new StringBuilder(input.length());
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inString) {
                out.append(c);
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }

            if (c == '/' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == '/') {
                    i += 2;
                    while (i < input.length() && input.charAt(i) != '\n') {
                        i++;
                    }
                    continue;
                }
                if (next == '*') {
                    i += 2;
                    while (i + 1 < input.length()) {
                        if (input.charAt(i) == '*' && input.charAt(i + 1) == '/') {
                            i++;
                            break;
                        }
                        i++;
                    }
                    continue;
                }
            }

            out.append(c);
        }

        return out.toString();
    }
}
