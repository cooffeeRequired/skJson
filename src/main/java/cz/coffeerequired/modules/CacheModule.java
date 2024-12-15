package cz.coffeerequired.modules;

import ch.njol.skript.conditions.CondIsLeftHanded;
import ch.njol.skript.lang.ExpressionType;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.skript.cache.*;

@Module(module = "cache", version = "1.0.0")
public class CacheModule extends Modulable {
    public CacheModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.cache";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {
        register.apply(this);
        register.registerEffect(AEffHandleWatcher.class, "bind storage watcher to %string%", "unbind storage watcher from %string%");
        register.registerCondition(CondIsListened.class, "json storage %string% is listen", "json storage %string% is(n't| not) listen");
        register.registerEffect(EffVirtualStorage.class, "create json virtual storage named %string%");
        register.registerEffect(AEffBindFile.class, "bind json file %string% as %string%", "bind json file %string% as %string% and let bind storage watcher");
        register.registerExpression(ExprGetCacheStorage.class, JsonElement.class, ExpressionType.SIMPLE, "json storage of id %string%", "all cached jsons");



    }
}
