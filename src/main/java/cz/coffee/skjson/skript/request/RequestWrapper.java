package cz.coffee.skjson.skript.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import cz.coffee.skjson.skript.request.RequestUtil.*;

public class RequestWrapper {
    private JsonOrString inputContent = new JsonOrString("");
    private Pairs[] inputHeaders;
    private boolean lenient;
    private boolean saveIncorrect;
    private final String method;
    private final String uri;
    public RequestWrapper(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public Pairs[] getInputHeaders() {
        return inputHeaders;
    }

    public void setInputHeaders(Pairs[] inputHeaders) {
        this.inputHeaders = inputHeaders;
    }

    public JsonElement getInputContent() {
        if (this.inputContent.isJson()) {
            return this.inputContent.json();
        } else {
            return JsonParser.parseString(inputContent.string());
        }
    }

    public void setInputContent(JsonOrString inputContent) {
        this.inputContent = inputContent;
    }

    public boolean isLenient() {
        return lenient;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    public boolean isSaveIncorrect() {
        return saveIncorrect;
    }

    public void setSaveIncorrect(boolean saveIncorrect) {
        this.saveIncorrect = saveIncorrect;
    }
}
