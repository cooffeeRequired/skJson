package cz.coffeerequired.api.json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class SkriptJsonInputParser {

    private static ArrayList<Map.Entry<String, Type>> getTokens(String path, String delim) {
        if (path == null || path.isEmpty()) return null;
        String[] tokens = path.split(Pattern.quote(delim));
        ArrayList<Map.Entry<String, Type>> tokensList = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];
            Type type = getType(i, tokens, currentToken);
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


        if (currentToken.matches("\\d+")) {
            type = Type.Index;
        } else if (currentToken.endsWith("[]")) {
            type = Type.List;
        } else {
            if (currentToken.equals(last)) {
                if (currentToken.matches("\\d+")) {
                    type = Type.Index;
                } else {
                    type = Type.Object;
                }
            } else {
                if (previousToken != null && previousToken.matches("\\d+")) {
                    type = Type.List;
                } else if (nextToken != null && nextToken.matches("\\d+")) {
                    type = Type.List;
                } else {
                    type = Type.Object;
                }
            }
        }
        return type;
    }

    public enum Type { Index, List, Object }
}
