package cz.coffeerequired.api.exceptions;

public class ExtensibleThrowable extends RuntimeException {
    public ExtensibleThrowable(String message) {
        super(message);
    }
}
