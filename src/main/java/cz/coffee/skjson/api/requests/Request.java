package cz.coffee.skjson.api.requests;

import ch.njol.util.Pair;
import com.google.gson.JsonElement;

import static cz.coffee.skjson.utils.Util.fstring;

public class Request {
    public Request setStatus(RequestStatus status) {
        this.status = status;
        return this;
    }

    public RequestStatus status() {
        return status;
    }

    RequestStatus status = RequestStatus.UNKNOWN;

    public Request setResponse(Response response) {
        this.response = response;
        return this;
    }

    public Response response() {
        return response;
    }

    Response response = Response.empty();

    final String uri;

    public String uri() {
        return uri;
    }

    public RequestMethod method() {
        return method;
    }

    final RequestMethod method;

    public JsonElement content() {
        return content;
    }

    public Request setContent(JsonElement content) {
        this.content = content;
        return this;
    }

    JsonElement content;

    public Pairs[] header() {
        return header;
    }

    public Request setHeader(Pairs[] header) {
        this.header = header;
        return this;
    }

    Pairs[] header;
    public Request(String uri, RequestMethod method, JsonElement content, Pairs[] headers) {
        this.uri = uri;
        this.method = method;
        this.content = content;
        this.header = headers;
    }

    public Request(String uri, RequestMethod method) {
        this.uri = uri;
        this.method = method;
    }

    @Override
    public String toString() {
        return fstring("Request{ uri: %s, method: %s, content: %s, header: %s, status: %s }", this.uri, this.method, this.content, this.header, this.status);
    }
}

