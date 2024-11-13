package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.CustomLogger;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.support.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Slf4j
public final class SkJson extends JavaPlugin {
    @Getter static SkJson instance;
    @Getter static Configuration configuration;

    static CustomLogger logger;



    public static @NotNull CustomLogger logger() {
        return logger;
    }

    private final Register register = new Register();



    @Override
    public void onLoad() {
        instance = this;
        logger = new CustomLogger(this.getName());

        Configuration.applyScheduledUpdate();

        configuration = new Configuration(this);
        configuration.checkForUpdate();

        setupMetrics(17374, this);
        logger.info("bStats metrics enabled.");

        // TODO could add JSON watcher registration here


    }

    @Override
    public void onEnable() {
        logger.info("Enabling...");
        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }


    @SuppressWarnings("UnstableApiUsage")
    public void setupMetrics(int id, JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, id);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));
        metrics.addCustomChart(new SimplePie("skjson_version", () -> plugin.getPluginMeta().getVersion()));
    }
}
