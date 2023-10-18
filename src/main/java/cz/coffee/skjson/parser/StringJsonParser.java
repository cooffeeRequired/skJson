package cz.coffee.skjson.parser;

import cz.coffee.skjson.utils.Util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type String json parser.
 */
public abstract class StringJsonParser {
    /**
     * The Special replacer.
     */
    static final String SPECIAL_REPLACER = "⁞";
    /**
     * The Patter split.
     */
    static final Pattern PATTER_SPLIT = Pattern.compile("(\"[^\"]+\"|\\w+)"+SPECIAL_REPLACER+"(?: ?)+([\\w{}():'+\\-\\\\,\"\\s]+)([,}])(?: ?)+");
    /**
     * The Pattern special colon.
     */
    static final String PATTERN_SPECIAL_COLON = "(?<=\\w)\\s*:(?=[^:])(?![^\"]*\":\"[^\"]*\")";
    /**
     * The Pattern special spl.
     */
    static final String PATTERN_SPECIAL_SPL = "(?<=\\w)\\s*"+SPECIAL_REPLACER+"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    /**
     * The Case function regex.
     */
    static final Pattern CASE_FUNCTION_REGEX = Pattern.compile("\\s*\\w+\\(([\\w\\W]|)+\\)");
    /**
     * The Case simple variable regex.
     */
    static final Pattern CASE_SIMPLE_VARIABLE_REGEX = Pattern.compile("\\{[A-z_*:0-9]+}");
    /**
     * The Case simple expression regex.
     */
    static final Pattern CASE_SIMPLE_EXPRESSION_REGEX = Pattern.compile("([A-z-Z-a-0-9\\s\\W]+)");
    /**
     * The Case unknown expression regex.
     */
    static final Pattern CASE_UNKNOWN_EXPRESSION_REGEX = Pattern.compile("\\([\\d+.\\-*?]+\\)");
    /**
     * The Pattern array.
     */
    static final Pattern PATTERN_ARRAY = Pattern.compile("\\[.*?]");

    /**
     * The Valued arrays.
     */
    static final HashMap<String,String> valuedArrays = new HashMap<>();

    /**
     * Parse input string.
     *
     * @param input    the input
     * @param finished the finished
     * @return the string
     */
    public static String parseInput(String input, boolean finished) {
        try {
            input = input.replaceAll(PATTERN_SPECIAL_COLON, SPECIAL_REPLACER);
            Matcher m = PATTER_SPLIT.matcher(input);
            Matcher array_matcher = PATTERN_ARRAY.matcher(input);

            int i = 0;
            while (array_matcher.find()) {
                var g = array_matcher.group();
                var array = Arrays.stream(g.substring(1, g.length()-1).split("},\\s(\\{)")).toList();
                input = evaluateArray(input, array, i);
                i++;
            }
            while(m.find()) {
                var v = m.group(2).trim();
                var stringCase = getValueCase(v);
                var start_index = input.indexOf(v);
                var end_index = start_index + v.length();
                switch (stringCase) {
                    case VARIABLE, EXPRESSION, FUNCTION, EXPRESSION_CASE-> {
                        if (start_index != -1) {
                            input=input.substring(0, start_index) + makeQuoted(v) + input.substring(end_index);
                        }}
                }
            }
        } catch (Exception e) {
            Util.enchantedError(e, e.getStackTrace(), "StringJsonParser (33)");
        }
        if (finished) {
            if (!valuedArrays.isEmpty()) {
                for (var entry : valuedArrays.entrySet()) {
                    input = input.replaceAll(entry.getKey(), entry.getValue());
                }
            }
            return finishParsing(input);
        } else {
            return input;
        }
    }

    /**
     * Make quoted string.
     *
     * @param v the v
     * @return the string
     */
    static String makeQuoted(final String v) {
        return v.startsWith("%") && v.endsWith("%") ? v : '%' + v + '%';
    }

    /**
     * Evaluate array string.
     *
     * @param input the input
     * @param array the array
     * @param i     the
     * @return the string
     */
    static String evaluateArray(String input, List<String> array, int i) {
        if (!array.isEmpty()) {
            for (var value : array) {
                if (value.charAt(0) != '{') value = "{" + value;
                if (value.charAt(0) == '{' && value.charAt(value.length()-1) != '}')  value += "}";

                var start_index = input.indexOf(value);
                var end_index = start_index + value.length();
                input = input.substring(0, start_index) + "@"+i + input.substring(end_index);
                var parsed_value = parseInput(value, false);
                valuedArrays.put("@"+i, parsed_value);
                i++;
            }
        }
        return input;
    }

    /**
     * Finish parsing string.
     *
     * @param v the v
     * @return the string
     */
    static String finishParsing(String v) {
        v = v.replaceAll("%%", "%");
        return v.replaceAll(PATTERN_SPECIAL_SPL, ":");
    }
    /**
     * Gets value case.
     *
     * @param v the v
     * @return the value case
     */
    public static ExpressionCase getValueCase(String v) {
        if (v.startsWith("\"") && v.endsWith("\"")) {
            return ExpressionCase.STRING;
        } else if (v.matches("(true|false)")) {
            return ExpressionCase.BOOLEAN;
        } else if (v.matches("(\\d+(\\.\\d+)?)")) {
            return ExpressionCase.NUMBER;
        } else {
            if (CASE_FUNCTION_REGEX.matcher(v).matches()) {
                return ExpressionCase.FUNCTION;
            } else if (CASE_SIMPLE_VARIABLE_REGEX.matcher(v).matches()) {
                return ExpressionCase.VARIABLE;
            } else if (CASE_SIMPLE_EXPRESSION_REGEX.matcher(v).matches()) {
                int c = 0;
                int c1 = 0;
                if (v.contains("{") || v.contains("}")) {
                    for (var ch : v.split("")) {
                        if (Objects.equals(ch, "{")) c++;
                        if (Objects.equals(ch, "}")) c1++;
                    }
                    return c != c1 ? ExpressionCase.UNKNOWN : ExpressionCase.EXPRESSION;
                }
                return ExpressionCase.EXPRESSION;
            } else if (CASE_UNKNOWN_EXPRESSION_REGEX.matcher(v).matches()) {
                return ExpressionCase.EXPRESSION_CASE;
            }
        }
        return ExpressionCase.UNKNOWN;
    }
}
