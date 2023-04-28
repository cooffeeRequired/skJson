package cz.coffee.core;

import java.lang.reflect.Field;

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
 * Created: pátek (28.04.2023)
 */
public class Reflection {
    public static class Variables {
        public static void setCaseInsensitiveVariables(boolean value) {
            Class<?> cls = ch.njol.skript.variables.Variables.class;
            try {
                Field f = cls.getField("caseInsensitiveVariables");
                f.set(null, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
