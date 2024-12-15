package cz.coffeerequired.api.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
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
            Type type = getType(i, tokens, currentToken);

            currentToken = currentToken
                    .replaceFirst("\\*", "")
                    .replaceFirst("\\[]", "");

            tokensList.add(Map.entry(currentToken, type));
        }
        return tokensList;
    }

    @SuppressWarnings("unused")
    public static Iterator<Map.Entry<String, Type>> tokenizeIterator(String path, String delim) {
        return getTokens(path, delim).iterator();
    }

    public static ArrayList<Map.Entry<String, Type>> tokenize(String path, String delim) {
        return getTokens(path, delim);
    }

    private static @NotNull Type getType(int i, String[] tokens, String currentToken) {
        String previousToken = i - 1 >= 0 ? tokens[i - 1] : null;
        String nextToken = i + 1 < tokens.length ? tokens[i + 1] : null;
        String last = tokens[tokens.length - 1];
        Type type;

        if (currentToken.endsWith("[]")) {
            type = Type.ListInit;
        } else if (currentToken.endsWith("*")) {
            type = Type.ListAll;
        } else if (currentToken.matches("\\d+")) {
            type = Type.Index;
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
        return type;
    }

    public static ArrayList<Map.Entry<String, Type>> tokenizeFromPattern(String path) {
        String convertedPath = convertPath(path);
        return getTokens(convertedPath, PROJECT_DELIM);
    }

    private static String convertPath(String cleanedInput) {

        // Define patterns for array and object handling
        Pattern arrayPattern = Pattern.compile("\\[\\d+]");
        Pattern arrayAnyPattern = Pattern.compile("\\[]$");

        // StringBuilder to build output
        StringBuilder output = new StringBuilder();

        // Split the input string by '.' to handle each segment
        String[] segments = cleanedInput.split("\\.");
        for (String segment : segments) {
            // If the segment matches an array with a specific index
            Matcher arrayMatcher = arrayPattern.matcher(segment);
            if (arrayMatcher.find()) {
                String arrayName = segment.substring(0, segment.indexOf('['));
                String index = segment.substring(segment.indexOf('[') + 1, segment.indexOf(']'));
                if (!output.isEmpty()) {
                    output.append(PROJECT_DELIM);
                }
                output.append(arrayName).append(PROJECT_DELIM).append(index);
                if (segment.endsWith("*")) output.append("*");
            }
            // If the segment matches an array without an index (e.g., [])
            else if (arrayAnyPattern.matcher(segment).find()) {
                if (!output.isEmpty()) output.append(PROJECT_DELIM);
                output.append(segment);
            }
            // Otherwise, it's a regular object property
            else {
                if (!output.isEmpty()) output.append(PROJECT_DELIM);
                output.append(segment);
            }
        }

        return output.toString();
    }

    @Getter
    public enum Type {
        Index(0), List(new JsonArray()), Object(new JsonObject()), ListInit("[]$"), ListAll("*$");

        private final Object value;
        Type(Object value) {
            this.value = value;
        }
    }
}