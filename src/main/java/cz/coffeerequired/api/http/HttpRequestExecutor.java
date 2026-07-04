package cz.coffeerequired.api.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.requests.*;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;

/**
 * Builds and sends prepared {@link Request} instances using the shared HTTP client.
 */
public final class HttpRequestExecutor {

    private static final Gson GSON = new Gson();

    private HttpRequestExecutor() {
    }

    public static RequestResponse sendBlocking(Request request) {
        if (request.getMethod() == RequestMethod.MOCK) {
            SkJson.debug("HTTP MOCK %s (local, no network)", buildUri(request));
            return RequestResponse.synthetic(200, "{\"mock\":true}", true);
        }
        try {
            HttpRequest httpRequest = buildHttpRequest(request);
            if (httpRequest == null) {
                return null;
            }
            HttpResponse<String> response = HttpClientProvider.getClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return RequestResponse.of(response);
        } catch (Exception e) {
            logSendFailure(request, e);
            return null;
        }
    }

    /**
     * Sends asynchronously. The callback runs on the HTTP worker thread (not the main thread).
     */
    public static void sendAsync(Request request, BiConsumer<RequestResponse, Throwable> callback) {
        if (request.getMethod() == RequestMethod.MOCK) {
            SkJson.debug("HTTP MOCK %s (local, no network)", buildUri(request));
            callback.accept(RequestResponse.synthetic(200, "{\"mock\":true,\"url\":\"" + request.getUri() + "\"}", true), null);
            return;
        }
        try {
            HttpRequest httpRequest = buildHttpRequest(request);
            if (httpRequest == null) {
                callback.accept(null, new IllegalStateException("Could not build HTTP request"));
                return;
            }

            HttpClientProvider.getClient()
                    .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .whenComplete((response, error) -> {
                        if (error != null) {
                            logSendFailure(request, error);
                            callback.accept(null, error);
                            return;
                        }
                        try {
                            callback.accept(RequestResponse.of(response), null);
                        } catch (Exception e) {
                            logSendFailure(request, e);
                            callback.accept(null, e);
                        }
                    });
        } catch (Exception e) {
            logSendFailure(request, e);
            callback.accept(null, e);
        }
    }

    public static void applyResponse(Request request, RequestResponse result, Throwable error) {
        if (error != null || result == null) {
            request.setStatus(RequestStatus.FAILED);
            request.setResponse(Response.empty());
            return;
        }
        RequestStatus status = result.isSuccessful() ? RequestStatus.OK : RequestStatus.FAILED;
        Response response = new Response(
                result.getStatusCode(),
                result.getBodyContent(true),
                result.getResponseHeader().toJson(),
                status
        );
        request.setResponse(response);
        request.setStatus(status);
    }

    public static void prepareEvent(Request request) {
        if (request.getEvent() == null) {
            request.setEvent(new HttpReceivedResponse());
        }
    }

    public static String buildUri(Request request) {
        try {
            StringBuilder uriBuilder = new StringBuilder(request.getUri());

            if (!uriBuilder.toString().matches("^[a-zA-Z]+://.*")) {
                uriBuilder.insert(0, "http://");
            }

            if (!request.getQueryParams().isEmpty()) {
                char separator = uriBuilder.indexOf("?") >= 0 ? '&' : '?';
                uriBuilder.append(separator);

                request.getQueryParams().forEach((key, value) ->
                        uriBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                                .append("=")
                                .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                                .append("&"));
                uriBuilder.setLength(uriBuilder.length() - 1);
            }
            return URI.create(uriBuilder.toString()).normalize().toString().replace("§", "&");
        } catch (Exception ex) {
            SkJson.exception(ex, "Error building request URI");
            return null;
        }
    }

    private static HttpRequest buildHttpRequest(Request request) {
        String url = buildUri(request);
        if (url == null) {
            SkJson.severe("Request URI could not be built. Check URI and query parameters.");
            return null;
        }

        SkJson.debug("HTTP %s %s", request.getMethod(), url);
        SkJson.debug("HTTP headers: %s", request.getHeader() != null ? Arrays.toString(request.getHeader()) : "none");

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(HttpClientProvider.requestTimeoutSeconds()));

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

        if (!request.getAttachments().isEmpty()) {
            if (request.getMethod() == RequestMethod.GET) {
                if (request.getContent() != null) {
                    SkJson.warning("GET request to %s cannot include a body or attachments", url);
                }
                return null;
            }
            try {
                var mpd = MimeMultipartData.newBuilder().addContent(request.getContent());
                request.getAttachments().forEach(attachment ->
                        mpd.addFile(attachment.path(), attachment.file().toPath(), MimeMultipartData.FileType.AUTOMATIC));
                MimeMultipartData multipart = mpd.build();
                bodyPublisher = multipart.getBodyPublisher();
                builder.header("Content-Type", multipart.getContentType());
            } catch (java.io.IOException e) {
                SkJson.exception(e, "Failed to build multipart body for %s", url);
                return null;
            }
        } else if (request.getMethod() != RequestMethod.GET && request.getContent() != null && !request.getContent().isJsonNull()) {
            bodyPublisher = HttpRequest.BodyPublishers.ofString(GSON.toJson(request.getContent()));
            if (request.getHeader() == null || Arrays.stream(request.getHeader()).noneMatch(p -> "Content-Type".equalsIgnoreCase(p.getKey()))) {
                builder.header("Content-Type", "application/json");
            }
        } else if (request.getMethod() == RequestMethod.GET && request.getContent() != null) {
            SkJson.warning("GET request to %s ignores body content", url);
        }

        builder.method(request.getMethod().name(), bodyPublisher);
        addHeaders(builder, request.getHeader());
        return builder.build();
    }

    private static void addHeaders(HttpRequest.Builder builder, Pairs[] headers) {
        if (headers == null) {
            return;
        }
        for (Pairs pair : headers) {
            if (pair == null || pair.getKey() == null || pair.getKey().isEmpty()) {
                continue;
            }
            builder.header(pair.getKey(), pair.getValue() != null ? pair.getValue() : "");
        }
    }

    private static void logSendFailure(Request request, Throwable e) {
        if (e instanceof TimeoutException) {
            SkJson.severe("HTTP request timed out after %ss: %s", HttpClientProvider.requestTimeoutSeconds(), request.getUri());
        } else if (e.getCause() instanceof java.net.ConnectException) {
            SkJson.severe("HTTP connection failed for %s: %s", request.getUri(), e.getCause().getMessage());
        } else {
            SkJson.severe("HTTP request failed for %s: %s", request.getUri(), e.getMessage());
        }
        SkJson.exception(e, "HTTP request error");
    }
}
