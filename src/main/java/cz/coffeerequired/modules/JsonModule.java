package cz.coffeerequired.modules;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ExpressionType;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.Modulable;
import cz.coffeerequired.api.Register;
import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.skript.json.*;

import static cz.coffeerequired.skript.json.SupportSkriptJson.*;

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

        register.registerExpression(ExprStrictLiteralJson.class, Object.class, ExpressionType.PATTERN_MATCHES_EVERYTHING,
                "%json%.<(\\w+|[\\{\\}}])(\\[\\]|\\[\\d+\\])?(\\*)?(\\.)?>"
        );

        register.registerCondition(CondJsonHas.class,
                "%json% has [:all] (:value[s]|:key[s]) %objects%",
                "%json% does(n't| not) have [:all] (:value[s]|:key[s]) %objects%"
        );
        register.registerCondition(CondJsonType.class,
            "type of %json% is (json[-]:object|json[-]:array|json[-]:primitive|json[-]:null)",
                "type of %json% (is(n't| not)) (json[-]:object|json[-]:array|json[-]:primitive|json[-]:null)"
        );
        register.registerExpression(JsonSupportElement.class, Object.class, ExpressionType.COMBINED,
            "(1st|first) (:value|:key) of %json%",
                "(2nd|second) (:value|:key) of %json%",
                "(3rd|third) (:value|:key) of %json%",
                "last (:value|:key) of %json%",
                "random (:value|:key) of %json%",
                "%integer%. (:value|:key) of %json%"
        );
        register.registerExpression(ExprGetAllKeys.class, String.class, ExpressionType.SIMPLE, "[all] keys [%-string%] of %json%");
        register.registerExpression(JsonLoopExpression.class, Object.class, ExpressionType.SIMPLE, "[the] json-(:key|:value)[-<(\\d+)>]");
        register.registerExpression(ExprCountElements.class, Integer.class, ExpressionType.SIMPLE, "[the] count of (:key[s]|:value[s]) %object% in %json%");
        register.registerExpression(ExprJsonElements.class, Object.class, ExpressionType.COMBINED, "(element|value) [%-string%] of %json%", "(elements|values) [%-string%] of %json%");
        register.registerEffect(EffMapJson.class, "[:async] (map|copy) %json% to %objects%");
        register.registerPropertyExpression(ExprFormattingJsonToVariable.class, JsonElement.class, "form[atted [json]]", "jsons");
        register.registerExpression(ExprNewJson.class, JsonElement.class, ExpressionType.COMBINED, "json from file %strings%", "json from website %strings%", "json from %objects%");
        register.registerExpression(ExprPrettyPrint.class, String.class, ExpressionType.SIMPLE, "%json% as pretty[ printed]", "%json% as uncolo[u]red pretty[ printed]");
        register.registerSimplePropertyExpression(ExprJsonSize.class, Integer.class, "json size", "jsons");
        register.registerExpression(ExprAllJsonFiles.class, String.class, ExpressionType.COMBINED, "[all] json [files] (from|in) (dir[ectory]|folder) %string%");
        register.registerEffect(EffNewFile.class,
                "create json file %string% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]",
                "create json file %string% and write to it %object% [:with configuration<\\[\\s*((\\w+)=([\\w-]+)(?:,\\s*)?)+\\s*\\]>]"
        );
        register.registerCondition(CondJsonFileExist.class, "json file %string% exists", "json file %string% does(n't| not) exist");
        register.registerCondition(CondJsonIsEmpty.class, "json %json% is empty", "json %json% is(n't| not) empty");
    }
}
