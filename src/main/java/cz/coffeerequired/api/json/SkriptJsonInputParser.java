package cz.coffeerequired.api.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffeerequired.api.Api.Records.PROJECT_DELIM;

public class SkriptJsonInputParser {

    private static ArrayList<Map.Entry<String, Type>> getTokens(String path, String delim) {
        if (path == null || path.isEmpty()) return null;
        String[] tokens = path.split(Pattern.quote(delim));
        ArrayList<Map.Entry<String, Type>> tokensList = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];
            Type type;
            var rsp = getType(i, tokens, currentToken);
            if (rsp.size() == 2) {
                type = (Type) rsp.get(0);
                currentToken = (String) rsp.get(1);

                currentToken = currentToken
                        .replaceFirst("\\*", "")
                        .replaceFirst("\\[]", "");

                tokensList.add(Map.entry(currentToken, type));
            } else if (rsp.size() > 2) {
                for (int x = 0; x < rsp.size(); x = x + 2) {
                    type = (Type) rsp.get(x);
                    currentToken = (String) rsp.get(x + 1);

                    currentToken = currentToken
                            .replaceFirst("\\*", "")
                            .replaceFirst("\\[]", "");

                    tokensList.add(Map.entry(currentToken, type));
                }
            }

        }
        return tokensList;
    }

    @SuppressWarnings("unused")
    public static Iterator<Map.Entry<String, Type>> tokenizeIterator(String path, String delim) {
        return getTokens(path, delim).iterator();
    }

    public static ArrayList<Map.Entry<String, Type>> tokenize(String path, String delim) {
        SkJson.info("Tokenizing path: %s", path);
        var tokens = getTokens(path, delim);
        SkJson.debug("Tokens: %s", tokens);

        return tokens;
    }

    private static @NotNull List<Object> getType(int i, String[] tokens, String currentToken) {
        String previousToken = i - 1 >= 0 ? tokens[i - 1] : null;
        String nextToken = i + 1 < tokens.length ? tokens[i + 1] : null;
        String last = tokens[tokens.length - 1];
        Type type;

        if (currentToken.endsWith("[]")) {
            type = Type.ListInit;
        } else {
            boolean b = nextToken != null && !nextToken.matches("\\d+") && !nextToken.matches("\\d+\\*");
            if (currentToken.matches(".+\\[\\d+]")) {
                try {
                    var indexOfNumber = currentToken.indexOf('[');
                    var string = currentToken.substring(0, indexOfNumber);
                    currentToken = currentToken.substring(indexOfNumber + 1, currentToken.length() - 1);

                    // Kontrola, zda je následující token písmeno (ne číslo)
                    if (b) {
                        // Pokud je následující token písmeno, pak je to ListObject, ne index
                        return List.of(Type.List, string, Type.ListObject, currentToken);
                    } else {
                        return List.of(Type.List, string, Type.Index, currentToken);
                    }
                } catch (NumberFormatException ignored) {
                    type = Type.Object;
                }
            } else if (currentToken.endsWith("*")) {
                type = Type.ListAll;
            } else if (currentToken.matches("\\d+")) {
                // Kontrola, zda je následující token písmeno (ne číslo)
                if (b) {
                    // Pokud je následující token písmeno, pak je to ListObject, ne index
                    type = Type.ListObject;
                } else {
                    // Pokud je token pouze číslo, je to index
                    type = Type.Index;
                }
            } else {
                if (currentToken.equals(last)) {
                    if (currentToken.matches("\\d+")) {
                        type = Type.Index;
                    } else if (currentToken.matches("\\d+\\*")) {
                        type = Type.List;
                    } else {
                        type = Type.Object;
                    }
                } else {
                    if (previousToken != null && (previousToken.matches("\\d+") || previousToken.matches("\\d+\\*"))) {
                        type = Type.List;
                    } else if (nextToken != null && (nextToken.matches("\\d+") || nextToken.matches("\\d+\\*"))) {
                        type = Type.List;
                    } else {
                        type = Type.Object;
                    }
                }
            }
        }
        return List.of(type, currentToken);
    }

    public static ArrayList<Map.Entry<String, Type>> tokenizeFromPattern(String path) {
        if (isQuoted(path)) path = path.substring(1, path.length() - 1);
        return getTokens(convertPath(path), PROJECT_DELIM);
    }

    private static boolean isQuoted(String s) {
        return s.startsWith("\"") && s.endsWith("\"");
    }

    private static String convertPath(String cleanedInput) {

        // Define patterns for array and object handling
        Pattern arrayPattern = Pattern.compile("\\[\\d+]");
        Pattern arrayAnyPattern = Pattern.compile("\\[]$");

        StringBuilder output = new StringBuilder();

        String[] segments = cleanedInput.split("\\.");
        for (String segment : segments) {
            Matcher arrayMatcher = arrayPattern.matcher(segment);
            if (arrayMatcher.find()) {
                if (segment.matches("\\[\\d+]")) {
                    String index = segment.substring(1, segment.length() - 1);
                    if (!output.isEmpty()) {
                        output.append(PROJECT_DELIM);
                    }
                    output.append(index);
                } else {
                    String arrayName = segment.substring(0, segment.indexOf('['));
                    String index = segment.substring(segment.indexOf('[') + 1, segment.indexOf(']'));
                    if (!output.isEmpty()) {
                        output.append(PROJECT_DELIM);
                    }
                    output.append(arrayName).append(PROJECT_DELIM).append(index);
                }
                if (segment.endsWith("*")) output.append("*");
            }
            else if (arrayAnyPattern.matcher(segment).find()) {
                if (!output.isEmpty()) output.append(PROJECT_DELIM);
                output.append(segment);
            }
            else {
                if (!output.isEmpty()) output.append(PROJECT_DELIM);
                output.append(segment);
            }
        }

        return output.toString();
    }

    @Getter
    public enum Type {
        Index(0), List(new JsonArray()), Key(""), Object(new JsonObject()), ListInit("[]$"), ListAll("*$"), ListObject(new JsonObject());

        private final Object value;

        Type(Object value) {
            this.value = value;
        }
    }
}
