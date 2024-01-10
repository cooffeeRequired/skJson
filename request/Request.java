package cz.coffee.skjson.skript.request;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import com.google.gson.JsonElement;
import cz.coffee.skjson.api.SkriptReflection;
import cz.coffee.skjson.api.http.RequestClient;
import cz.coffee.skjson.api.http.RequestResponse;
import org.bukkit.event.Event;

import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

public class Request {
    private final SectionNode node;
    private final String url;
    private final Object variablesMap;


    private String method;
    private RequestUtil.Pairs[] headers;
    private JsonElement body;
    private boolean lenient;
    private boolean saveIncorrect;

    public Request(SectionNode node, String url, Event event) {
        this(node, url, SkriptReflection.copyLocals(SkriptReflection.getLocals(event)));
    }

    public Request(SectionNode node, String url, Object variablesMap) {
        this.node = node;
        this.url = url;
        this.variablesMap = variablesMap;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public Request setHeaders(RequestUtil.Pairs[] headers) {
        this.headers = headers;
        return this;
    }

    public Request setBody(JsonElement json) {
        this.body = json;
        return this;
    }

    public Request lenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    public void saveIncorrect(boolean svs) {
        this.saveIncorrect = svs;
    }

    public RequestEvent execute() {
        RequestEvent event = new RequestEvent(this);
        SkriptReflection.putLocals(SkriptReflection.copyLocals(variablesMap), event);
        var entry = RequestUtil.SAVE_VALIDATOR.validate(this.node);
        if (entry == null) return event;
        Variable<?> reqBody = (Variable<?>) entry.getOptional("content", false);
        Variable<?> reqHeader = (Variable<?>) entry.getOptional("headers", false);
        Variable<?> reqStatus = (Variable<?>) entry.getOptional("status code", false);
        Variable<?> reqURL = (Variable<?>) entry.getOptional("url", false);

        var variableMap = new WeakHashMap<String, Variable<?>>();
        if (reqBody != null) variableMap.put("content", reqBody);
        if (reqHeader != null) variableMap.put("header", reqHeader);
        if (reqStatus != null) variableMap.put("status", reqStatus);
        if (reqURL != null) variableMap.put("url", reqURL);

        RequestResponse response = null;
        try (var client = new RequestClient(this.url)) {
            response = client
                    .method(method)
                    .setHeaders(headers)
                    .setContent(body)
                    .request(lenient)
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                event.setResponse(response);
                event.setVarMap(variableMap);
                event.setSaveIncorrect(saveIncorrect);
            }
        }
        SkriptReflection.removeLocals(event);
        return event;
    }
}
