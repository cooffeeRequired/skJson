package cz.coffee.skjson.skript;

import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.registrations.DefaultClasses;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.ConfigRecords;
import cz.coffee.skjson.parser.ParserUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@NoDoc
public class SkJsonFunctions {
    static {

        SkJson.registerFunction(new SimpleJavaFunction<>("getDelimiter", new Parameter[]{}, DefaultClasses.STRING, true) {
                    @Override
                    @SuppressWarnings("all")
                    public String @NotNull [] executeSimple(Object[] @NotNull [] params) {
                        return new String[]{ConfigRecords.PATH_VARIABLE_DELIMITER};
                    }
                })
                .description("Return a your defined delimiter in the config.yml")
                .since("2.9")
                .examples("send \"my delimiter is: %getDelimiter()%\"");

        @SuppressWarnings("all")
        Parameter<?>[] object = new Parameter[]{new Parameter<>("object", DefaultClasses.OBJECT, true, null)};
        SkJson.registerFunction(new SimpleJavaFunction<>("b64", object, DefaultClasses.STRING, true) {
                    @Override
                    @SuppressWarnings("all")
                    public String @NotNull [] executeSimple(Object[][] params) {
                        String data = params[0][0].toString();
                        return new String[]{Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8))};
                    }
                })
                .description("Return base64 encoded string")
                .since("2.9")
                .examples("serial: b64(\"This is a test\"");

        SkJson.registerFunction(new SimpleJavaFunction<>("string", object, DefaultClasses.OBJECT, true) {
                    @Override
                    @SuppressWarnings("all")
                    public Object @NotNull [] executeSimple(Object[][] params) {
                        String data = params[0][0].toString();
                        final String decodedData = new String(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                        final JsonElement parsed = ParserUtil.parse(decodedData);
                        assert decodedData != null;
                        assert parsed != null;
                        final boolean isSerializable = parsed instanceof JsonNull;
                        return new Object[]{isSerializable ? parsed : decodedData};
                    }
                })
                .description("Returns string/json from base64")
                .since("2.9.9-pre")
                .examples("stringify: string(\"dHR0dA==\"");
    }
}
