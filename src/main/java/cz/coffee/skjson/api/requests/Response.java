package cz.coffee.skjson.api.requests;

import com.google.gson.JsonElement;

public record Response(Integer statusCode, Object content, JsonElement headers) {
    public static Response empty() {
        return new Response(null, null, null);
    }
}
