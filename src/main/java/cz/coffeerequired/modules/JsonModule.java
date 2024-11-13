package cz.coffeerequired.modules;

import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;

@Module(module = "json", version = "1.0.0")
public class JsonModule extends Modulable {
    public JsonModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.json";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {

    }
}
