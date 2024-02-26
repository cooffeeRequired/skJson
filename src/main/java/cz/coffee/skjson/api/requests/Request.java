package cz.coffee.skjson.api.requests;

import com.google.gson.JsonElement;

import java.util.*;

import static cz.coffee.skjson.utils.Util.fstring;

public class Request {
    private final String uri;
    private final RequestMethod method;
    private JsonElement content;
    private Pairs[] header;
    private LinkedList<Attachment> attachments = new LinkedList<>();
    private RequestStatus status = RequestStatus.UNKNOWN;
    private HashMap<String, String[]> queryParams = new HashMap<>();
    private Response response = Response.empty();

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

    public LinkedList<Attachment> attachments() {
        return attachments;
    }

    public  HashMap<String, String[]> getQueryParams() {
        return this.queryParams;
    }

    public void addQueryParam(HashMap<String, String[]> queryParams) {
        this.queryParams.putAll(queryParams);
    }

    public void setQueryParam(HashMap<String, String[]> queryParams) {
        this.queryParams = queryParams;
    }

    public void setAttachments(LinkedList<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return fstring("Request{ uri: %s, method: %s, content: %s, header: %s, status: %s }", this.uri, this.method, this.content, this.header, this.status);
    }
}

