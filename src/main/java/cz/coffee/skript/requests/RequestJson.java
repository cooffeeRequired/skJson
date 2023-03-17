package cz.coffee.skript.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import cz.coffee.SkJson;
import okhttp3.*;
import org.bukkit.event.Event;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This file is part of skJson.
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
 * Created: pátek (17.03.2023)
 */

public class RequestJson {

    private static Event event;
    private static Request request;

    private static OkHttpClient client;
    private static JsonElement response;
    private static volatile boolean status;

    public static void init(Event event) {
        if (RequestJson.event == null || !RequestJson.event.equals(event)) {
            setEvent(event);
            client =  new OkHttpClient();
            status = false;
        }
    }


    public static void getPrepare(String urlInput, JsonObject headers) throws MalformedURLException {
        final URL url = new URL(urlInput);
        final Request.Builder builder = new Request.Builder().url(url);
        if (headers != null) {
            headers.entrySet().forEach(jsonHeader -> {
                if (jsonHeader.getValue().isJsonPrimitive()) {
                    JsonPrimitive primitive = jsonHeader.getValue().getAsJsonPrimitive();
                    if (primitive.isString()) {
                        builder.addHeader(jsonHeader.getKey(), primitive.getAsString());
                    }
                }
            });
        }
        setRequest(builder.get().build());
    }

    public static void postPrepare(String urlInput, JsonObject headers, JsonObject bodyInput) throws MalformedURLException {
        final URL url = new URL(urlInput);
        final Request.Builder builder = new Request.Builder().url(url);
        if (headers != null) {
            headers.entrySet().forEach(jsonHeader -> {
                if (jsonHeader.getValue().isJsonPrimitive()) {
                    JsonPrimitive primitive = jsonHeader.getValue().getAsJsonPrimitive();
                    if (primitive.isString()) {
                        builder.addHeader(jsonHeader.getKey(), primitive.getAsString());
                    }
                }
            });
        }
        System.out.println(bodyInput);
        if (bodyInput != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyInput.toString());
            System.out.println(body);
            builder.post(body);
        }
        setRequest(builder.build());
    }

    public static JsonElement getResponse() {
        return response;
    }

    public static void execute() throws IOException {
        if (request == null) {
            SkJson.error("Request is null.");
            return;
        }
        setResponse(JsonParser.parseString(client.newCall(request).execute().body().string()));
        request = null;
    }


    static void setEvent(Event event) {
        RequestJson.event = event;
    }
    static void setRequest(Request request) {
        RequestJson.request = request;
    }
    static void setResponse(JsonElement rsp) {
        RequestJson.response = rsp;
    }



}
