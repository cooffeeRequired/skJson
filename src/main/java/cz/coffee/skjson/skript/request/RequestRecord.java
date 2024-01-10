package cz.coffee.skjson.skript.request;

import cz.coffee.skjson.api.http.RequestHeaders;

public record RequestRecord(int code, RequestHeaders headers, RequestUtil.JsonOrString body) {
}
