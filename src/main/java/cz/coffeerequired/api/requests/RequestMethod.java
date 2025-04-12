package cz.coffeerequired.api.requests;

public enum RequestMethod {
    GET("get"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    MOCK("mock"),
    HEAD("head"),
    PATCH("patch");

    final String value;

    RequestMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value.toUpperCase();
    }
}
