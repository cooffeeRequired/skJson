package cz.coffee.skjson.skript.requests;

public enum RequestMethods {
    GET("get"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    MOCK("mock"),
    HEAD("head"),
    PATCH("patch");

    final String stringMethod;

    RequestMethods(String stringMethod) {
        this.stringMethod = stringMethod;
    }

    @Override
    public String toString() {
        return stringMethod.toUpperCase();
    }
}