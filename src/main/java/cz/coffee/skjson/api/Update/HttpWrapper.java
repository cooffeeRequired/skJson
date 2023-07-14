package cz.coffee.skjson.api.Update;

import ch.njol.skript.log.ErrorQuality;
import com.google.gson.*;
import cz.coffee.skjson.api.FileWrapper;
import cz.coffee.skjson.skript.requests.Requests;
import cz.coffee.skjson.utils.TimerWrapper;
import cz.coffee.skjson.utils.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cz.coffee.skjson.api.Config.*;

/**
 * The type Http wrapper.
 */
public class HttpWrapper implements AutoCloseable {

    /**
     * The constant GSON.
     */
    final static Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().setLenient().create();

    @Override
    public void close() {
        if (client == null && builder == null) return;
        client = null;
        builder = null;
        method = null;
    }
    /**
     * The type Header.
     */
    public static class Header {
        private final HttpHeaders headers;

        /**
         * Instantiates a new Header.
         *
         * @param headers the headers
         */
        public Header(HttpHeaders headers) {
            this.headers = headers;
        }

        /**
         * Json json element.
         *
         * @return the json element
         */
        public JsonElement json() {
            return GSON.toJsonTree(headers.map());
        }

        /**
         * Text string.
         *
         * @return the string
         */
        public String text() {
            return GSON.toJson(headers.map());
        }

        /**
         * Raw http headers.
         *
         * @return the http headers
         */
        public HttpHeaders raw() {
            return headers;
        }

    }


    /**
     * The interface Response.
     */
    public interface Response {

        /**
         * Of response.
         *
         * @param requestHeaders  the request headers
         * @param responseHeaders the response headers
         * @param uri             the uri
         * @param body            the body
         * @param statusCode      the status code
         * @return the response
         */
        static Response of(HttpHeaders requestHeaders, HttpHeaders responseHeaders, URI uri, String body, int statusCode) {
            return new Response() {
                @Override
                public Header getRequestHeaders() {
                    return new Header(requestHeaders);
                }
                @Override
                public Header getResponseHeader() {
                    return new Header(responseHeaders);
                }
                @Override
                public int getStatusCode() {
                    return statusCode;
                }
                @Override
                public JsonElement getBodyContent() {
                    try {
                        return JsonParser.parseString(body);
                    } catch (Exception e) {
                        if (PROJECT_DEBUG) Util.error(e.getMessage(), ErrorQuality.NONE);
                    }
                    return JsonNull.INSTANCE;
                }
                @Override
                public URL getRequestURL() {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException exception) {
                        if (PROJECT_DEBUG) Util.error(exception.getMessage(), ErrorQuality.SEMANTIC_ERROR);
                        return null;
                    }
                }
                @Override
                public boolean isSuccessfully(){
                    return statusCode >= 200 && statusCode < 230;
                }
            };
        }

        /**
         * Gets request headers.
         *
         * @return the request headers
         */
        Header getRequestHeaders();

        /**
         * Gets response header.
         *
         * @return the response header
         */
        Header getResponseHeader();

        /**
         * Gets status code.
         *
         * @return the status code
         */
        int getStatusCode();

        /**
         * Gets body content.
         *
         * @return the body content
         */
        JsonElement getBodyContent();

        /**
         * Gets request url.
         *
         * @return the request url
         */
        URL getRequestURL();

