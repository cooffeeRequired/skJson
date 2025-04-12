package cz.coffeerequired.api.http;

import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;

@SuppressWarnings("unused")
public interface RequestResponse {

    static RequestResponse of(HttpResponse<String> response) {
        return new RequestResponse() {

            @Override
            public RequestHeaders getResponseHeader() {
                return new RequestHeaders(response.headers());
            }

            @Override
            public int getStatusCode() {
                return response.statusCode();
            }

            @Override
            public Object getBodyContent(boolean saveIncorrect) {
                String body = response.body();
                try {
                    return JsonParser.parseString(body);
                } catch (Exception e) {
                    if (!saveIncorrect) {
                        return "Invalid JSON Response: " + body;
                    }
                    return body;
                }
            }

            @Override
            public Optional<URI> getRequestURI() {
                return Optional.ofNullable(response.uri());
            }

            @Override
            public boolean isSuccessful() {
                int statusCode = response.statusCode();
                return statusCode >= 200 && statusCode < 500;
            }

            @Override
            public String toString() {
                return "RequestResponse{statusCode=" + response.statusCode() + ", body=" + response.body() + "}";
            }
        };
    }

    RequestHeaders getResponseHeader();

    int getStatusCode();

    Object getBodyContent(boolean saveIncorrect);

    Optional<URI> getRequestURI();

    boolean isSuccessful();
}
