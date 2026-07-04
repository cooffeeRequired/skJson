package cz.coffeerequired.api;


import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.api.exceptions.ExtensibleThrowable;
import cz.coffeerequired.modules.Core;
import cz.coffeerequired.modules.HttpModule;
import cz.coffeerequired.support.AnsiColorConverter;
import lombok.Getter;
import org.bukkit.event.Event;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Priority;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Register {

    static final String prefix = "[skjson] ";
    static ArrayDeque<Class<? extends Extensible>> modules = new ArrayDeque<>();
    private static final Set<Class<? extends Extensible>> registeredModuleClasses = new HashSet<>();
    @Getter
    private static SkriptAddon addon;
    @Getter
    private final SkriptRegister skriptRegister = new SkriptRegister();

    public static boolean isClassAvailable(Class<?> className) {
        try {
            Class.forName(className.getName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static final String EVENT_VALUE_REGISTRY =
            "org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry";

    public void tryRegisterSkript() {

        if (isSkriptAvailable()) {
            if (!isClassAvailable(EVENT_VALUE_REGISTRY)) {
                SkJson.severe(
                        "SkJson 6.0 requires Skript 2.15 or newer (missing EventValueRegistry). "
                                + "Please update Skript before using this addon."
                );
                org.bukkit.Bukkit.getPluginManager().disablePlugin(SkJson.getInstance());
                return;
            }
            addon = Skript.instance().registerAddon(SkJson.class, SkJson.getInstance().getName());
            addon.localizer().setSourceDirectories(
                    "lang",
                    SkJson.getInstance().getDataFolder().getAbsolutePath() + "/lang"
            );

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

        SkJson.debug("&8Extensible modules %s", m.getLoadedElements());

        m.getLoadedElements().forEach((key, value) -> {
            if (value.isEmpty()) return;
            logElement(key, value.size());
        });
    }

    public <T> void registerNewHook(Class<T> tClass) throws IOException {
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

    public static boolean isModuleRegistered(Class<? extends Extensible> module) {
        return registeredModuleClasses.contains(module);
    }

    public <T extends Extensible> void registerModule(Class<T> module) {
        if (registeredModuleClasses.contains(module)) {
            SkJson.debug("Module already registered, skipping: %s", module.getSimpleName());
            return;
        }
        try {
            if (module.isAnnotationPresent(Module.class) && Modifier.isPublic(module.getModifiers())) {
                Module annotation = module.getAnnotation(Module.class);
                assert annotation != null;
                String moduleName = annotation.module();
                SkJson.info(
                        "Registering module: %s%s&r",
                        AnsiColorConverter.hexToAnsi("#47a5ff"), moduleName
                );
                try {
                    Extensible m = module.getDeclaredConstructor().newInstance();
                    m.load();
                    m.registerElements(this.getSkriptRegister());
                    registeredModuleClasses.add(module);
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
            case "Event Values" -> "&aEvent Values";
            default -> input;
        };
    }

    public void logElement(String id, int count) {
        SkJson.info(String.format("&8" + AnsiColorConverter.hexToAnsi("#47a5ff") + " + %s &f%d",
                coloredElement(id), count) + AnsiColorConverter.RESET);
    }

    public static List<Extensible> registers = new ArrayList<>();

    public static void loadAddonClasses(String path) {
        org.skriptlang.skript.util.ClassLoader.builder()
                .basePackage(path)
                .build()
                .loadClasses(SkJson.class);
    }

    static SyntaxRegistry syntaxRegistry() {
        return addon.syntaxRegistry();
    }

    static EventValueRegistry eventValueRegistry() {
        return addon.registry(EventValueRegistry.class);
    }

    static String[] prefixPatterns(String... patterns) {
        String[] prefixed = patterns.clone();
        for (int i = 0; i < prefixed.length; i++) {
            prefixed[i] = prefix + prefixed[i];
        }
        return prefixed;
    }

    public static class SkriptRegister {

        Extensible extensible;

        public void apply(final Extensible extensible) {
            this.extensible = extensible;
            registers.add(extensible);
        }

        public <E extends Effect> void registerEffect(Class<E> effect, String... patterns) {
            registerEffect(effect, SyntaxInfo.COMBINED, patterns);
        }

        public <E extends Effect> void registerEffect(Class<E> effect, Priority priority, String... patterns) {
            extensible.addNewElement("Effects", effect);
            SkJson.debug("&8Registering effect: &7patterns: %s&8; name: %s",
                Arrays.toString(patterns).substring(1, Arrays.toString(patterns).length() - 1),
                effect.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(effect)
                    .priority(priority)
                    .addPatterns(prefixPatterns(patterns))
                    .build());
        }

        public <T> void registerProperty(Class<? extends Expression<T>> expressionClass, Class<T> type, String property, String fromType) {
            extensible.addNewElement("Expressions", expressionClass);
            SkJson.debug("&8Registering property: &7property: %s&8; from: %s&8; name: %s",
                property, fromType, expressionClass.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(expressionClass, type)
                    .priority(PropertyExpression.DEFAULT_PRIORITY)
                    .addPatterns(prefixPatterns(
                            "[the] " + property + " of %" + fromType + "%",
                            "%" + fromType + "%'[s] " + property
                    ))
                    .build());
        }

        public <T> void registerType(ClassInfo<T> classInfo, String name) {
            extensible.addNewElement("Types", classInfo.getClass());
            SkJson.debug("&8Registering type: &7name: %s&8; class: %s",
                name, classInfo.getClass().getSimpleName());
            Classes.registerClass(classInfo);
        }

        public <E extends Expression<T>, T> void registerExpression(Class<E> c, Class<T> returnType, Priority priority, String... patterns) {
            extensible.addNewElement("Expressions", c);
            SkJson.debug("&8Registering expression: &7patterns: %s&8; name: %s",
                Arrays.toString(patterns).substring(1, Arrays.toString(patterns).length() - 1),
                c.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(c, returnType)
                    .priority(priority)
                    .addPatterns(prefixPatterns(patterns))
                    .build());
        }

        public <E extends Event, V> void registerEventValue(
                Class<E> eventClass,
                Class<V> valueClass,
                Converter<E, V> converter,
                String... patterns
        ) {
            extensible.addNewElement("Event Values", eventClass);
            SkJson.debug("&8Registering event value: &7patterns: %s&8; event: %s&8; type: %s",
                Arrays.toString(patterns).substring(1, Arrays.toString(patterns).length() - 1),
                eventClass.getSimpleName(), valueClass.getSimpleName());
            eventValueRegistry().register(EventValue.builder(eventClass, valueClass)
                    .getter(converter)
                    .patterns(patterns)
                    .time(EventValue.Time.NOW)
                    .build());
        }

        public void registerEvent(String name, Class<? extends SkriptEvent> c, Class<? extends Event> event, String description, String examples, String version, String... patterns) {
            extensible.addNewElement("Events", c);
            String[] prefixed = prefixPatterns(patterns);
            for (int i = 0; i < prefixed.length; i++) {
                prefixed[i] = BukkitSyntaxInfos.fixPattern(prefixed[i]);
            }

            SkJson.debug("&8Registering event: &7patterns: %s&8; name: %s&8; version: %s",
                Arrays.toString(prefixed).substring(1, Arrays.toString(prefixed).length() - 1),
                name, version);

            syntaxRegistry().register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(c, name)
                    .addDescription(description)
                    .addExamples(examples)
                    .addSince(version)
                    .addEvent(event)
                    .addPatterns(prefixed)
                    .build());
        }

        public <E extends Condition> void registerCondition(Class<E> c, String... patterns) {
            registerCondition(c, SyntaxInfo.COMBINED, patterns);
        }

        public <E extends Condition> void registerCondition(Class<E> c, Priority priority, String... patterns) {
            extensible.addNewElement("Conditions", c);
            SkJson.debug("&8Registering condition: &7patterns: %s&8; name: %s",
                Arrays.toString(patterns).substring(1, Arrays.toString(patterns).length() - 1),
                c.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(c)
                    .priority(priority)
                    .addPatterns(prefixPatterns(patterns))
                    .build());
        }

        public <E extends Section> void registerSection(Class<E> requestClass, String... patterns) {
            extensible.addNewElement("Sections", requestClass);
            SkJson.debug("&8Registering section: &7patterns: %s&8; name: %s",
                Arrays.toString(patterns).substring(1, Arrays.toString(patterns).length() - 1),
                requestClass.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.SECTION, SyntaxInfo.builder(requestClass)
                    .addPatterns(prefixPatterns(patterns))
                    .build());
        }

        public <T> void registerPropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
            extensible.addNewElement("Expressions", c);
            SkJson.debug("&8Registering property expression: &7property: %s&8; from: %s&8; name: %s",
                property, fromType, c.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.EXPRESSION, PropertyExpression.infoBuilder(c, returnType, property, fromType, false)
                    .clearPatterns()
                    .addPatterns(prefixPatterns(PropertyExpression.getPatterns(property, fromType)))
                    .build());
        }

        public <T> void registerSimplePropertyExpression(Class<? extends Expression<T>> c, Class<T> returnType, String property, String fromType) {
            extensible.addNewElement("Expressions", c);
            SkJson.debug("&8Registering simple property expression: &7property: %s&8; from: %s&8; name: %s",
                property, fromType, c.getSimpleName());
            syntaxRegistry().register(SyntaxRegistry.EXPRESSION, PropertyExpression.infoBuilder(c, returnType, property, fromType, false)
                    .clearPatterns()
                    .addPatterns(prefixPatterns(PropertyExpression.getPatterns(property, fromType)))
                    .build());
        }

        public DefaultFunction<?> registerFunction(DefaultFunction<?> fn) {
            extensible.addNewElement("Functions", fn.getClass());
            SkJson.debug("&8Registering function: &7name: %s&8; class: %s",
                fn.name(), fn.getClass().getSimpleName());
            return Functions.register(fn);
        }
    }
}
