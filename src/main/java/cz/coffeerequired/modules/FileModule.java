package cz.coffeerequired.modules;

import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.skript.json.EffNewFile;


@Module(module = "file", version = "1.0.0")
public class FileModule extends Modulable {

    public FileModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.json-file";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {
        register.apply(this);

        register.registerEffect(EffNewFile.class, "new json file %~string%", "new json file %~string% with [content] %-objects%");
    }
}