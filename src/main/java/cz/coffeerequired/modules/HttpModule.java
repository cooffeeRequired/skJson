package cz.coffeerequired.modules;

import cz.coffeerequired.api.Extensible;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;

@Module(module = "http", version = "1.0.0")
public class HttpModule extends Extensible {
    public HttpModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.http";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {

    }
}
