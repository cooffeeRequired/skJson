package cz.coffee.skjson.json;

public class JsonParserException extends Exception {

    private final String message;

    public JsonParserException(final String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return "JsonParserException: error occurred. stack: " + this.message;
    }
}
