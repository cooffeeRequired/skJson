package cz.coffeerequired.modules;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Extensible;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import org.bukkit.Bukkit;

import static de.tr7zw.changeme.nbtapi.NBT.preloadApi;


@Module(module = "nbt", version = "1.0.0")
public class NbtModule extends Extensible {
    public NbtModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.nbt";

        if (!preloadApi()) {
            SkJson.logger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(SkJson.getInstance());
        }
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {

    }
}
