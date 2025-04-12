package cz.coffeerequired.api.types;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import com.google.gson.JsonElement;

import javax.validation.constraints.NotNull;

public class JSONTypeParser extends Parser<JsonElement> {
    @Override
    public @NotNull String toString(JsonElement o, int flags) {
        return o.toString();
    }

    @Override
    public @NotNull String toVariableNameString(JsonElement o) {
        return toString(o, 1);
    }

    @Override
    public boolean canParse(@NotNull ParseContext context) {
        return false;
    }
}
