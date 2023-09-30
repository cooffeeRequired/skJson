package cz.coffee.skjson.api.http;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public record JsonFixer(String json) {
    String removeTrailingComma() {
        return json.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");
    }
}
