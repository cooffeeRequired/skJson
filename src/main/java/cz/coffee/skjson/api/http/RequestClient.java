package cz.coffee.skjson.api.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.FileHandler;
import cz.coffee.skjson.api.requests.Attachment;
import cz.coffee.skjson.api.requests.Pairs;
import cz.coffee.skjson.parser.ParserUtil;
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

import static cz.coffee.skjson.api.ConfigRecords.*;
import static cz.coffee.skjson.api.http.RequestClientUtils.changeExtension;
import static cz.coffee.skjson.api.http.RequestClientUtils.colorizedMethod;
import static cz.coffee.skjson.utils.Logger.*;

public class RequestClient implements AutoCloseable {
    private final LinkedList<File> attachments = new LinkedList<>();
    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().disableJdkUnsafe().serializeNulls().setLenient().create();
    private String uri;
    private HttpClient client;
    private Request request;
    private TimerWrapper timer;
    private boolean done = false;

    public RequestClient(String uri) {
        try {
            this.uri = uri;
            this.client = new HttpClient();
            this.client.start();
            this.timer = new TimerWrapper(0);
        } catch (Exception ex) {
            if (PROJECT_DEBUG) requestLog(ex.getMessage());
        }
    }

    private boolean isOk(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    @SuppressWarnings("unused")
    private boolean isDone() {
        return this.done;
    }

    public RequestClient method(String method) {
        this.request = this.client.newRequest(this.uri);
        this.request.headers((it) -> it.add("User-Agent", "SkJson 3.0.3/Client Jetty"));
        switch (method.toUpperCase()) {
            case "GET", "MOCK" -> this.request.method("GET");
            case "POST" -> this.request.method("POST");
            case "PUT" -> this.request.method("PUT");
            case "DELETE" -> this.request.method("DELETE");
            case "PATCH" -> this.request.method("PATCH");
            case "HEAD" -> this.request.method("HEAD");
        }
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
                error(ex);
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
                        info(
                                "%s: %s request was send to &b'%s'&r and takes %s",
                                REQUESTS_PREFIX, colorizedMethod(request.getMethod()), request.getURI(), timer.toHumanTime()
                        );
                }
                done = true;
            };

            Response.FailureListener failureListener = (response, failure) -> {
                if (PROJECT_DEBUG && LOGGING_LEVEL > 2)
                    error(failure);
                this.done();
                future.completeExceptionally(new IllegalStateException("HTTP request failed"));
            };

            this.request.onResponseContent(contentListener);
            this.request.onResponseFailure(failureListener);
            this.request.send(completeListener);
            this.request.onResponseSuccess(successListener);
        } catch (Exception ex) {
            requestLog(ex.getMessage());
            future.completeExceptionally(ex);
        }

        return future;
    }


    public RequestClient setContent(final JsonElement body) {
        if (body != null) this.request.body(new StringRequestContent(GSON.toJson(body)));
        return this;
    }

    public RequestClient addHeaders(WeakHashMap<String, String> map) {
        if (this.request != null) {
            this.request.headers((x) -> map.forEach(x::put));
        }
        return this;
    }

    public RequestClient setHeaders(Pairs[] pairs) {
        if (this.request != null & pairs != null) {
            this.request.headers((x) -> Arrays.stream(pairs).forEach((p) ->
                    x.put(p.getKey(), p.getValue())));

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
            file = FileHandler.searchFile(pathToAttachment.replaceAll("[*/]", "")).join();
        } else file = new File(pathToAttachment);
        try {
            if (file.getName().endsWith(".sk")) file = changeExtension(file, ".vb");
        } catch (IOException exception) {
            if (PROJECT_DEBUG) requestLog(exception.getMessage());
        }
        if (file.exists()) attachments.add(file);
        return Optional.of(this);
    }

    @SuppressWarnings("UnusedReturnValue")
    public RequestClient setAttachments(LinkedList<Attachment> attachments) {
        if (attachments == null) return this;
        for (var att : attachments) {
            if (att.extension().endsWith(".sk")) {
                try {
                    att.regenerate(changeExtension(att.file(), ".vb"));
                } catch (Exception exception) {
                    error(exception);
                }
            }
            if (att.file().exists()) this.attachments.add(att.file());
        }
        return this;
    }

    public RequestClient postAttachments(JsonElement body) {
        this.postAttachments(GSON.toJson(body));
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Optional<RequestClient> postAttachments(String body) {
        try (var mpr = new MultiPartRequestContent()) {
            attachments.forEach(attachment -> {
                try {
                    mpr.addPart(new MultiPart.PathPart(attachment.getName(), attachment.getName(), HttpFields.EMPTY, attachment.toPath()));
                } catch (Exception e) {
                    if (PROJECT_DEBUG) error(e);
                }
            });
            mpr.addPart(new MultiPart.ContentSourcePart("payload_json", null, HttpFields.EMPTY, new StringRequestContent(body, StandardCharsets.UTF_8)));
            this.request.body(mpr);
        } catch (Exception ex) {
            if (PROJECT_DEBUG) requestLog(ex.getMessage());
        }
        return Optional.of(this);
    }

    private void done() {
        if (this.isDone()) {
            try {
                this.client.stop();
            } catch (Exception e) {
                if (PROJECT_DEBUG) error(e);
            }
        }
    }

    @Override
    public void close() {
        this.done();
    }
}
