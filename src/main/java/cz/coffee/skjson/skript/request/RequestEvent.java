package cz.coffee.skjson.skript.request;

import ch.njol.skript.lang.Variable;
import cz.coffee.skjson.api.http.RequestResponse;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;


@SuppressWarnings("all")
public class RequestEvent extends Event {
    private final Request request;
    private RequestResponse response;
    private boolean saveIncorrect;

    private WeakHashMap<String, Variable<?>> varMap;

    public void setResponse(RequestResponse response) {
        this.response = response;
    }

    public void setSaveIncorrect(boolean saveIncorrect) {
        this.saveIncorrect = saveIncorrect;
    }

    public void setVarMap(WeakHashMap<String, Variable<?>> varMap) {
        this.varMap = varMap;
    }

    public RequestEvent(Request req) {
        if (req == null) throw RequestException.requestNull();
        this.request = req;
    }

    public Request getRequest() {
        return request;
    }

    public RequestResponse getResponse() {
        return response;
    }

    public boolean isSaveIncorrect() {
        return saveIncorrect;
    }

    public WeakHashMap<String, Variable<?>> getVarMap() {
        return varMap;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        throw RequestException.handlersCalled();
    }
}
