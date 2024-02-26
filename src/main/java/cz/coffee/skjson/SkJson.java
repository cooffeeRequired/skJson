package cz.coffee.skjson;

import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Config;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

import static cz.coffee.skjson.utils.Logger.coloredElement;
import static cz.coffee.skjson.utils.Logger.info;

public final class SkJson extends JavaPlugin {

    public static double CONFIG_PRIMARY_VERSION = 1.4;
    static SkJson plugin;
    Config config = new Config(this);

    public static SkJson getInstance() {
        return plugin;
    }

    public static Server getThisServer() {
        return Bukkit.getServer();
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
                SkJsonElements.SkjsonElements.forEach((key, value) -> info("  &8&l - &7Registered " + coloredElement(key) + "&f " + value.size()));
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
}
