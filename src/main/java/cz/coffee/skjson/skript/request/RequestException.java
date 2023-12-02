package cz.coffee.skjson.skript.request;

public class RequestException extends IllegalStateException {

    public RequestException(String msg) {
        super(msg);
    }

    public static RequestException handlersCalled() {
        return new RequestException("Event handlers may be called... ");
    }

    public static RequestException requestNull() {
        return new RequestException("Request cannot be null...");
    }

    public static RequestException wrongStoreVar() {
        return new RequestException("You can store request only to Variables!");
    }
}
