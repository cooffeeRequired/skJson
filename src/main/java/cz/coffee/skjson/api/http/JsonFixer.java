package cz.coffee.skjson.api.http;

public record JsonFixer(String json) {
    String removeTrailingComma() {
        return json.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");
    }
}
