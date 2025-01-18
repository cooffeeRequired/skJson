package cz.coffeerequired.api.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Pairs {
    private static final String DELIMITER = ":";
    private String LEFT;
    private String RIGHT;

    public Pairs(Object input) {
        if (input instanceof String str) {
            var sp = str.split(DELIMITER);
            if (isPair(input)) {
                this.LEFT = sp[0].replaceAll("[{\\[\\]}]", "");
                this.RIGHT = sp[1].replaceAll("[{\\[\\]}]", "");
            }
        } else if (input instanceof JsonElement json && json instanceof JsonObject o) {
            o.entrySet().forEach(entry -> {
                this.LEFT = entry.getKey();
                this.RIGHT = entry.getValue().getAsString();
            });
        }
    }

    private static boolean isPair(Object input) {
        if (input instanceof String str) {
            var sp = str.split(DELIMITER);
            return sp.length == 2 && !sp[0].isEmpty() && !sp[1].isEmpty();
        }
        return false;
    }

    public String getKey() {
        return this.LEFT.trim();
    }

    public String getValue() {
        return this.RIGHT.trim();
    }

    @Override
    public String toString() {
        return this.LEFT + DELIMITER + this.RIGHT;
    }
}
