package cz.coffee.skjson.skript;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.DefaultClasses;
import cz.coffee.skjson.api.Config;
import org.jetbrains.annotations.NotNull;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: čtvrtek (13.07.2023)
 */

@NoDoc
public class SkJsonFunctions {
    static {
        Parameter<?>[] bool = new Parameter[]{new Parameter<>("bool", DefaultClasses.BOOLEAN, true, null)};
        Functions.registerFunction(new SimpleJavaFunction<>("skjson_getdelim", bool, DefaultClasses.STRING, true) {
            @Override
            public String @NotNull [] executeSimple(Object[] @NotNull [] params) {
                return new String[]{Config.PATH_VARIABLE_DELIMITER};
            }
        })
                .description("Return a your defined delimiter in the config.yml")
                .since("2.9")
                .examples("send \"my delimiter is: %skjson_getdelim()%\"");
    }
}
