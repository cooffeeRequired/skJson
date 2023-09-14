package cz.coffee.skjson.json;

public class ParsedJsonException extends Exception {

    private final String message;

    public ParsedJsonException(final String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return "ParsedJsonException: error occurred. stack: " +this.message;
    }
}
