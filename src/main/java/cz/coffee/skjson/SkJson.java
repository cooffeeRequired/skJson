package cz.coffee.skjson;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Config;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static cz.coffee.skjson.utils.Logger.coloredElement;
import static cz.coffee.skjson.utils.Logger.info;

public final class SkJson extends JavaPlugin {

    public static double CONFIG_PRIMARY_VERSION = 1.4;

    private static final Map<String, ArrayList<String>> SkjsonElements = new HashMap<>(Map.of(
            "Expressions", new ArrayList<>(),
            "Events", new ArrayList<>(),
            "Effects", new ArrayList<>(),
            "Sections", new ArrayList<>(),
            "Structures", new ArrayList<>(),
            "Functions", new ArrayList<>(),
            "Conditions", new ArrayList<>())
    );
    Config config = new Config(this);
    static SkJson plugin;

    public static SkJson getInstance() {
        return plugin;
    }

    @Override
    @SuppressWarnings("all")
    public void onEnable() {
        plugin = this;
        if (Bukkit.getServer().getName().equals("CraftBukkit")) {
            System.out.println("\033[0;31m-------------------------SPIGOT DETECTED------------------------------");
            System.out.println("Please install SkJson for Spigot version " + this.getPluginMeta().getVersion());
            System.out.println("---------------------------------------------------------------------\033[0m");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            try {
                config.init();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (config.ready()) {
                info("Registered elements..");
                SkjsonElements.forEach((key, value) -> info("  &8&l - &7Registered " + coloredElement(key) + "&f " + value.size()));
                info("Hurray! SkJson is &aenabled.");
            } else {
                throw new IllegalStateException("Opps! Something is wrong");
            }
        }
    }

    @Override
    public void onDisable() {
        if (Bukkit.getServer().getName().equals("CraftBukkit")) {
            System.out.println("Disabled");
        } else {
            JsonWatcher.unregisterAll();
            info("Goodbye! SkJson is &#d60f3aDisabled!");
        }
    }

    public static Server getThisServer() {
        return Bukkit.getServer();
    }


    public static <E extends Effect> void registerEffect(Class<E> c, String... patterns) {
        SkjsonElements.get("Effects").add(c.toString());
        for (int i = 0; i < patterns.length; i++) patterns[i] = "[skJson] " + patterns[i];
        Skript.registerEffect(c, patterns);
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
