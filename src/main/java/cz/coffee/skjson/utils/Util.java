package cz.coffee.skjson.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * The type Util.
 */
public abstract class Util {

    public static String fstring(String message, Object ...arguments) {
        return String.format(message, arguments);
    }


    /**
     * Parse number int.
     *
     * @param potentialNumber the potential number
     * @return the int
     */
    public static int parseNumber(Object potentialNumber) {
        if (potentialNumber != null && potentialNumber.toString().matches("-?\\d+(\\.\\d+)?")) {
            return Integer.parseInt(potentialNumber.toString());
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

    public static String getNow() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }
}