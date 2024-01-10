package cz.coffee.skjson.skript.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

abstract public class RequestUtil {
    public static class JsonOrString {
        private final Object input;
        public JsonOrString(final Object object) {
            this.input = object;
        }

        public boolean isJson() {
            return this.input instanceof JsonElement;
        }

        public JsonElement json() {
            if (this.isJson()) {
                return ((JsonElement) this.input);
            }
            return JsonNull.INSTANCE;
        }

        public String string() {
            if (!this.isJson() && this.input instanceof String string) {
                return string;
            }
            return "";
        }

    }

    public static final class Pairs {
        private String LEFT;
        private static final String DELIMITER = ":";
        private String RIGHT;

        Pairs(Object input) {
            if (input instanceof String str) {
                var sp = str.split(DELIMITER);
                if (sp.length == 2 && !sp[0].isEmpty() && !sp[1].isEmpty()) {
                    this.LEFT = sp[0];
                    this.RIGHT = sp[1];
                }
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
}
