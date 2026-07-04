package cz.coffeerequired.api.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.requests.*;
import org.bukkit.Bukkit;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * Fluent builder for one-off HTTP calls (e.g. loading JSON from a URL).
 * Plugin HTTP requests should use {@link HttpRequestExecutor} instead.
 */
@SuppressWarnings("unused")
public class RequestClient implements AutoCloseable {

    private static final Gson GSON = new Gson();

    private HttpRequest.Builder requestBuilder;
    private HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

    public RequestClient setUri(String uri) {
        this.requestBuilder = HttpRequest.newBuilder(URI.create(uri))
                .timeout(java.time.Duration.ofSeconds(HttpClientProvider.requestTimeoutSeconds()));
        return this;
    }

    public RequestClient method(String method) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("URI must be set before setting the method.");
        }
        this.requestBuilder.method(method, bodyPublisher);
        return this;
    }

    public RequestClient method(RequestMethod method) {
        return method(method.toString().toUpperCase());
    }

    public void setJsonBody(JsonElement body) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        this.bodyPublisher = body == null || body.isJsonNull()
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(GSON.toJson(body));
        this.requestBuilder.method("POST", this.bodyPublisher);
    }

    public void setBodyPublisher(HttpRequest.BodyPublisher bodyPublisher) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        this.bodyPublisher = bodyPublisher;
        this.requestBuilder.POST(bodyPublisher);
    }

    public RequestClient addHeaders(Map<String, String> headers) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        headers.forEach((key, value) -> {
            if (!"Content-Type".equalsIgnoreCase(key)) {
                this.requestBuilder.header(key, value);
            }
        });
        return this;
    }

    public RequestClient addHeader(Pairs[] pairs) {
        if (this.requestBuilder == null || pairs == null) {
            return this;
        }
        for (Pairs pair : pairs) {
            if (pair != null && pair.getKey() != null && !pair.getKey().isEmpty()) {
                this.requestBuilder.header(pair.getKey(), pair.getValue() != null ? pair.getValue() : "");
            }
        }
        return this;
    }

    public HttpResponse<String> send() throws Exception {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        return HttpClientProvider.getClient().send(this.requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public java.util.concurrent.CompletableFuture<HttpResponse<String>> sendAsync() {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        return HttpClientProvider.getClient().sendAsync(this.requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Override
    public void close() {
        this.bodyPublisher = null;
        this.requestBuilder = null;
    }

    public static class Handler {
        private final Request request;

        public Handler(Request request) {
            this.request = request;
        }

        /**
         * Blocking send — runs on Skript's async effect thread.
         * Use {@code last response of {_request}}; does not fire {@code on http response}.
         */
        public void run() {
            RequestResponse result = HttpRequestExecutor.sendBlocking(request);
            HttpRequestExecutor.applyResponse(request, result, null);
        }

        /**
         * Non-blocking send — returns immediately; fires {@code on http response} on the main thread when done.
         */
        public void runAsNonBlocking() {
            HttpRequestExecutor.prepareEvent(request);
            HttpRequestExecutor.sendAsync(request, (result, error) ->
                    Bukkit.getScheduler().runTask(SkJson.getInstance(), () -> {
                        HttpRequestExecutor.applyResponse(request, result, error);
                        if (request.getEvent() != null) {
                            request.getEvent().callEvent();
                        }
                    }));
        }
    }
}
