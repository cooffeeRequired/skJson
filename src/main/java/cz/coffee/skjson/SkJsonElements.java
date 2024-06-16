package cz.coffee.skjson;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SkJsonElements {
    protected static final Map<String, ArrayList<String>> SkjsonElements = new HashMap<>(Map.of(
            "Expressions", new ArrayList<>(),
            "Events", new ArrayList<>(),
            "Effects", new ArrayList<>(),
            "Sections", new ArrayList<>(),
            "Structures", new ArrayList<>(),
            "Functions", new ArrayList<>(),
            "Conditions", new ArrayList<>())
    );

    public static <E extends Effect> void registerEffect(Class<E> c, String... patterns) {
        SkjsonElements.get("Effects").add(c.toString());
        for (int i = 0; i < patterns.length; i++) patterns[i] = "[skJson] " + patterns[i];
        Skript.registerEffect(c, patterns); //
    }

    public static <T> void registerProperty(Class<? extends Expression<T>> expressionClass, Class<T> type, String property, String fromType) {
        SkjsonElements.get("Expressions").add(expressionClass.toString());
        Skript.registerExpression(expressionClass, type, ExpressionType.PROPERTY, "[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property);
    }

    public static <E extends Expression<T>, T> void registerExpression(Class<E> c, Class<T> returnType, ExpressionType type, String... patterns) {
        SkjsonElements.get("Expressions").add(c.toString());
        for (int i = 0; i < patterns.length; i++) patterns[i] = "[skJson] " + patterns[i];
        Skript.registerExpression(c, returnType, type, patterns);
    }

    public static <T> void registerPropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
        SkjsonElements.get("Expressions").add(c.toString());
        PropertyExpression.register(c, returnType, property, fromType);
    }

    public static <T> void registerSimplePropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
        PropertyExpression.register(c, returnType, property, fromType);
    }

    public static void registerEvent(String name, Class<? extends SkriptEvent> c, Class<? extends Event> event, String description, String examples, String version, String... patterns) {
        SkjsonElements.get("Events").add(name);
        Skript.registerEvent(name, c, event, patterns)
                .since(version)
                .examples(examples)
                .description(description);
    }

    public static <E extends Condition> void registerCondition(Class<E> c, String... patterns) {
        SkjsonElements.get("Conditions").add(c.toString());
        for (int i = 0; i < patterns.length; i++) patterns[i] = "[skJson] " + patterns[i];
        Skript.registerCondition(c, patterns);
    }

    public static <E extends Section> void registerSection(Class<E> requestClass, String... patterns) {
        SkjsonElements.get("Sections").add(requestClass.toString());
        for (int i = 0; i < patterns.length; i++) patterns[i] = "[skJson] " + patterns[i];
        Skript.registerSection(requestClass, patterns);
    }

    public static JavaFunction<?> registerFunction(JavaFunction<?> fn) {
        SkjsonElements.get("Functions").add(fn.toString());
        return Functions.registerFunction(fn);
    }
}