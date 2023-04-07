package cz.coffee.core.requests;

import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

/**
* This file is part of CoffeeThing.
* <p>
* Skript is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* <p>
* Skript is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* <p>
* You should have received a copy of the GNU General Public License
* along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
* <p>
* Copyright coffeeRequired nd contributors
* <p>
* Created: neděle (02.04.2023)
*/



public class HttpHandler {
    private String _method;
    private Timer _timer;
    private java.net.http.HttpClient _client;
    private HttpRequest.Builder _requestBuilder;
    private JsonObject _content = new JsonObject();
    private final WeakHashMap<String, String> _headers = new WeakHashMap<>();
    private HandlerBody response_;
    protected static int lastHttpResponseCode;

    public HttpHandler(String url, String method) {
        _method = method;
        _timer = null;
        _client = java.net.http.HttpClient.newHttpClient();
        _requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));
    }
    public boolean isSucccessfull() {
       return lastHttpResponseCode == 200;
    }

    public HttpHandler(String url, String method, Timer timer) {
        this(url, method);
        _timer = timer;
    }

    public HttpHandler addHeader(String key, String value) {
        _headers.put(key, value);
        return this;
    }

    public HttpHandler addBodyContent(String key, Object value) {
        _content.add(key, new Gson().toJsonTree(value, value.getClass()));;
        return this;
    }

    public static int getLastHttpResponseCode() {
        return lastHttpResponseCode;
    }

    public final HttpHandler addBodyContent(Content... contents) {
        Arrays.stream(contents).forEach(c -> {
            _content.add(c.c1, c.c2);
        });
        return this;
    }

    private void jsonEncoded() {
        _requestBuilder.header("Content-type", "application/json");
    }

    public HttpHandler setBodyContent(JsonElement json) {
        if (json instanceof JsonObject object) {
            _content = object;
        }
        return this;
    }

    public HandlerBody getBody() {
        return response_;
    }

    public Timer getTimer() {
        return _timer;
    }

    private void setHeaders() {
        if (!_headers.isEmpty() && _requestBuilder != null) {
            _headers.forEach(_requestBuilder::header);
        }
    }

    private HttpRequest makeRequest() {
        if (_requestBuilder != null) {
            return switch (_method.toUpperCase()) {
                case "GET" -> _requestBuilder.GET().build();
                case "POST" ->  {
                    jsonEncoded();
                    yield _requestBuilder.POST(HttpRequest.BodyPublishers.ofString(_content.toString())).build();
                }
                default -> _requestBuilder.build();
            };
        }
        return null;
    }


    public void asyncSend() throws Exception {
        setHeaders();
        HttpRequest request = makeRequest();
        long startTime = System.nanoTime();
        CompletableFuture<HttpResponse<String>> future = _client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        long endTime = System.nanoTime();
        if (_timer != null) _timer.addTime(endTime - startTime);
        HttpResponse<String> result = future.get();
        lastHttpResponseCode = result.statusCode();
        response_ = HandlerBody.of(result.body());
    }

    public void send() throws Exception {
        setHeaders();
        HttpRequest request = makeRequest();
        long startTime = System.nanoTime();
        HttpResponse<String> response = _client.send(request, HttpResponse.BodyHandlers.ofString());
        long endTime = System.nanoTime();
        if (_timer != null) {
            _timer.addTime(endTime - startTime);
        }
        response_ = HandlerBody.of(response.body());
        System.out.println(_timer.getTime());
    }

    public void disconnect() {
        if (_client == null && _requestBuilder == null) {
            return;
        }
        _client = null;
        _requestBuilder = null;
        _method = null;
        response_ = null;
    }

    public static class Content {

        final String c1;
        final JsonElement c2;

        public static Content of(String c1, String c2) {
            return new Content(c1, c2);
        }

        public Content(String c1, String c2) {
            this.c1 = c1;
            this.c2 = new Gson().toJsonTree(c2, c2.getClass());
        }
    }

    public interface HandlerBody {
        static HandlerBody of(Object obj) {
            return new HandlerBody() {
                final Gson gson = new GsonBuilder()
                        .disableHtmlEscaping()
                        .serializeNulls()
                        .setPrettyPrinting()
                        .create();

                @Override
                public JsonElement toJson() {
                    JsonElement parsed;
                    try {
                        parsed = JsonParser.parseString(obj.toString());
                    } catch (Exception e) {
                        if (getLastHttpResponseCode() == 200) {
                            return null;
                        } else {
                            JsonElement main = JsonParser.parseString("{'message': 'connection refused'}");
                            main.getAsJsonObject().add("result", HtmlToJson.of(obj.toString()));
                            return main;
                        }
                    }
                    return parsed;
                }

                @Override
                public String prettyPrint() {
                    JsonElement parsed;
                    try {
                        parsed = JsonParser.parseString(obj.toString());
                    } catch (Exception e) {
                        if (getLastHttpResponseCode() == 200) {
                            return obj.toString();
                        } else {
                            JsonElement main = JsonParser.parseString("{'message': 'connection refused'}");
                            main.getAsJsonObject().add("result", HtmlToJson.of(obj.toString()));
                            return gson.toJson(main);
                        }
                    }
                    return gson.toJson(parsed);
                }

                @Override
                public String toString() {
                    return obj.toString();
                }
            };
        }

        public JsonElement toJson();

        public String toString();

        public String prettyPrint();
    }

}
class HtmlToJson {
    public static JsonObject of(String html) {
        Document doc = Jsoup.parse(html);

        JsonObject json = new JsonObject();
        Element root = doc.child(0);
        json.add(root.tagName(), elementToJson(root));

        return json;
    }

    private static JsonObject elementToJson(Element element) {
        JsonObject json = new JsonObject();

        // Add element attributes to JSON object
        for (org.jsoup.nodes.Attribute attr : element.attributes()) {
            json.addProperty(attr.getKey(), attr.getValue());
        }

        // Add element content to JSON object
        String content = element.ownText().trim();
        if (!content.isEmpty()) {
            json.addProperty("content", content);
        }
        Elements children = element.children();
        if (!children.isEmpty()) {
            JsonObject childJson = new JsonObject();
            for (Element child : children) {
                childJson.add(child.tagName(), elementToJson(child));
            }
            json.add("children", childJson);
        }

        return json;
    }
}


