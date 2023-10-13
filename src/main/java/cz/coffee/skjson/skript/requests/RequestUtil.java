package cz.coffee.skjson.skript.requests;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Variable;
import com.google.gson.*;
import cz.coffee.skjson.parser.ParserUtil;
import org.bukkit.event.Event;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.Map;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: středa (04.10.2023)
 */
public class RequestUtil {
    public static final class Pairs {
        private String LEFT;
        private static final String DELIMITER = ":";
        private String RIGHT;

        Pairs(Object input) {
            if (input instanceof String str) {
                var sp = str.split(DELIMITER);
                if (sp.length == 2 && !sp[0].isEmpty() && !sp[1].isEmpty()) {
                    this.LEFT = sp[0];
                    this.RIGHT = sp[1];
                }
            }
        }

        private static boolean isPair(Object input) {
            if (input instanceof String str) {
                var sp = str.split(DELIMITER);
                return sp.length == 2 && !sp[0].isEmpty() && !sp[1].isEmpty();
            }
            return false;
        }

        public String getKey() {
            return this.LEFT.trim();
        }
        public String getValue() {
            return this.RIGHT.trim();
        }

        @Override
        public String toString() {
            return this.LEFT+DELIMITER+this.RIGHT;
        }
    }

    public static final EntryValidator VALIDATOR = EntryValidator.builder()
            .addEntryData(new ExpressionEntryData<>("content", null, true, Object.class))
            .addEntryData(new ExpressionEntryData<>("body", null, true, Object.class))

            .addEntryData(new ExpressionEntryData<>("headers", null, true, Object.class))

            .addEntryData(new ExpressionEntryData<>("lenient", null, true, Object.class))
            .addEntryData(new ExpressionEntryData<>("save incorrect response", null, true, Object.class))
            .addSection("save", true)
            .addSection("on complete", false)
            .build();

    public static final EntryValidator SAVE_VALIDATOR = EntryValidator.builder()
            .addEntryData(new ExpressionEntryData<>("content", null, true, Variable.class))
            .addEntryData(new ExpressionEntryData<>("body", null, true, Variable.class))

            .addEntryData(new ExpressionEntryData<>("headers", null, true, Variable.class))
            .addEntryData(new ExpressionEntryData<>("status code", null, true, Variable.class))
            .addEntryData(new ExpressionEntryData<>("url", null, true, Variable.class))
            .build();

    @SuppressWarnings("nullable")
    static Map<Boolean, JsonElement> isJSON(String str) {
        try {
            var json = JsonParser.parseString(str);
            if (json == null) {
                json = new Gson().fromJson(str, JsonElement.class);
            }
            if (json != null) return  Map.of(true, json);
        } catch (Exception ignored) {
            return Map.of(false, JsonNull.INSTANCE);
        }
        return Map.of(false, JsonNull.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    public static <T, O extends JsonElement> O validateContent(final T input, final Event event) {
        var MAX_ELEMENTS_PER_REQUEST = 1;
        if (event == null) throw new IllegalArgumentException();

        if (input instanceof Expression<?> expr) {
            var ds = expr.getAll(event);
            JsonElement[] jsonElements = new JsonElement[ds.length];
            for (var i = 0; i < ds.length; i++) {
                if (i == MAX_ELEMENTS_PER_REQUEST) {
                    throw new IllegalStateException("Buffer size of streamable objects was excited");
                }
                var d = ds[i];
                if (d instanceof String str) {
                    var check = isJSON(str);
                    var json = check.get(true) != null ? check.get(true) : new JsonObject();
                    jsonElements[i] = json;
                } else if (d instanceof JsonElement element) {
                    jsonElements[i] = element;
                }
            }
            return (O) jsonElements[0];
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T, O extends Pairs> O[] validateHeaders(final T input, final Event event) {
        if (input instanceof Expression<?> expr) {
            var ds = expr.getAll(event);
            Pairs[] pairs = new Pairs[ds.length];
            for (var i = 0; i < ds.length; i++) {
                var d = ds[i];
                if (Pairs.isPair(d)) {
                    pairs[i] = new Pairs(d);
                } else if (d instanceof JsonElement element) {
                    if (element instanceof JsonObject object) {
                        int finalI = i;
                        object.asMap().forEach((key, value) -> pairs[finalI] = new Pairs(key + ":" + ParserUtil.jsonToType(value)));
                    }
                }
            }
            return (O[]) pairs;
        }
        return null;
    }
}
