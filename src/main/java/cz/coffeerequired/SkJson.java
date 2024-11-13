package cz.coffeerequired;

import ch.njol.skript.Skript;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.CustomLogger;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.modules.CacheModule;
import cz.coffeerequired.modules.HttpModule;
import cz.coffeerequired.modules.JsonModule;
import cz.coffeerequired.modules.NbtModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
        configuration = new Configuration();
        logger = new CustomLogger(this.getName());

        logger.info("Enabling...");

        if (Api.canInstantiateSafety() && configuration.load()) {
            register.registerNewHook(Skript.class);
            register.registerModule(HttpModule.class);
            register.registerModule(JsonModule.class);
            register.registerModule(CacheModule.class);
            register.registerModule(NbtModule.class);
        }

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
