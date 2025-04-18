package cz.coffeerequired.api.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.requests.*;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@SuppressWarnings("unused")
public class RequestClient implements AutoCloseable {

    public static final ExecutorService threadPool =
        new ThreadPoolExecutor(
                Api.Records.HTTP_MAX_THREADS,                 // corePoolSize
                Api.Records.HTTP_MAX_THREADS * 4,             // maximumPoolSize
                60L, TimeUnit.SECONDS,                        // keep-alive time
                new SynchronousQueue<>(),                     // work queue
                r -> {                                        // thread factory
                    Thread thread = new Thread(r);
                    thread.setName("SkJson-HTTP-" + thread.threadId());
                    thread.setDaemon(true);
                    return thread;
                },
                (r, executor) -> {
                    SkJson.severe("HTTP task %s was rejected", r);
                    SkJson.severe("Executor tasks %s", executor.getTaskCount());
                }
        );

    private final HttpClient httpClient;
    private final Gson gson;
    private HttpRequest.Builder requestBuilder;
    private HttpRequest.BodyPublisher bodyPublisher; // Keep track of the actual body publisher

    public RequestClient() {
        this.httpClient = HttpClient.newBuilder()
                .executor(threadPool)  // Use our shared cached thread pool
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.gson = new Gson();

        SkJson.debug("HTTP Client initialized with shared thread pool");
    }

    public Object getUri() {
        return this.requestBuilder;
    }

    public RequestClient setUri(String uri) {
        this.requestBuilder = HttpRequest.newBuilder(URI.create(uri));
        return this;
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
            Thread currentThread = Thread.currentThread();
            ThreadGroup group = currentThread.getThreadGroup();
            SkJson.debug("Active threads before sending request: " + group.activeCount() +
                    ", Current thread: " + currentThread.getName());

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

    public static class Handler {
        private final Request request;

        public Handler(Request request, Event event) {
            this.request = request;
        }

        public void runAsNonBlocking() {
            var future = CompletableFuture.supplyAsync(this::execute, threadPool);
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    SkJson.severe("Error in request execution: %s", throwable.getMessage());
                    request.setStatus(RequestStatus.FAILED);
                    request.setResponse(Response.empty());
                    return;
                }

                Bukkit.getScheduler().runTask(SkJson.getInstance(), () -> {
                    try {
                        if (result != null) {
                            Response rsp = new Response(result.getStatusCode(), result.getBodyContent(true), result.getResponseHeader().toJson(), RequestStatus.OK);
                            request.setResponse(rsp).setStatus(RequestStatus.OK);
                            request.getEvent().callEvent();
                        }
                    } catch (Exception e) {
                        SkJson.severe("Error processing response: %s", e.getMessage());
                        request.setResponse(Response.empty()).setStatus(RequestStatus.FAILED);
                    }
                });
            });
        }

        public void run() {
            RequestResponse response = this.execute();

            if (response != null) {
                var rsp = new Response(response.getStatusCode(), response.getBodyContent(true), response.getResponseHeader().toJson(), RequestStatus.OK);
                request.setResponse(rsp).setStatus(RequestStatus.OK);
            } else {
                SkJson.severe("Error processing response");
                request.setResponse(Response.empty()).setStatus(RequestStatus.FAILED);
            }
        }


        private RequestResponse execute() {
            String url = buildUri();

            if (url == null) {
                SkJson.severe("Request URI could not be built. Check request URI or query parameters.");
                return null;
            }

            request.setEvent(new HttpReceivedResponse());


            try (var client = new RequestClient()) {
                SkJson.debug("Preparing to send HTTP request.");
                SkJson.debug("Request URI: %s", url);
                SkJson.debug("Request Method: %s", request.getMethod());
                SkJson.debug("Request Headers: %s", Arrays.toString(request.getHeader()));
                SkJson.debug("Request Content: %s", request.getContent());


                client.setUri(url);

                if (request.getAttachments().isEmpty()) {
                    client.setAttachments(request.getAttachments());
                    var mpd = MimeMultipartData.newBuilder().addContent(request.getContent());
                    request.getAttachments().forEach(attachment ->
                            mpd.addFile(
                                    attachment.path(),
                                    attachment.file().toPath(),
                                    MimeMultipartData.FileType.AUTOMATIC
                            )
                    );

                    if (!request.getMethod().equals(RequestMethod.GET)) {
                        var mimeMultipartData = mpd.build();
                        client.setBodyPublisher(mimeMultipartData.getBodyPublisher());
                        client.addHeader(new Pairs[]{new Pairs("Content-Type:" + mimeMultipartData.getContentType())});
                    } else if (request.getContent() != null) {
                        SkJson.warning("Sending request with method GET, method GET doesn't support body!");
                        return null;
                    }
                } else {
                    if (!request.getMethod().equals(RequestMethod.GET)) {
                        client.setJsonBody(request.getContent());
                    } else if (request.getContent() != null) {
                        SkJson.warning("Sending request to " + url + " with method GET, method GET doesn't support body!");
                    }
                }

                var clientResponse = client.method(request.getMethod())
                        .addHeader(request.getHeader())
                        .sendAsync()
                        .get(10, TimeUnit.SECONDS);

                return RequestResponse.of(clientResponse);
            } catch (ExecutionException ee) {
                // ExecutionException wraps the actual cause, often a networking issue
                Throwable cause = ee.getCause();
                if (cause instanceof java.net.ConnectException) {
                    SkJson.severe("Connection failed: " + cause.getMessage());
                    SkJson.severe("Possible causes: server is offline, wrong port, firewall, or incorrect URI.");
                } else {
                    SkJson.severe("Unexpected execution error while sending request: " + cause);
                }
                SkJson.exception(ee, "ExecutionException during request execution.");
                return null;

            } catch (IOException ioe) {
                SkJson.severe("I/O error during request: %s", ioe.getMessage());
                SkJson.severe("Check server availability, request body validity, or file attachments.");
                SkJson.exception(ioe, "IOException during request execution.");
                return null;
            } catch (Exception ex) {
                SkJson.severe("Unhandled exception during request: %s - %s", ex.getClass().getSimpleName(), ex.getMessage());
                SkJson.exception(ex, "Unhandled error in sendRequest");
                return null;
            }
        }

        private String buildUri() {
            try {
                StringBuilder uriBuilder = new StringBuilder(request.getUri());

                if (!uriBuilder.toString().matches("^[a-zA-Z]+://.*")) {
                    uriBuilder.insert(0, "http://");
                }

                if (!request.getQueryParams().isEmpty()) {
                    char separator = uriBuilder.indexOf("?") >= 0 ? '&' : '?';
                    uriBuilder.append(separator);

                    request.getQueryParams().forEach((key, value) -> {
                        try {
                            uriBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                                    .append("=")
                                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                                    .append("&");
                        } catch (Exception e) {
                            throw new RuntimeException("Error encoding query parameters", e);
                        }
                    });
                    uriBuilder.setLength(uriBuilder.length() - 1);
                }
                var uri = URI.create(uriBuilder.toString());
                return uri.normalize().toString().replaceAll("§", "&");
            } catch (Exception ex) {
                SkJson.exception(ex, "Error building URI", ex);
                return null;
            }
        }
    }
}
