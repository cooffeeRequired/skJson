package cz.coffee.skjson.skript;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.DefaultClasses;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.Config;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: čtvrtek (13.07.2023)
 */

@NoDoc
public class SkJsonFunctions {
    static {
        Parameter<?>[] bool = new Parameter[]{new Parameter<>("bool", DefaultClasses.BOOLEAN, true, null)};
        SkJson.registerFunction(new SimpleJavaFunction<>("skjson_getdelim", bool, DefaultClasses.STRING, true) {
                    @Override
                    public String @NotNull [] executeSimple(Object[] @NotNull [] params) {
                        return new String[]{Config.PATH_VARIABLE_DELIMITER};
                    }
                })
                .description("Return a your defined delimiter in the config.yml")
                .since("2.9")
                .examples("send \"my delimiter is: %skjson_getdelim()%\"");


        Parameter<?>[] object = new Parameter[]{new Parameter<>("object", DefaultClasses.OBJECT, true, null)};

        SkJson.registerFunction(new SimpleJavaFunction<>("b64", object, DefaultClasses.STRING, true) {
                    @Override
                    public String @NotNull [] executeSimple(Object[][] params) {
                        String data = params[0][0].toString();
                        return new String[]{Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8))};
                    }
                })
                .description("Return base64 encoded string")
                .since("2.9")
                .examples("serial: b64(\"This is a test\"");
    }
}
