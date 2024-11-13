package cz.coffeerequired.modules;

import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;

@Module(module = "nbt", version = "1.0.0")
public class NbtModule extends Modulable {
    public NbtModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.nbt";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {

    }
}
