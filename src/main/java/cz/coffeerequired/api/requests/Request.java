package cz.coffeerequired.api.requests;

import ch.njol.skript.util.Timespan;
import com.google.gson.JsonElement;
import cz.coffeerequired.skript.http.bukkit.HttpReceivedResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;

@Data
@Setter
@Getter
@SuppressWarnings("all")
public class Request {
    private final String uri;
    private final RequestMethod method;
    private JsonElement content;
    private Pairs[] header;
    private LinkedList<Attachment> attachments = new LinkedList<>();
    private RequestStatus status = RequestStatus.UNKNOWN;
    private HashMap<String, String> queryParams = new HashMap<>();
    private Response response = Response.empty();
    private HttpReceivedResponse event = null;
    private Timespan timeout;
    private boolean followRedirects;
    private String httpVersion;

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

    public void setTimeout(Timespan time) {
        this.timeout = time;
    }

    public Request setResponse(Response response) {
        this.response = response;
        this.event.setResponse(response);
        return this;
    }

    public Request setContent(JsonElement content) {
        this.content = content;
        return this;
    }


    public Request setHeader(Pairs[] header) {
        this.header = header;
        return this;
    }

    public void addQueryParam(HashMap<String, String> queryParams) {
        this.queryParams.putAll(queryParams);
    }

    public void setEvent(HttpReceivedResponse event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "prepared request for uri: " + uri + ", method: " + method + " and status: " + status;
    }
}

