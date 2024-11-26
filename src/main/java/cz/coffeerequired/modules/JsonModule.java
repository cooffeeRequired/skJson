package cz.coffeerequired.modules;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ExpressionType;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.skript.json.*;

@Module(module = "json", version = "1.0.0")
public class JsonModule extends Modulable {

    public JsonModule() {
        this.sign = this.getClass().getSimpleName();
        this.skriptElementPath = "cz.coffeerequired.skript.json";
    }

    @Override
    public void registerElements(Register.SkriptRegister register) {
        register.apply(this);

        register.registerType(new ClassInfo<>(JsonElement.class, "json")
                        .user("json")
                        .name("json")
                        .description("Json representation of any object in skript")
                        .since("2.9"),
                "type.json"
        );

        register.registerEffect(EffNewFile.class, "new json file %~string%", "new json file %~string% with [content] %-objects%");

        register.registerEffect(EffMapJson.class, "[:async] (map|copy) %json% to %objects%");

        register.registerExpression(ExprCountElements.class, Integer.class, ExpressionType.SIMPLE, "[the] count of (:key[s]|:value[s]) in %json%");

        register.registerExpression(ExprJsonElements.class, Object.class, ExpressionType.SIMPLE, "element %-string% of %json%", "elements %-string% of %json%");

        register.registerPropertyExpression(ExprFormattingJsonToVariable.class, JsonElement.class, "formatted [json]", "jsons");
        // #Done("26.11.24")
        register.registerExpression(ExprNewJson.class, JsonElement.class, ExpressionType.COMBINED, "json from file %strings%", "json from website %strings%", "json from %objects%");
        // #Done("26.11.24")
        register.registerExpression(ExprPrettyPrint.class, String.class, ExpressionType.SIMPLE, "%json% as pretty[ printed]", "%json% as uncolo[u]red pretty[ printed]");
        // #Done("26.11.24")
        register.registerSimplePropertyExpression(ExprJsonSize.class, Integer.class, "json size", "jsons");
    }
}
