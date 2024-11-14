package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.Commands;
import cz.coffeerequired.api.CustomLogger;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.support.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public final class SkJson extends JavaPlugin {
    @Getter
    static SkJson instance;
    @Getter
    static Configuration configuration;

    static CustomLogger logger;
    private final Register register = new Register();

    public static @NotNull CustomLogger logger() {
        return logger;
    }

    @Override
    public void onLoad() {

        instance = this;
        logger = new CustomLogger(this.getName());

        Configuration.applyScheduledUpdate();

        configuration = new Configuration(this);
        configuration.checkForUpdate();

        setupMetrics(17374, this);
        logger.info("bStats metrics enabled.");

        try {
            Class.forName("cz.coffeerequired.api.json.JsonFileWatcher");
            logger.info("Json watchers found & enabled.");
        } catch (ClassNotFoundException e) {
            logger.info("Unable to find Json watchers.");
            logger.exception(e.getMessage(), e);
        }
    }

    @Override
    public void onEnable() {
        logger.info("Enabling...");
        if (Api.canInstantiateSafety()) {
            register.registerNewHook(Skript.class);

            Commands.setMainCommand("skjson");
            Commands.add(
                    "hello",
                    (sender, _) -> sender.sendMessage("Hello, world!"),
                    (_, _) -> List.of("world", "there", "player")
            );

            Commands.registerCommand(this);
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
