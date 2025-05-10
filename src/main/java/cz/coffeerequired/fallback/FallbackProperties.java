package cz.coffeerequired.fallback;

import cz.coffeerequired.SkJson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FallbackProperties {
    private static FallbackProperties instance;
    private final Properties fallback;

    private FallbackProperties() {
        this.fallback = new Properties();
        this.loadFallback(new File(SkJson.getInstance().getDataFolder(), "libraries/configuration.properties"));
    }

    public static FallbackProperties getInstance() {
        if (instance == null) {
            instance = new FallbackProperties();
        }
        return instance;
    }

    private void loadFallback(File file) {
        try {
            SkJson.debug("Loading fallback properties from %s", file);
            SkJson.debug("Fallback properties exists: %s", file.exists());
            this.fallback.load(new FileInputStream(file));
        } catch (IOException e) {
            SkJson.severe("Failed to load fallback properties: %s", e.getMessage());
        }
    }

    public String getProperty(String key) {
        return this.fallback.getProperty(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        try {
            return (T) this.fallback.getProperty(key);
        } catch (ClassCastException e) {
            SkJson.severe("Failed to cast fallback property: %s", e.getMessage());
            return null;
        }
    }
}
