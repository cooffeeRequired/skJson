package cz.coffeerequired.api.nbts;

import cz.coffeerequired.SkJson;

import java.util.logging.Logger;

public class SkJsonLoggerNBT extends Logger {
    public SkJsonLoggerNBT(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    /**
     * Get an instance of SkJsonLoggerNBT
     *
     * @return new instance of SkJsonLoggerNBT
     */
    public static SkJsonLoggerNBT getLogger() {
        return new SkJsonLoggerNBT("", null);
    }

    @Override
    public void info(String msg) {
        String prefix = msg.replace("[NBTAPI]", "&7[&bNBT&3API&7]");
        if (msg.contains("google.gson") || msg.contains("bStats")) {
            return;
        }
        SkJson.info(prefix);
    }
}