        /**
         * Is successfully boolean.
         *
         * @return the boolean
         */
        boolean isSuccessfully();
    }

    private final ConcurrentHashMap<String, String> _headers = new ConcurrentHashMap<>();
    private Requests.RequestMethods method;
    private HttpClient client;
    private HttpRequest.Builder builder;
    private JsonObject content = new JsonObject();
    private TimerWrapper timer;
    private HttpRequest request;
    private final ArrayList<File> attachments = new ArrayList<>();

    /**
     * Instantiates a new Http wrapper.
     *
     * @param URL    the url
     * @param method the method
     */
    public HttpWrapper(String URL, String method) {
        this(URL, Requests.RequestMethods.valueOf(method.toUpperCase()));
    }

    /**
     * Instantiates a new Http wrapper.
     *
     * @param URL    the url
     * @param method the method
     */
    public HttpWrapper(String URL, Requests.RequestMethods method) {
        if (method == null) {
            if (PROJECT_DEBUG) Util.error("HttpWrapper: The method cannot be null", ErrorQuality.NONE);
            return;
        }
        this.method = method;


        // Initialized http client
        client = HttpClient.newHttpClient();
        timer = new TimerWrapper(0);

        // create URL from string
        URI requestLink;
        try {
            requestLink = URI.create(sanitizeLink(URL));
        } catch (Exception e) {
            if (PROJECT_DEBUG) Util.error(e.getMessage(), ErrorQuality.SEMANTIC_ERROR);
            return;
        }
        builder = HttpRequest.newBuilder().uri(requestLink);
    }

    public void postAttachments(String body) {
        AtomicInteger i = new AtomicInteger(0);
        MimeMultipartData data = null;
        var mmd = MimeMultipartData.newBuilder().withCharset(StandardCharsets.UTF_8);
        attachments.forEach(attachment -> {
            try {mmd.addFile(String.valueOf(i.incrementAndGet()),attachment.toPath(), Files.probeContentType(attachment.toPath()));
            } catch (Exception e) {if (PROJECT_DEBUG) Util.error(e.getMessage());}
        });
        mmd.addText("payload_json", body);
        try {
            data = mmd.build();
            builder.header("Content-Type", data.getContentType());
            request = builder.POST(data.getBodyPublisher()).build();
        } catch (Exception ex) {
            if (PROJECT_DEBUG) Util.log(ex);
        }
    }

    /**
     * Request http wrapper.
     *
     * @return the http wrapper
     */
    public HttpWrapper request() {
        if (builder != null) {
            switch (method) {
                case GET -> request = builder.GET().build();
                case POST -> {
                    String convertedBody = GSON.toJson(content);
                    if (!attachments.isEmpty()) {
                        postAttachments(convertedBody);
                    } else {
                        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(convertedBody);
                        request = builder.POST(body).build();
                    }
                }
                case PUT -> {
                    String convertedBody = GSON.toJson(content);
                    if (!attachments.isEmpty()) {
                        postAttachments(convertedBody);
                    } else {
                        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(convertedBody);
                        request = builder.PUT(body).build();
                    }
                }
                case DELETE -> request = builder.DELETE().build();
                case HEAD -> {
                    HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.noBody();
                    request = builder.method("HEAD", body).build();
                }
                case PATCH -> {
                    // need to be JsonEncoded
                    String convertedBody = GSON.toJson(content);
                    HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(convertedBody);
                    request = builder.method("PATCH", body).build();
                }
                default -> request = builder.build();
            }
        }
        return this;
    }

    /**
     * Colorized method string.
     *
     * @param method the method
     * @return the string
     */
    public String colorizedMethod(Requests.RequestMethods method) {
        return "&l" + switch (method) {
            case GET -> "&aGET";
            case POST -> "&bPOST";
            case PUT -> "&7PUT";
            case DELETE -> "&cDELETE";
            case HEAD -> "&3HEAD";
            case PATCH -> "&ePATCH";
            case MOCK -> "&5MOCK";
        } + "&r";
    }

    public static File changeExtension(File f, String newExtension) throws IOException {
        int i = f.getName().lastIndexOf('.');
        String name = f.getName().substring(0, i);
        File tempFile = File.createTempFile(name + ".sk -- ", newExtension);

        try (FileInputStream fis = new FileInputStream(f); FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) fos.write(buffer, 0, bytesRead);
        }

        return tempFile;
    }
    public HttpWrapper addAttachment(String pathToAttachment) {
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

    private String requestUrl;

    /**
     * Process response.
     *
     * @return the response
     */
    public Response process() {
        try (var timer = new TimerWrapper(0)) {
            this.timer = timer;
            HttpResponse.BodyHandler<String> body = HttpResponse.BodyHandlers.ofString();
            return client.sendAsync(request, body).thenApply(future -> {
                requestUrl = future.uri().toString();
                if (LOGGING_LEVEL > 1)
                    Util.log(String.format(
                            REQUESTS_PREFIX + ": "+ colorizedMethod(method) +" request was send to &b'%s'&r and takes %s", requestUrl, timer.toHumanTime()));
                return Response.of(request.headers(), future.headers(), future.uri(), future.body(), future.statusCode());
            }).get();
        } catch (Exception e) {
            if (PROJECT_DEBUG) Util.error(e.getMessage(), ErrorQuality.NONE);
            return null;
        }
    }

    /**
     * Gets time.
     *
     * @return the time
     */
    public String getTime() {
        return timer.toHumanTime();
    }

    /**
     * Sets content.
     *
     * @param body the body
     * @return the content
     */
    public HttpWrapper setContent(final JsonElement body) {
        if (body.isJsonObject()) {
            content = body.getAsJsonObject();
        }
        return this;
    }

    /**
     * Sets headers.
     *
     * @param headers the headers
     * @return the headers
     */
    public HttpWrapper setHeaders(final JsonElement headers) {
        if (!headers.isJsonNull()) {
            if (headers.isJsonObject()) {
                JsonObject object = headers.getAsJsonObject();
                object.entrySet().forEach(entry -> {
                    String value = null;
                    if (entry.getValue() instanceof JsonPrimitive primitive) {
                        if (primitive.isString()) {
                            value = primitive.getAsString();
                        } else {
                            value = primitive.toString();
                        }
                    }
                    if (value != null) builder.setHeader(entry.getKey(), value);
                });
            }
        }
        return this;
    }

    /**
     * Add header http wrapper.
     *
     * @param name  the name
     * @param value the value
     * @return the http wrapper
     */
    public HttpWrapper addHeader(final String name, final String value) {
        if (name != null && value != null) {
            builder.header(name, value);
        }
        return this;
    }

    private String sanitizeLink(String url) {
        return url.replace(" ", "%20");
    }
}
