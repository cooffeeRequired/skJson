package cz.coffee.core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Saturday (3/4/2023)
 */
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
