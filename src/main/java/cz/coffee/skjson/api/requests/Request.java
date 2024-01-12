package cz.coffee.skjson.api.requests;

import com.google.gson.JsonElement;

import static cz.coffee.skjson.utils.Util.fstring;

public class Request {
    final String uri;
    final RequestMethod method;
    RequestStatus status = RequestStatus.UNKNOWN;
    Response response = Response.empty();
    JsonElement content;
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

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public RequestStatus status() {
        return status;
    }

    public Request setResponse(Response response) {
        this.response = response;
        return this;
    }

    public Response response() {
        return response;
    }

    public String uri() {
        return uri;
    }

    public RequestMethod method() {
        return method;
    }

    public JsonElement content() {
        return content;
    }

    public Request setContent(JsonElement content) {
        this.content = content;
        return this;
    }

    public Pairs[] header() {
        return header;
    }

    public Request setHeader(Pairs[] header) {
        this.header = header;
        return this;
    }

    @Override
    public String toString() {
        return fstring("Request{ uri: %s, method: %s, content: %s, header: %s, status: %s }", this.uri, this.method, this.content, this.header, this.status);
    }
}

