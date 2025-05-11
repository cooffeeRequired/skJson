package cz.coffeerequired.api.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.support.Performance;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.coffeerequired.api.Api.Records.PROJECT_DELIM;

public class PathParser {
    private static final ConcurrentHashMap<String, ArrayList<Map.Entry<String, Type>>> TOKEN_CACHE = new ConcurrentHashMap<>();
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[\\d+]");
    private static final Pattern ARRAY_ANY_PATTERN = Pattern.compile("\\[]$");
    private static final Pattern STRING_PATTERN = Pattern.compile("([\\W\\w]+)");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");
    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile(".+\\[\\d+]");
    private static final Pattern LIST_ALL_PATTERN = Pattern.compile("\\*$");
    private static final Pattern LIST_INIT_PATTERN = Pattern.compile("\\[]$");

    private static ArrayList<Map.Entry<String, Type>> getTokens(String path, String delim) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        String cacheKey = path + delim;
        ArrayList<Map.Entry<String, Type>> cachedTokens = TOKEN_CACHE.get(cacheKey);
        if (cachedTokens != null) {
            return new ArrayList<>(cachedTokens);
        }

        String[] tokens = path.split(Pattern.quote(delim));
        ArrayList<Map.Entry<String, Type>> tokensList = new ArrayList<>(tokens.length);

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];
            List<Object> typeResult = getType(i, tokens, currentToken);
            
            if (typeResult.size() == 2) {
                processSingleType(tokensList, typeResult);
            } else if (typeResult.size() > 2) {
                processMultipleTypes(tokensList, typeResult);
            }
        }

        TOKEN_CACHE.put(cacheKey, new ArrayList<>(tokensList));
        return tokensList;
    }

    private static void processSingleType(ArrayList<Map.Entry<String, Type>> tokensList, List<Object> typeResult) {
        Type type = (Type) typeResult.get(0);
        String token = (String) typeResult.get(1);
        token = cleanToken(token);
        tokensList.add(Map.entry(token, type));
    }

    private static void processMultipleTypes(ArrayList<Map.Entry<String, Type>> tokensList, List<Object> typeResult) {
        for (int x = 0; x < typeResult.size(); x += 2) {
            Type type = (Type) typeResult.get(x);
            String token = (String) typeResult.get(x + 1);
            token = cleanToken(token);
            tokensList.add(Map.entry(token, type));
        }
    }

    private static String cleanToken(String token) {
        return token.replaceFirst("\\*", "")
                   .replaceFirst("\\[]", "");
    }

    @SuppressWarnings("unused")
    public static Iterator<Map.Entry<String, Type>> tokenizeIterator(String path, String delim) {
        return getTokens(path, delim).iterator();
    }

    public static ArrayList<Map.Entry<String, Type>> tokenize(String path, String delim) {
        SkJson.debug("Tokenizing path: %s", path);
        ArrayList<Map.Entry<String, Type>> tokens;
        if (Api.Records.PROJECT_DEBUG) {
            var perf = new Performance();
            perf.start();
            tokens = getTokens(path, delim);
            perf.stop();
            SkJson.debug("&e[form path] tokens: %s, time: %s", tokens, perf.toHumanTime());
        } else {
            tokens = getTokens(path, delim);
        }
        return tokens;
    }

    private static @NotNull List<Object> getType(int i, String[] tokens, String currentToken) {
        if (currentToken == null || currentToken.isEmpty()) {
            return List.of();
        }

        String previousToken = i - 1 >= 0 ? tokens[i - 1] : null;

        if (LIST_INIT_PATTERN.matcher(currentToken).find()) {
            return List.of(Type.ListInit, currentToken);
        }
        if (LIST_ALL_PATTERN.matcher(currentToken).find()) {
            return List.of(Type.ListAll, currentToken);
        }

        try {
            if (ARRAY_INDEX_PATTERN.matcher(currentToken).matches()) {
                return processArrayIndex(currentToken);
            }

            if (STRING_PATTERN.matcher(currentToken).matches()) {
                return processWordToken(currentToken, previousToken);
            }

            if (NUMERIC_PATTERN.matcher(currentToken).matches()) {
                return processNumericToken(currentToken, previousToken);
            }
        } catch (Exception e) {
            SkJson.exception(e, "Error processing token: %s", currentToken);
            return List.of(Type.Object, currentToken);
        }

        return List.of();
    }

    private static List<Object> processArrayIndex(String currentToken) {
        int indexOfNumber = currentToken.indexOf('[');
        String string = currentToken.substring(0, indexOfNumber);
        String index = currentToken.substring(indexOfNumber + 1, currentToken.length() - 1);
        return List.of(Type.List, string, Type.Index, index);
    }

    private static List<Object> processWordToken(String currentToken, @Nullable String previousToken) {
        boolean isNumeric = JsonAccessorUtils.isNumeric(currentToken) != null;
        boolean isPreviousNumericOrArray = previousToken != null && 
            (NUMERIC_PATTERN.matcher(previousToken).matches() || ARRAY_INDEX_PATTERN.matcher(previousToken).matches());

        if (isNumeric && isPreviousNumericOrArray) {
            return List.of(Type.List, currentToken);
        }
        return List.of(Type.Key, currentToken);
    }

    private static List<Object> processNumericToken(String currentToken, @Nullable String previousToken) {
        boolean isPreviousObject = previousToken != null && STRING_PATTERN.matcher(previousToken).matches();
        return List.of(isPreviousObject ? Type.Key : Type.Index, currentToken);
    }

    public static ArrayList<Map.Entry<String, Type>> tokenizeFromPattern(String path) {
        if (isQuoted(path)) path = path.substring(1, path.length() - 1);
        ArrayList<Map.Entry<String, Type>> tokens;
        if (Api.Records.PROJECT_DEBUG) {
            var perf = new Performance();
            perf.start();
            tokens = getTokens(convertPath(path), PROJECT_DELIM);
            perf.stop();
            SkJson.debug("&e[from pattern] tokens: %s, time: %s", tokens, perf.toHumanTime());
        } else {
            tokens = getTokens(convertPath(path), PROJECT_DELIM);
        }
        return tokens;
    }

    public static String getPathFromTokens(ArrayList<Map.Entry<String, Type>> tokens) {
        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, Type> token : tokens) {
            if (!output.isEmpty()) output.append(PROJECT_DELIM);
            if (token.getValue() == Type.ListInit) output.append("[]");
            else if (token.getValue() == Type.ListAll) output.append("*");
            else output.append(token.getKey());
        }
        return output.toString();
    }

    private static boolean isQuoted(String s) {
        return s.startsWith("\"") && s.endsWith("\"");
    }

    private static String convertPath(String cleanedInput) {
        StringBuilder output = new StringBuilder();

        String[] segments = cleanedInput.split("\\.");
        for (String segment : segments) {
            Matcher arrayMatcher = ARRAY_PATTERN.matcher(segment);
            if (arrayMatcher.find()) {
                if (segment.matches("\\[\\d+]") ) {
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
            else if (ARRAY_ANY_PATTERN.matcher(segment).find()) {
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

        Object(new JsonObject()),
        List(new JsonArray()),

        Key(""),
        Index(0),

        ListInit("[]$"),
        ListAll("*$");

        private final Object value;

        Type(Object value) {
            this.value = value;
        }
    }
}