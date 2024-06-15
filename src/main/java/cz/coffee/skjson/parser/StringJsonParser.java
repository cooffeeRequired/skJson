package cz.coffee.skjson.parser;

import cz.coffee.skjson.utils.ConsoleColors;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// import static cz.coffee.skjson.utils.Logger.error;

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
    static final Pattern PATTER_SPLIT = Pattern.compile("(\"[^\"]+\"|\\w+)" + SPECIAL_REPLACER + "(?: ?)+([\\w{}():'+\\-\\\\,\"\\s]+)([,}])(?: ?)+");
    /**
     * The Pattern special colon.
     */
    static final String PATTERN_SPECIAL_COLON = "(?<=\\w)\\s*:(?=[^:])(?![^\"]*\":\"[^\"]*\")";
    /**
     * The Pattern special spl.
     */
    static final String PATTERN_SPECIAL_SPL = "(?<=\\w)\\s*" + SPECIAL_REPLACER + "(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    /**
     * The Case function regex.
     */
    static final Pattern CASE_FUNCTION_REGEX = Pattern.compile("\\s*\\w+\\(([\\w\\W]|)+\\)");
    /**
     * The Case simple variable regex.
     */
    static final Pattern CASE_SIMPLE_VARIABLE_REGEX = Pattern.compile("\\{[A-z*:0-9]+}");
    /**
     * The Case simple expression regex.
     */
    static final Pattern CASE_SIMPLE_EXPRESSION_REGEX = Pattern.compile("([A-z-0-9\\s\\W]+)");
    /**
     * The Case unknown expression regex.
     */
    static final Pattern CASE_UNKNOWN_EXPRESSION_REGEX = Pattern.compile("\\([\\d+.\\-*?]+\\)");
    /**
     * The Pattern array.
     */
    static final Pattern PATTERN_ARRAY = Pattern.compile("\\[.*?]");

    static final Pattern PATTERN_OBJECT = Pattern.compile("\\{.*}");

    /**
     * The Valued arrays.
     */
    static final HashMap<String, String> valuedArrays = new HashMap<>();

    /**
     * Parse input string.
     *
     * @param input    the input
     * @param argv the finished
     * @return the string
     */

    public static String parseInput(String input, boolean ...argv) {
        var finished = argv != null && argv.length > 0 && argv[0];
        if (finished) {
            return finishParsing(input);
        } else {
            try {
                input = input.replaceAll(PATTERN_SPECIAL_COLON, SPECIAL_REPLACER);
                Matcher m = PATTER_SPLIT.matcher(input);
                Matcher array_matcher = PATTERN_ARRAY.matcher(input);

                while (array_matcher.find()) {
                    var matched = array_matcher.group();
                    //System.out.println("Matched: " + matched);
                    if (!matched.isEmpty()) {
                        //System.out.println("parseInput-array [matched]: " + matched);
                        //System.out.println("M: "+ matched);
                        //System.out.println(".... => " + input);
                        String evaluated = evaluateArray(matched.substring(1, matched.length() -1));
                        //System.out.println("Evaluated: " + evaluated);
                        input = input.replace(matched, evaluated);
                    }
                }


                while (m.find()) {
                    var v = m.group(2).trim();
                    var stringCase = getValueCase(v);
                    var start_index = input.indexOf(v);
                    var end_index = start_index + v.length();
                    switch (stringCase) {
                        case VARIABLE, EXPRESSION, FUNCTION, EXPRESSION_CASE -> {
                            if (start_index != -1) {
                                input = input.substring(0, start_index) + makeQuoted(v) + input.substring(end_index);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //error(e);
                input += ConsoleColors.RED + " -> @has_error: " + e.getMessage() + ConsoleColors.RESET;
                e.printStackTrace();
            } finally {
                input = parseInput(input, true);
            }
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
        //System.out.println(v);
        //System.out.println(ConsoleColors.RED + out + ConsoleColors.RESET);
        return v.startsWith("%") && v.endsWith("%") ? v.trim() : '%' + v.trim() + '%';
    }

    /**
     * Evaluate array string.
     *
     * @param input the input
     * @return the string
     */
    static String evaluateArray(String input) {

//        System.out.println("evaluateArray [input]: " + input);
//        System.out.println("evaluateArray [i]: " + i);

        TreeMap<String, String> __map = new TreeMap<>();

        Matcher vars = CASE_SIMPLE_VARIABLE_REGEX.matcher(input);
        Matcher funcs = CASE_FUNCTION_REGEX.matcher(input);
        int z = 0;
        while (funcs.find()) {
            String group = funcs.group().trim();
            input = input.replace(group, "__FUN"+z);
            __map.put("__FUN"+z, makeQuoted(group));
            z++;
        }

        while (vars.find()) {
            String group = vars.group().trim();
            input = input.replace(group, "_VAR"+z);
            __map.put("_VAR"+z, makeQuoted(group));
        }


        //return input;


        Matcher m = PATTERN_OBJECT.matcher(input);
        int x = 0;
        while (m.find()) {
            String group = m.group();
            int groupLength = group.length();
            int startIndex = input.indexOf(group);
            var unparsed = input.substring(startIndex, startIndex+groupLength-1);
            valuedArrays.put("@" + x, parseInput(unparsed));
            input = input.substring(0, startIndex) + "@"+x + input.substring( startIndex + groupLength-1);
            x++;
        }

        for (String expression : input.split(",")) {
            String proccesed = expression.trim();
            ExpressionCase case_ = getValueCase(expression);
            //System.out.println("Case: " + case_ + " => " + proccesed);
            switch (case_) {
                case EXPRESSION -> {
                    if (!expression.startsWith("@")) proccesed = makeQuoted(expression);
                }
                case VARIABLE, FUNCTION, EXPRESSION_CASE -> proccesed = makeQuoted(expression);
            }
            input = input.replaceAll(expression, proccesed);
        }

        for (String expression : __map.keySet()) {
            input = input.replaceAll(expression, __map.get(expression));
        };
        //System.out.println(ConsoleColors.PURPLE + input + ConsoleColors.RESET);
        //System.out.println(ConsoleColors.CYAN + valuedArrays + ConsoleColors.RESET);
        return "[" + input + "]";
    }

    /**
     * Finish parsing string.
     *
     * @param input the v
     * @return the string
     */
    static String finishParsing(String input) {
        if (!StringJsonParser.valuedArrays.isEmpty()) {
            for (var entry : StringJsonParser.valuedArrays.entrySet()) {
                input = input.replaceAll(entry.getKey(), entry.getValue());
            }
        }

        return input.replaceAll("%%", "%").replaceAll(PATTERN_SPECIAL_SPL, ":");
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
        } else if (v.trim().startsWith("_VAR")) {
            return ExpressionCase.UNKNOWN;
        } else if (v.matches("(true|false)")) {
            return ExpressionCase.BOOLEAN;
        } else if (v.matches("(\\d+(\\.\\d+)?)")) {
            return ExpressionCase.NUMBER;
        } else {
            if (CASE_FUNCTION_REGEX.matcher(v).matches() || v.startsWith("__FUN")) {
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
