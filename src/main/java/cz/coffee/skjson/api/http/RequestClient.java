package cz.coffee.skjson.api.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.FileWrapper;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.skript.request.RequestUtil;
import cz.coffee.skjson.utils.LoggingUtil;
import cz.coffee.skjson.utils.TimerWrapper;
import org.eclipse.jetty.client.*;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MultiPart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cz.coffee.skjson.api.ConfigRecords.*;
import static cz.coffee.skjson.api.http.RequestClientUtils.changeExtension;
import static cz.coffee.skjson.api.http.RequestClientUtils.colorizedMethod;

public class RequestClient implements AutoCloseable {
    private static final String USER_AGENT_FORMAT = "SkJson-requests-%s";
    private String uri;
    private HttpClient client;
    private Request request;
    private TimerWrapper timer;
    private final LinkedList<File> attachments = new LinkedList<>();
    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().disableJdkUnsafe().serializeNulls().setLenient().create();

    public RequestClient(String uri) {
        try {
            this.uri = uri;
            this.client = new HttpClient();
            this.client.start();
            this.timer = new TimerWrapper(0);
        } catch (Exception ex) {
            if (PROJECT_DEBUG) LoggingUtil.requestLog(ex.getMessage());
        }
    }

    private boolean isOk(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }


    private boolean done = false;

    @SuppressWarnings("unused")
    private boolean isDone() {
        return this.done;
    }

    public RequestClient method(String method) {
        this.request = this.client.newRequest(this.uri);
        switch (method.toUpperCase()) {
            case "GET", "MOCK" -> this.request.method("GET");
            case "POST" -> this.request.method("POST");
            case "PUT" -> this.request.method("PUT");
            case "DELETE" -> this.request.method("DELETE");
            case "PATCH" -> this.request.method("PATCH");
            case "HEAD" -> this.request.method("HEAD");
        }

        String userAgent = String.format(USER_AGENT_FORMAT, 2);
        this.request.headers((x) -> {
            x.remove("User-agent");
            x.add("User-agent", userAgent);
        });

        return this;
    }


    public RequestClient put() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("PUT");
        return this;
    }


    public RequestClient test() {
        try {
            this.request.timeout(15L, TimeUnit.SECONDS);
            ContentResponse rsp = this.request.send();
            if (isOk(rsp.getStatus())) {
                return this;
            } else {
                return null;
            }
        } catch (Exception ex) {
            if (PROJECT_DEBUG) {
                String errorMessage = String.format("Error occurred during test(): %s", ex.getMessage());
                LoggingUtil.enchantedError(ex, ex.getStackTrace(), errorMessage);
            }
            return null;
        }
    }

    public CompletableFuture<RequestResponse> request(boolean... lenientI) {
        final boolean lenient = lenientI != null && lenientI.length > 0 && lenientI[0];
        final CompletableFuture<RequestResponse> future = new CompletableFuture<>();
        ByteArrayOutputStream streamByte = new ByteArrayOutputStream();

        try {
            Response.ContentListener contentListener = (response, byteBuffer) -> {
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                try {
                    streamByte.write(bytes);
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            };

            Response.SuccessListener successListener = (response) -> this.done();

            Response.CompleteListener completeListener = (result) -> {
                if (result.isSucceeded()) {
                    Charset charset = StandardCharsets.UTF_8;
                    String text = streamByte.toString(charset);
                    var response = result.getResponse();
                    var request = result.getRequest();

                    var serverResponse = RequestResponse.of(response.getHeaders(), request.getURI(), text, response.getStatus(), lenient);
                    future.complete(serverResponse);
                    if (LOGGING_LEVEL > 1)
                        LoggingUtil.log(String.format(REQUESTS_PREFIX + ": " + colorizedMethod(request.getMethod()) + " request was send to &b'%s'&r and takes %s", request.getURI(), timer.toHumanTime()));
                }
                done = true;
            };

            Response.FailureListener failureListener = (response, failure) -> {
                if (PROJECT_DEBUG && LOGGING_LEVEL > 2)
                    LoggingUtil.enchantedError(failure, failure.getStackTrace(), "In FailureListener");
                this.done();
                future.completeExceptionally(new IllegalStateException("HTTP request failed"));
            };

            this.request.onResponseContent(contentListener);
            this.request.onResponseFailure(failureListener);
            this.request.send(completeListener);
            this.request.onResponseSuccess(successListener);
        } catch (Exception ex) {
            LoggingUtil.requestLog(ex.getMessage());
            future.completeExceptionally(ex);
        }

        return future;
    }


    public RequestClient setContent(final JsonElement body) {
        this.request.body(new StringRequestContent(GSON.toJson(body)));
        return this;
    }

    public RequestClient addHeaders(WeakHashMap<String, String> map) {
        if (this.request != null) {
            this.request.headers((x) -> map.forEach(x::add));
        }
        return this;
    }

    public RequestClient setHeaders(RequestUtil.Pairs[] pairs) {
        if (this.request != null & pairs != null) {
            this.request.headers((x) -> Arrays.stream(pairs).forEach((p) -> x.add(p.getKey(), p.getValue())));
        }
        return this;
    }

    public RequestClient setHeaders(JsonElement[] coll) {
        if (this.request == null) {
            return this;
        }
        this.request.headers(x -> Arrays.stream(coll).toList().parallelStream().forEach(c -> {
            if (c instanceof JsonObject o) {
                o.entrySet().parallelStream().forEach(entry -> {
                    String value = ParserUtil.jsonToType(entry.getValue());
                    if (!entry.getKey().isBlank() || !entry.getKey().isEmpty())
                        x.add(entry.getKey().trim(), value.trim());
                });
            }
        }));
        return this;
    }


    @SuppressWarnings("UnusedReturnValue")
    public Optional<RequestClient> addAttachment(String pathToAttachment) {
        File file;
        if (pathToAttachment.startsWith("*")) {
            file = FileWrapper.serchFile(pathToAttachment.replaceAll("[*/]", ""));
        } else file = new File(pathToAttachment);
        try {
            if (file.getName().endsWith(".sk")) file = changeExtension(file, ".vb");
        } catch (IOException exception) {
            if (PROJECT_DEBUG) LoggingUtil.requestLog(exception.getMessage());
        }
        if (file.exists()) attachments.add(file);
        return Optional.of(this);
    }

    public RequestClient postAttachments(JsonElement body) {
        this.postAttachments(GSON.toJson(body));
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Optional<RequestClient> postAttachments(String body) {
        AtomicInteger i = new AtomicInteger(0);
        try (var mpr = new MultiPartRequestContent()) {
            attachments.forEach(attachment -> {
                try {
                    mpr.addPart(new MultiPart.PathPart("file" + i.incrementAndGet(), attachment.getName(), HttpFields.EMPTY, attachment.toPath()));
                } catch (Exception e) {
                    if (PROJECT_DEBUG) LoggingUtil.error(e.getMessage());
                }
            });
            mpr.addPart(new MultiPart.ContentSourcePart("payload_json", null, HttpFields.EMPTY, new StringRequestContent(body, StandardCharsets.UTF_8)));
            this.request.body(mpr);
        } catch (Exception ex) {
            if (PROJECT_DEBUG) LoggingUtil.requestLog(ex.getMessage());
        }
        return Optional.of(this);
    }

    private void done() {
        if (this.isDone()) {
            try {
                this.client.stop();
            } catch (Exception e) {
                if (PROJECT_DEBUG) LoggingUtil.error(e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        this.done();
    }
}
