package cz.coffee.skjson.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cz.coffee.skjson.utils.Logger.error;
import static cz.coffee.skjson.utils.Logger.times;

/**
 * The type Util.
 */
public abstract class Util {

    @FunctionalInterface
    @SuppressWarnings("unused")
    public interface call<T> {
        T _call();
    }


    public static String fstring(String message, Object... arguments) {
        return String.format(message, arguments);
    }

    public static String fstring(String m, boolean c, Object... a) {
        if (c) {
            return String.format(m + times("%s", a.length), a);
        } else {
            return fstring(m, a);
        }
    }


    /**
     * Parse number int.
     *
     * @param potentialNumber the potential number
     * @return the int
     */
    public static int parseNumber(Object potentialNumber) {
        try {
            return Integer.parseInt(potentialNumber.toString());
        } catch (NumberFormatException e) {
            error(e);
        }

        return -9999;
    }

    /**
     * Is number boolean.
     *
     * @param obj the obj
     * @return the boolean
     */
    public static boolean isNumber(Object obj) {
        return obj != null && obj.toString().matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Is increment boolean.
     *
     * @param inputs the inputs
     * @return the boolean
     */
    public static boolean isIncrement(Object @NotNull [] inputs) {
        Integer[] numbers;
        ArrayList<Integer> intArray = new ArrayList<>();
        for (Object input : inputs) {
            if (isNumber(input)) {
                intArray.add(Integer.parseInt(input.toString()));
            }
        }
        numbers = intArray.toArray(new Integer[0]);

        for (int i = 0; i < numbers.length - 1; i++) {
            if (numbers[i + 1] != numbers[i] + 1) {
                return false;
            }
        }
        return true;
    }
}