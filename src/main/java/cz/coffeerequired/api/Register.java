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
import cz.coffeerequired.api.exceptions.ModulableException;
import cz.coffeerequired.modules.CacheModule;
import cz.coffeerequired.modules.HttpModule;
import cz.coffeerequired.modules.JsonModule;
import cz.coffeerequired.modules.NbtModule;
import cz.coffeerequired.support.AnsiColorConverter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;

import static cz.coffeerequired.SkJson.logger;

public class Register {

    static final String prefix = "[skjson]";
    static ArrayDeque<Class<? extends Modulable>> modules = new ArrayDeque<>();
    @Getter
    private static SkriptAddon addon;
    @Getter
    private SkriptRegister skriptRegister = new SkriptRegister();

    @ExternalAPI
    public static <T extends Modulable> void registerModules(JavaPlugin plugin, Class<T> module) {
        try {
            if (module.isAnnotationPresent(Module.class) && Modifier.isPublic(module.getModifiers())) {
                Module annotation = module.getAnnotation(Module.class);
                String moduleName = annotation.module();
                String moduleVersion = annotation.version();
                logger().info("[" + plugin.getName() + "]Registering module: " + AnsiColorConverter.hexToAnsi("#47a5ff") + moduleName + AnsiColorConverter.RESET + " version: " + AnsiColorConverter.hexToAnsi("#8dff3f") + moduleVersion);
                modules.add(module);
            } else {
                throw new IllegalCallerException("Class what extends Modulable always need to be annotated by @Module");
            }
        } catch (Exception e) {
            logger().exception(e.getMessage(), e);
        }
    }

    public void tryRegisterSkript() {

        if (isSkriptAvailable()) {
            addon = Skript.registerAddon(SkJson.getInstance());
            addon.setLanguageFileDirectory("lang");
            logger().info("Hooking into Skript plugin... Hooks initialized.");
            logger().info("Trying register Skript addon...");
            logger().info("Trying register Skript elements...");

            registerModule(HttpModule.class);
            registerModule(JsonModule.class);
            registerModule(CacheModule.class);
            registerModule(NbtModule.class);

        } else {
            logger().error("Skript plugin not detected.");
        }
    }

    private void printAllRegistered(Modulable m) {
        m.getLoadedElements().forEach((key, value) -> {
            if (value.isEmpty()) return;
            logElement(key, value.size());
        });
    }

    public <T> void registerNewHook(Class<T> tClass) {
        if (isClassAvailable(tClass) && tClass.getName().equals("ch.njol.skript.Skript")) {
            logger().info("Attempting to hook into Skript plugin...");
            tryRegisterSkript();
        } else {
            logger().error("Unsupported hook class: " + tClass.getName());
        }
    }

    private boolean isSkriptAvailable() {
        try {
            Class.forName("ch.njol.skript.Skript");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isClassAvailable(Class<?> className) {
        try {
            Class.forName(className.getName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public <T extends Modulable> void registerModule(Class<T> module) {
        try {
            if (module.isAnnotationPresent(Module.class) && Modifier.isPublic(module.getModifiers())) {
                Module annotation = module.getAnnotation(Module.class);
                String moduleName = annotation.module();
                String moduleVersion = annotation.version();
                logger().info("Registering module: " + AnsiColorConverter.hexToAnsi("#47a5ff") + moduleName + AnsiColorConverter.RESET + " version: " + AnsiColorConverter.hexToAnsi("#8dff3f") + moduleVersion);

                try {
                    Modulable m = module.getDeclaredConstructor().newInstance();
                    m.load();
                    m.registerElements(this.getSkriptRegister());
                    printAllRegistered(m);
                } catch (ModulableException | InstantiationException | IllegalAccessException |
                         InvocationTargetException |
                         NoSuchMethodException e) {
                    SkJson.logger().exception(e.getMessage(), e);
                }
            } else {
                throw new IllegalCallerException("Class what extends Modulable always need to be annotated by @Module");
            }
        } catch (Exception e) {
            logger().exception(e.getMessage(), e);
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
        Bukkit.getConsoleSender()
                .sendMessage(CustomLogger
                        .getConverter()
                        .deserialize(String.format(
                                "[%s]: &8&l" + AnsiColorConverter.hexToAnsi("#47a5ff") + "+ %s &f%d",
                                CustomLogger.getGRADIENT_PREFIX(), coloredElement(id), count) + AnsiColorConverter.RESET
                        )
                );
    }

    @SuppressWarnings("unused")
    public static class SkriptRegister {

        Modulable modulable;

        public void apply(final Modulable modulable) {
            this.modulable = modulable;
        }

        public <E extends Effect> void registerEffect(Class<E> effect, String... patterns) {
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            modulable.addNewElement("Effects", effect);
            Skript.registerEffect(effect, patterns);
        }

        public <T> void registerProperty(Class<? extends Expression<T>> expressionClass, Class<T> type, String property, String fromType) {
            modulable.addNewElement("Expressions", expressionClass);
            Skript.registerExpression(expressionClass, type, ExpressionType.PROPERTY, "[the] " + property + " of %" + fromType + "%", "%" + fromType + "%'[s] " + property);
        }

        public <T> void registerType(ClassInfo<T> classInfo, String name) {
            modulable.addNewElement("Types", classInfo.getClass());
            Classes.registerClass(classInfo);
        }

        public <E extends Expression<T>, T> void registerExpression(Class<E> c, Class<T> returnType, ExpressionType type, String... patterns) {
            modulable.addNewElement("Expressions", c);
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            Skript.registerExpression(c, returnType, type, patterns);
        }

        public <T> void registerPropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
            modulable.addNewElement("Expressions", c);
            PropertyExpression.register(c, returnType, property, fromType);
        }

        public <T> void registerSimplePropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
            modulable.addNewElement("Expressions", c);
            PropertyExpression.register(c, returnType, property, fromType);
        }

        public void registerEvent(String name, Class<? extends SkriptEvent> c, Class<? extends Event> event, String description, String examples, String version, String... patterns) {
            modulable.addNewElement("Events", c);
            Skript.registerEvent(name, c, event, patterns)
                    .since(version)
                    .examples(examples)
                    .description(description);
        }

        public <E extends Condition> void registerCondition(Class<E> c, String... patterns) {
            modulable.addNewElement("Conditions", c);
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            Skript.registerCondition(c, patterns);
        }

        public <E extends Section> void registerSection(Class<E> requestClass, String... patterns) {
            modulable.addNewElement("Sections", requestClass);
            for (int i = 0; i < patterns.length; i++) patterns[i] = prefix + patterns[i];
            Skript.registerSection(requestClass, patterns);
        }

        public JavaFunction<?> registerFunction(JavaFunction<?> fn) {
            modulable.addNewElement("Functions", fn.getClass());
            return Functions.registerFunction(fn);
        }
    }
}
