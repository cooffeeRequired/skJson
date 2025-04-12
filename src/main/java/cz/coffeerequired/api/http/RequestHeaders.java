package cz.coffeerequired.api.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.net.http.HttpHeaders;
import java.util.Map;

public class RequestHeaders {

    private final Gson gson = new Gson();
    private final HttpHeaders headers;

    public RequestHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public JsonElement toJson() {
        return gson.toJsonTree(headers.map());
    }

    public String toText() {
        return gson.toJson(headers.map());
    }

    public Map<String, java.util.List<String>> getRaw() {
        return headers.map();
    }
}
