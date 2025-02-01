package cz.coffeerequired.api;


import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.registrations.Classes;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.annotators.ExternalAPI;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.api.exceptions.ExtensibleThrowable;
import cz.coffeerequired.modules.Core;
import cz.coffeerequired.modules.HttpModule;
import cz.coffeerequired.support.AnsiColorConverter;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;

public class Register {

    static final String prefix = "[skjson]";
    static ArrayDeque<Class<? extends Extensible>> modules = new ArrayDeque<>();
    @Getter
    private static SkriptAddon addon;
    @Getter
    private final SkriptRegister skriptRegister = new SkriptRegister();

    @ExternalAPI
    public static <T extends Extensible> void registerModules(JavaPlugin plugin, Class<T> module) {
        try {
            if (module.isAnnotationPresent(Module.class) && Modifier.isPublic(module.getModifiers())) {
                Module annotation = module.getAnnotation(Module.class);
                String moduleName = annotation.module();
                String moduleVersion = annotation.version();

                SkJson.info(
                        "Registering module: %s%s&r%s version: %s%s&r" + AnsiColorConverter.RESET,
                        AnsiColorConverter.hexToAnsi("#47a5ff"), moduleName,
                        AnsiColorConverter.hexToAnsi("#8dff3f"), moduleVersion
                );
                modules.add(module);
            } else {
                throw new IllegalCallerException("Class what extends Extensible always need to be annotated by @Module");
            }
        } catch (Exception e) {
            SkJson.exception(e, e.getMessage());
        }
    }

    public static boolean isClassAvailable(Class<?> className) {
        try {
            Class.forName(className.getName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void tryRegisterSkript() {

        if (isSkriptAvailable()) {
            addon = Skript.registerAddon(SkJson.getInstance());
            addon.setLanguageFileDirectory("lang");
            SkJson.info("Hooking into Skript plugin... Hooks initialized.");
            SkJson.info("Trying register Skript addon...");
            SkJson.info("Trying register Skript elements...");

            registerModule(Core.class);

            if (Api.Records.PROJECT_ENABLED_HTTP) {
                registerModule(HttpModule.class);
            }

        } else {
            SkJson.severe("Skript plugin not detected.");
        }
    }

    private void printAllRegistered(Extensible m) {
        m.getLoadedElements().forEach((key, value) -> {
            if (value.isEmpty()) return;
            logElement(key, value.size());
        });
    }

    public <T> void registerNewHook(Class<T> tClass) {
        if (isClassAvailable(tClass) && tClass.getName().equals("ch.njol.skript.Skript")) {
            SkJson.info("Attempting to hook into Skript plugin...");
            tryRegisterSkript();
        } else {
            SkJson.severe("Unsupported hook class: %s", tClass.getName());
        }
    }

    private boolean isSkriptAvailable() {
        return isClassAvailable(Skript.class);
    }

    public <T extends Extensible> void registerModule(Class<T> module) {
        try {
            if (module.isAnnotationPresent(Module.class) && Modifier.isPublic(module.getModifiers())) {
                Module annotation = module.getAnnotation(Module.class);
                String moduleName = annotation.module();
                String moduleVersion = annotation.version();
                SkJson.info(
                        "Registering module: %s%s&r version: %s%s",
                        AnsiColorConverter.hexToAnsi("#47a5ff"), moduleName,
                        AnsiColorConverter.hexToAnsi("#8dff3f"), moduleVersion
                );
                try {
                    Extensible m = module.getDeclaredConstructor().newInstance();
                    m.load();
                    m.registerElements(this.getSkriptRegister());
                    printAllRegistered(m);
                } catch (ExtensibleThrowable | InstantiationException | IllegalAccessException |
                         InvocationTargetException |
                         NoSuchMethodException e) {
                    SkJson.exception(e, e.getMessage());
                }
            } else {
                throw new IllegalCallerException("Class what extends Extensible always need to be annotated by @Module");
            }
        } catch (Exception e) {
            SkJson.exception(e, e.getMessage());
        }
    }

    String coloredElement(String input) {
        return switch (input) {
            case "Expressions" -> "&aExpressions";
            case "Effects" -> "&bEffects";
            case "Events" -> "&5Events";
            case "Sections" -> "&fSections";
            case "Conditions" -> "&4Conditions";
            case "Functions" -> "&7Functions";
            case "Structures" -> "&9Structures";
            case "Types" -> "&6Types";
            default -> input;
        };
    }

    public void logElement(String id, int count) {
        SkJson.info(String.format("&8" + AnsiColorConverter.hexToAnsi("#47a5ff") + " + %s &f%d",
                coloredElement(id), count) + AnsiColorConverter.RESET);
    }

    @SuppressWarnings("unused")
    public static class SkriptRegister {

        Extensible extensible;

        public void apply(final Extensible extensible) {
            this.extensible = extensible;
        }

        public <E extends Effect> void registerEffect(Class<E> effect, String... patterns) {
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            extensible.addNewElement("Effects", effect);
            Skript.registerEffect(effect, patterns);
        }

        public <T> void registerProperty(Class<? extends Expression<T>> expressionClass, Class<T> type, String property, String fromType) {
            extensible.addNewElement("Expressions", expressionClass);
            Skript.registerExpression(expressionClass, type, ExpressionType.PROPERTY, "[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property);
        }

        public <T> void registerType(ClassInfo<T> classInfo, String name) {
            extensible.addNewElement("Types", classInfo.getClass());
            Classes.registerClass(classInfo);
        }

        public <E extends Expression<T>, T> void registerExpression(Class<E> c, Class<T> returnType, ExpressionType type, String... patterns) {
            extensible.addNewElement("Expressions", c);
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            Skript.registerExpression(c, returnType, type, patterns);
        }

        public <T> void registerPropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
            extensible.addNewElement("Expressions", c);
            PropertyExpression.register(c, returnType, property, fromType);
        }

        public <T> void registerSimplePropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
            extensible.addNewElement("Expressions", c);
            PropertyExpression.register(c, returnType, property, fromType);
        }

        public void registerEvent(String name, Class<? extends SkriptEvent> c, Class<? extends Event> event, String description, String examples, String version, String... patterns) {
            extensible.addNewElement("Events", c);
            Skript.registerEvent(name, c, event, patterns)
                    .since(version)
                    .examples(examples)
                    .description(description);
        }

        public <E extends Condition> void registerCondition(Class<E> c, String... patterns) {
            extensible.addNewElement("Conditions", c);
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            Skript.registerCondition(c, patterns);
        }

        public <E extends Section> void registerSection(Class<E> requestClass, String... patterns) {
            extensible.addNewElement("Sections", requestClass);
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            Skript.registerSection(requestClass, patterns);
        }

        public JavaFunction<?> registerFunction(JavaFunction<?> fn) {
            extensible.addNewElement("Functions", fn.getClass());
            return Functions.registerFunction(fn);
        }
    }
}
