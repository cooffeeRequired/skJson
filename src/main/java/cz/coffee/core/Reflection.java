package cz.coffee.core;

import java.lang.reflect.Field;

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
