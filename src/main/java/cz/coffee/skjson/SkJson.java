package cz.coffee.skjson;

import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.FileWrapper;
import cz.coffee.skjson.api.SkriptLoaderFile;
import cz.coffee.skjson.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.RUN_TEST_ON_START;

public final class SkJson extends JavaPlugin {
    Config config = new Config(this);
    @Override
    public void onEnable() {
        try {
            config.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (config.ready()) {
            Util.log("Hurray! SkJson is &aenabled.");
            CompletableFuture.runAsync(() -> {
                if (RUN_TEST_ON_START) {
                    try {
                        Util.log("Preparing to run tests... delay limit is: " + Config.TEST_START_UP_DELAY);
                        Thread.sleep(Config.TEST_START_UP_DELAY);
                        var loader = new SkriptLoaderFile(new File(this.getDataFolder() + "/" + "..tests"));
                        loader.load();
                        Thread.sleep(200);
                        loader.unload();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            throw new IllegalStateException("Opps! Something is wrong");
        }
    }

    @Override
    public void onDisable() {
        JsonWatcher.unregisterAll();
        Util.log("Goodbye! SkJson is &#d60f3aDisabled!");
    }
    public static Server getThisServer() {
        return Bukkit.getServer();
    }
}
