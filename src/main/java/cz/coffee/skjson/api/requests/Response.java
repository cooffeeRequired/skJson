package cz.coffee.skjson.api.requests;

import com.google.gson.JsonElement;

public class Response {
    final Integer statusCode;

    public Integer statusCode() {
        return statusCode;
    }

    public Object content() {
        return content;
    }

    public JsonElement headers() {
        return headers;
    }

    final Object content;
    final JsonElement headers;

    public Response(Integer statusCode, Object content, JsonElement headers) {
        this.statusCode = statusCode;
        this.content = content;
        this.headers = headers;
    }
    public static Response empty() {
        return new Response(null, null, null);
    }
}
