package cz.coffeerequired.api.json;

/*
 * ! {"key": "value", "nested": {"key1": "v1"}, "n1": [{"k2": "v3"}]}}
 *
 * when a number appears in the path, the handling need by handle as like for JSON array
 *
 * update {current, nested, root}[key, value]
 * create {current, nested, new}[value ^ key]
 * delete {current, nested}[value, key ^ value]
 * get {current, nested}[value]
 *? - parser.getNested("nested.key1", Type.Key) == v1
 *  - parser.getNested("n1.0.k2", Type.Key) == v3
 *  - parser.get("nested", Type.Key) == {"key1: "v1"}
 *
 * // KEYS
 * - parser.getNested("v1", Type.Value) == key1
 * - parser.get("key",. Type.Value) == value
 *
 * {"new": {"new": []}}
 * {"new": {"new": [1]}}
 * - parser.create("new.new[]", 1)
 *
 * {}
 * {"new": {"raw": 1}}
 * - parser.create("new.raw", 1)
 */

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
