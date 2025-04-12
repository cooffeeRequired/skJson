package cz.coffeerequired.api.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.requests.Attachment;
import cz.coffeerequired.api.requests.Pairs;
import cz.coffeerequired.api.requests.RequestMethod;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings("unused")
public class RequestClient implements AutoCloseable {

    private final HttpClient httpClient;
    private final Gson gson;
    private HttpRequest.Builder requestBuilder;
    private HttpRequest.BodyPublisher bodyPublisher; // Keep track of the actual body publisher

    public RequestClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new Gson();
        
        SkJson.debug("HTTP Client initialized with HTTP/1.1");
    }

    public RequestClient setUri(String uri) {
        this.requestBuilder = HttpRequest.newBuilder(URI.create(uri));

        SkJson.debug("URI set to %s", uri);

        return this;
    }

    public Object getUri() {
        return this.requestBuilder;
    }

    public RequestClient method(String method) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("URI must be set before setting the method.");
        }
        this.requestBuilder.method(method, bodyPublisher != null ? bodyPublisher : HttpRequest.BodyPublishers.noBody());
        return this;
    }

    public RequestClient method(RequestMethod method) {
        return method(method.toString().toUpperCase());
    }

    public void setJsonBody(JsonElement body) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }
        this.bodyPublisher = body == null ?
                HttpRequest.BodyPublishers.noBody() :
                HttpRequest.BodyPublishers.ofString(gson.toJson(body));

        this.requestBuilder.header("Content-Type", "application/json")
                .POST(this.bodyPublisher);
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
            if ("Content-Type".equalsIgnoreCase(key) && this.requestBuilder.build().headers().firstValue(key).isPresent()) {
                return;
            }
            this.requestBuilder.header(key, value);
        });
        return this;
    }

    public RequestClient addHeader(Pairs[] pairs) {
        if (this.requestBuilder == null) {
            throw new IllegalStateException("Request builder is not initialized.");
        }

        if (pairs == null || pairs.length == 0) return this;

        Arrays.stream(pairs).forEach(pair -> {
            if ("Content-Type".equalsIgnoreCase(pair.getKey()) && this.requestBuilder.build().headers().firstValue(pair.getKey()).isPresent()) {
                return;
            }
            this.requestBuilder.header(pair.getKey(), pair.getValue());
        });
        return this;
    }

    public void setAttachments(LinkedList<Attachment> attachments) {

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

        try {
            CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            
            future.exceptionally(throwable -> {
                SkJson.exception(throwable, "Failed to process response from %s", request.uri());
                return null;
            });
            
            return future;
        } catch (Exception e) {
            SkJson.exception(e, "Failed to send request to %s", request.uri());
            throw e;
        }
    }


    @Override
    public void close() {
        this.bodyPublisher = null;
        this.requestBuilder = null;
        this.httpClient.close();
    }
}
