package cz.coffee.skjson.json;

import com.google.gson.JsonElement;

import java.util.concurrent.atomic.AtomicBoolean;

import static cz.coffee.skjson.json.JsonParserRecords.*;
import static cz.coffee.skjson.utils.Util.fstring;

public class JsonParser {
    public static void isNull(final JsonElement json) throws JsonParserException {
        if (json == null)
            throw new JsonParserException(fstring("Input cannot be null, required: (JsonObject, JsonArray, JsonPrimitive), given: %s", "NULL"));
    }

    public static Changer change(final JsonElement json) {
        return new Changer(json);
    }

    public static Remover remove(final JsonElement json) {
        return new Remover(json);
    }

    public static Searcher search(final JsonElement json) {
        return new Searcher(json);
    }

    public static Counter count(final JsonElement json) {
        return new Counter(json);
    }

    public static boolean isExpression(final JsonElement json) {
        AtomicBoolean r = new AtomicBoolean(true);
        if (json.isJsonObject()) {
            json.getAsJsonObject().entrySet().forEach(entry -> {
                if (!(entry.getKey().equals("element-index") || entry.getKey().equals("element-path"))) {
                    r.set(false);
                }
            });
        }
        return r.get();
    }
}
