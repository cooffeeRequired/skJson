package cz.coffee.skjson.api.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import cz.coffee.skjson.api.FileWrapper;
import cz.coffee.skjson.utils.TimerWrapper;
import cz.coffee.skjson.utils.Util;
import org.eclipse.jetty.client.*;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MultiPart;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;
import static cz.coffee.skjson.api.http.RequestClientUtils.changeExtension;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: sobota (30.09.2023)
 */
public class RequestClient implements AutoCloseable{
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isOk(int statusCode) {
        System.out.println(statusCode);
        return statusCode >= 200 && statusCode < 300;
    }

    public RequestClient get() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("GET");
        return this;
    }

    public RequestClient method(String method) {
        switch (method) {
            case "GET", "MOCK" -> {
                this.request = this.client.newRequest(this.uri);
                this.request.method("GET");
            }
            case "POST" -> {
                this.request = this.client.newRequest(this.uri);
                this.request.method("POST");
            }
            case "PUT" -> {
                this.request = this.client.newRequest(this.uri);
                this.request.method("PUT");
            }
            case "DELETE" -> {
                this.request = this.client.newRequest(this.uri);
                this.request.method("DELETE");
            }
            case "PATCH" -> {
                this.request = this.client.newRequest(this.uri);
                this.request.method("PATCH");
            }
            case "HEAD" -> {
                this.request = this.client.newRequest(this.uri);
                this.request.method("HEAD");
            }
        }
        return this;
    }

    public RequestClient post() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("POST");
        return this;
    }

    public RequestClient put() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("PUT");
        return this;
    }

    public RequestClient delete() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("DELETE");
        return this;
    }

    public RequestClient head() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("HEAD");
        return this;
    }

    public RequestClient patch() {
        this.request = this.client.newRequest(this.uri);
        this.request.method("PATCH");
        return this;
    }
    public RequestClient test() {
        try {
            this.request.timeout(15L, TimeUnit.SECONDS);
            this.request.send(response -> {
                System.out.println(response);
            });

            ContentResponse rsp = this.request.send();
            if (isOk(rsp.getStatus())) {
                return this;
            } else {
                return null;
            }
        } catch (Exception ex) {
            if (PROJECT_DEBUG) Util.enchantedError(ex, ex.getStackTrace(), "RequestClient - test()");
            return null;
        }
    }

    public RequestResponse request() {
        try (var timer = new TimerWrapper(0)) {
            if (this.request == null && this.client == null) return null;
            if (this.request != null) {
                var response = this.request.send();
                return RequestResponse.of(null, null, null, null, 0, false);
            }
            this.timer = timer;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        this.client.stop();
        this.request = null;
        this.client = null;
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

    @SuppressWarnings("all")
    public RequestClient addAttachment(String pathToAttachment) {
        File file;
        if (pathToAttachment.startsWith("*")) {
            file = FileWrapper.serchFile(pathToAttachment.replaceAll("[*/]", ""));
        } else file = new File(pathToAttachment);
        try {
            if (file.getName().endsWith(".sk")) file = changeExtension(file, ".vb");
        } catch (IOException exception) {
            if (PROJECT_DEBUG) Util.requestLog(exception.getMessage());
        }
        if (file.exists()) attachments.add(file);
        return this;
    }

    public RequestClient postAttachments(String body) {
        AtomicInteger i = new AtomicInteger(0);
        try (var mpr = new MultiPartRequestContent()) {
            attachments.forEach(attachment -> {
                try {
                    String contentType = Files.probeContentType(attachment.toPath());
                    mpr.addPart(new MultiPart.PathPart("file" + i.incrementAndGet(), attachment.getName(), HttpFields.EMPTY, attachment.toPath()));
                } catch (Exception e) {
                    if (PROJECT_DEBUG) Util.error(e.getMessage());
                }
            });
            mpr.addPart(new MultiPart.ContentSourcePart("payload_json", null, HttpFields.EMPTY, new StringRequestContent(body, StandardCharsets.UTF_8)));
            this.request.body(mpr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return this;
    }
}
