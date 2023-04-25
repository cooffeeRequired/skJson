package cz.coffee.core.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NumberUtils {
    public static boolean isNumber(Object obj) {
        return obj != null && obj.toString().matches("-?\\d+(\\.\\d+)?");
    }

    public static int parsedNumber(Object obj) {
        if (obj != null && obj.toString().matches("-?\\d+(\\.\\d+)?")) {
            return Integer.parseInt(obj.toString());
        }
        return -1;
    }

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
