package cz.coffeerequired.skript.http.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.http.MimeMultipartData;
import cz.coffeerequired.api.http.RequestClient;
import cz.coffeerequired.api.http.RequestResponse;
import cz.coffeerequired.api.requests.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;


@Name("Send created/prepared request")
@Examples("send prepared {_request}")
@Description("Send prepared/created request to the given method and uri")
@Since("2.9.9-pre Api Changes")
@ApiStatus.Experimental
public class EffSendRequest extends Effect {

    private static final Field DELAYED;
    private static final ExecutorService threadPool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static {
        Field _DELAYED = null;
        try {
            _DELAYED = Delay.class.getDeclaredField("DELAYED");
            _DELAYED.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Skript.error("Skript's 'delayed' method could not be resolved. Some Skript warnings may not be available.");
        }
        DELAYED = _DELAYED;
    }

    Expression<Request> exprRequest;
    boolean sync;

    @Override
    protected void execute(@NotNull Event event) {
        var request = exprRequest.getSingle(event);
        if (request == null) {
            SkJson.severe("Request is null");
            return;
        }

        if (sync) {
            handleSyncRequest(request);
        } else {
            handleAsyncRequest(event, request);
        }
    }

    private void handleSyncRequest(Request request) {
        var response = sendRequest(request);
        if (response != null) {
            var rsp = new Response(response.getStatusCode(), response.getBodyContent(true), response.getResponseHeader().toJson());
            request.setResponse(rsp);
        } else {
            request.setResponse(Response.empty());
        }
    }

    private void handleAsyncRequest(Event event, Request request) {
        var vars = Variables.copyLocalVariables(event);
        CompletableFuture.supplyAsync(() -> {
            try {
                return sendRequest(request);
            } catch (Exception e) {
                SkJson.severe("Error in request execution: " + e.getMessage());
                request.setStatus(RequestStatus.FAILED);
                request.setResponse(Response.empty());
                return null;
            }
        }, threadPool)
        .whenComplete((resp, err) -> Bukkit.getScheduler().runTask(SkJson.getInstance(), () -> {
            try {
                if (err != null) {
                    SkJson.exception(err, "Error sending request asynchronously");
                    request.setStatus(RequestStatus.FAILED);
                    request.setResponse(Response.empty());
                } else if (resp != null) {
                    var rsp = new Response(resp.getStatusCode(), resp.getBodyContent(true), resp.getResponseHeader().toJson());
                    request.setResponse(rsp);
                    request.setStatus(RequestStatus.OK);
                } else {
                    request.setResponse(Response.empty());
                    request.setStatus(RequestStatus.FAILED);
                }
            } catch (Exception e) {
                SkJson.severe("Error processing response: " + e.getMessage());
                request.setResponse(Response.empty());
                request.setStatus(RequestStatus.FAILED);
            } finally {
                Variables.setLocalVariables(event, vars);
                if (getNext() != null) {
                    TriggerItem.walk(getNext(), event);
                }
            }
        }));
    }

    @Override
    protected TriggerItem walk(@NotNull Event e) {
        var rq = this.exprRequest.getSingle(e);
        if (rq == null) return null;
        debug(e, true);
        if (!sync) delay(e);
        if (!sync) execute(e);
        if (sync) return super.walk(e);
        return null;
    }

    private RequestResponse sendRequest(Request request) {
        URI url = buildUri(request);
        if (url == null) {
            SkJson.severe("Request URI could not be built. Check request URI or query parameters.");
            return null;
        }

        try (RequestClient client = new RequestClient()) {
            // Log base request details for debugging
            SkJson.debug("Preparing to send HTTP request.");
            SkJson.debug("Request URI: %s", url);
            SkJson.debug("Request Method: " + request.getMethod());
            SkJson.debug("Request Headers: " + Arrays.toString(request.getHeader()));
            SkJson.debug("Request Content: " + request.getContent());

            setupClient(client, request, url);

            SkJson.debug("Request client successfully set up. Executing request...");
            return executeRequest(client, request, url);

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

        } catch (InterruptedException ie) {
            SkJson.severe("Request thread was interrupted: " + ie.getMessage());
            SkJson.exception(ie, "InterruptedException during request execution.");
            Thread.currentThread().interrupt(); // restore interrupted status
            return null;

        } catch (IOException ioe) {
            SkJson.severe("I/O error during request: " + ioe.getMessage());
            SkJson.severe("Check server availability, request body validity, or file attachments.");
            SkJson.exception(ioe, "IOException during request execution.");
            return null;

        } catch (Exception ex) {
            SkJson.severe("Unhandled exception during request: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            SkJson.exception(ex, "Unhandled error in sendRequest");
            return null;
        }
    }


    private URI buildUri(Request request) {
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

            return URI.create(uriBuilder.toString()).normalize();
        } catch (Exception ex) {
            SkJson.exception(ex, "Error building URI", ex);
            return null;
        }
    }


    private void setupClient(RequestClient client, Request request, URI url) throws IOException {
        client.setUri(url.toString().replaceAll("§", "&"));

        SkJson.debug("Setting up HTTP client., attachments: %s", request.getAttachments());

        if (!request.getAttachments().isEmpty()) {
            handleAttachments(client, request);
        } else {
            if (!request.getMethod().equals(RequestMethod.GET)) {
                client.setJsonBody(request.getContent());
            } else if(request.getContent() != null) {
                SkJson.warning("Sending request to " + url + " with method GET, method GET doesn't support body!");
            }
        }
    }

    private void handleAttachments(RequestClient client, Request request) throws IOException {

        SkJson.debug("handleAttachments");

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
        } else if(request.getContent() != null) {
            SkJson.warning("Sending request with method GET, method GET doesn't support body!");
        }
    }

    private RequestResponse executeRequest(RequestClient client, Request request, URI url) throws Exception {
        SkJson.debug("Sending request to " + client.getUri());

        try {
            var rsp = RequestResponse.of(client.method(request.getMethod())
                    .setUri(url.toString())
                    .addHeader(request.getHeader())
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS));

            SkJson.debug("Response received: " + rsp);
            return rsp;
        } catch (Exception e) {
            SkJson.severe("Error executing request to " + request.getUri() + ": " + e.getMessage());
            request.setStatus(RequestStatus.FAILED);
            throw e;
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "execute prepared " + this.exprRequest.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    private void delay(Event e) {
        if (DELAYED != null) {
            try {
                ((Set<Event>) DELAYED.get(null)).add(e);
            } catch (IllegalAccessException illegalAccessException) {
                SkJson.exception(illegalAccessException, "Error accessing delayed events");
            }
        }
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        exprRequest = defendExpression(expressions[0]);
        sync = parseResult.hasTag("sync");
        return canInitSafely(exprRequest);
    }
}