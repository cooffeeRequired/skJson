package cz.coffeerequired.modules;

import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;

@Module(module = "cache", version = "1.0.0")
public class CacheModule extends Modulable {
    public CacheModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.cache";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {
        register.apply(this);

    }
}
