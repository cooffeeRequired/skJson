package cz.coffee.core;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

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
