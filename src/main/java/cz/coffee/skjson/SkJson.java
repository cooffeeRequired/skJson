package cz.coffee.skjson;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.SkriptLoaderFile;
import cz.coffee.skjson.utils.LoggingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.RUN_TEST_ON_START;

public final class SkJson extends JavaPlugin {

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
    public void onEnable() {
        plugin = this;
        if (Bukkit.getServer().getName().equals("CraftBukkit")) {
            System.out.println("\033[0;31m-------------------------SPIGOT DETECTED------------------------------");
            System.out.println("Please install SkJson for Spigot version " + this.getDescription().getVersion());
            System.out.println("---------------------------------------------------------------------\033[0m");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            try {
                config.init();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (config.ready()) {
                LoggingUtil.log("Registered elements..");
                SkjsonElements.forEach((key, value) -> LoggingUtil.log("  &8&l - &7Registered " + LoggingUtil.coloredElement(key) + "&f " + value.size()));
                LoggingUtil.log("Hurray! SkJson is &aenabled.");
                CompletableFuture.runAsync(() -> {
                    if (RUN_TEST_ON_START) {
                        try {
                            LoggingUtil.log("Preparing to run tests... delay limit is: " + Config.TEST_START_UP_DELAY);
                            Thread.sleep(Config.TEST_START_UP_DELAY);
                            var loader = new SkriptLoaderFile(new File(this.getDataFolder() + "/" + "..tests"));
                            loader.load();
                            Thread.sleep(200);
                            loader.unload();
                        } catch (Exception ex) {
                            LoggingUtil.enchantedError(ex, ex.getStackTrace(), "Main thread in SkJson.java (38)");
                        }
                    }
                });
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
            LoggingUtil.log("Goodbye! SkJson is &#d60f3aDisabled!");
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
