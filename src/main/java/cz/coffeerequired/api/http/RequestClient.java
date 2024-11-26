package cz.coffeerequired.api.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class RequestClient implements AutoCloseable {

    private final HttpClient httpClient;
    private final Gson gson;
    private HttpRequest.Builder requestBuilder;
    private HttpRequest.BodyPublisher bodyPublisher; // Keep track of the actual body publisher

    public RequestClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.gson = new Gson();
    }

    public RequestClient setUri(String uri) {
        this.requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri));
        return this;
    }

    public RequestClient method(String method) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("URI must be set before setting the method.");
        }
        this.requestBuilder.method(method, bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody());
        return this;
    }

    public RequestClient setJsonBody(JsonElement body) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        this.bodyPublisher = HttpRequest.BodyPublishers.ofString(gson.toJson(body));
        this.requestBuilder.header("Content-Type", "application/json")
                .POST(this.bodyPublisher);
        return this;
    }

    public RequestClient setBodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        this.bodyPublisher = bodyPublisher;
        this.requestBuilder.POST(bodyPublisher);
        return this;
    }

    public RequestClient addHeaders(Map<String, String> headers) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        headers.forEach((key, value) -> {
            if ("Content-Type".equalsIgnoreCase(key) && this.requestBuilder.build().headers().firstValue(key).isPresent()) {
                return;
            }
            this.requestBuilder.header(key, value);
        });
        return this;
    }

    public HttpResponse<String> send() throws Exception {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        HttpRequest request = this.requestBuilder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> sendAsync() {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        HttpRequest request = this.requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }


    @Override
    public void close() {
        this.bodyPublisher = null;
        this.requestBuilder = null;
        this.httpClient.close();
    }
}
