package cz.coffeerequired.api.requests;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

public record Response(Integer statusCode, Object content, JsonElement headers, RequestStatus status) {
    public static Response empty() {
        return new Response(null, null, null, RequestStatus.UNKNOWN);
    }

    @Override
    public @NotNull String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", content=" + shorten(content) +
                ", headers=" + shorten(headers) +
                ", status=" + status +
                '}';
    }

    private String shorten(Object value) {
        if (value == null) return "null";
        String str = value.toString();
        return str.length() > 8 ? str.substring(0, 8) + "..." : str;
    }

}
