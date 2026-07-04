package cz.coffeerequired.support;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.cache.JsonWatchType;
import cz.coffeerequired.api.json.PathParser;
import cz.coffeerequired.api.nbts.NBTJsonCache;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static cz.coffeerequired.api.Api.Records.*;

@SuppressWarnings("all")
public class PluginConfigHandler {
    private static final String FORBIDDEN_DELIMITER_CHARS = "$#^[]{}_-";
    private final File configFile;
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public PluginConfigHandler(JavaPlugin plugin) {
        this(plugin, "config.yml");
        try {
            loadRecords();
        } catch (Exception e) {
            SkJson.severe("Config file is invalid. Regenerating config file!");
            regenerateConfig();
            try {
                loadRecords();
            } catch (Exception reloadError) {
                SkJson.exception(reloadError, "Failed to load regenerated config");
            }
        }
    }

    public PluginConfigHandler(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        loadOrRegenerateConfig();
    }

    private void loadOrRegenerateConfig() {
        try {
            if (!configFile.exists()) {
                if (plugin.getResource(configFile.getName()) != null) {
                    plugin.saveResource(configFile.getName(), false);
                    SkJson.info("Config file created at: " + configFile.getName());
                } else {
                    configFile.createNewFile();
                    SkJson.info("Empty config file created at: " + configFile.getName());
                }
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
            SkJson.info("Loaded config file: " + configFile.getAbsolutePath());
        } catch (Exception e) {
            SkJson.exception(e, "Config file is invalid. Regenerating config file!");
            regenerateConfig();
            try {
                loadRecords();
            } catch (Exception reloadError) {
                SkJson.exception(reloadError, "Failed to load regenerated config");
            }
        }
    }

    private void regenerateConfig() {
        try {
            if (configFile.exists()) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                File brokenFile = new File(configFile.getParent(), configFile.getName().replace(".yml", ".broken-" + timestamp + ".yml"));
                if (configFile.renameTo(brokenFile)) {
                    SkJson.info("Renamed broken config file to: " + brokenFile.getAbsolutePath());
                } else {
                    SkJson.warning("Could not rename broken config file.");
                }
            }

            if (plugin.getResource(configFile.getName()) != null) {
                plugin.saveResource(configFile.getName(), false);
                SkJson.info("Config file regenerated at: " + configFile.getAbsolutePath());
            } else {
                configFile.createNewFile();
                SkJson.info("Empty config file regenerated at: " + configFile.getAbsolutePath());
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            SkJson.exception(e, "Could not regenerate config file!");
        }
    }

    public String getString(String path, String defaultValue) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
            saveConfig();
        }
        return config.getString(path, defaultValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T get(String key, Class<T> type) throws Exception {
        if (type == Boolean.class) {
            return (T) Boolean.valueOf(config.getBoolean(key));
        }
        if (type == Integer.class) {
            return (T) Integer.valueOf(config.getInt(key));
        }
        if (type.isEnum()) {
            String value = config.getString(key);
            if (value == null) {
                return null;
            }
            return (T) Enum.valueOf((Class<Enum>) type, value.toUpperCase(Locale.ROOT));
        }
        return config.getObject(key, type);
    }

    public void set(String path, Object value) {
        config.set(path, value);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            SkJson.exception(e, "Could not save config to file!");
        }
    }

    private void loadRecords() throws Exception {
        PROJECT_DEBUG = config.getBoolean("plugin.debug", false);
        PROJECT_PERMISSION = getString("plugin.permission", "skjson.use");
        PROJECT_ENABLED_NBT = config.getBoolean("plugin.enabled-nbt", false);
        PROJECT_ENABLED_HTTP = config.getBoolean("plugin.enabled-http", true);
        PROJECT_DELIM = getString("json.path-delimiter", ".");
        PATH_TOKEN_CACHE_SIZE = config.getInt("json.path-token-cache-size", 1024);
        PathParser.configureTokenCache(PATH_TOKEN_CACHE_SIZE);
        NBT_CACHE_SIZE = config.getInt("plugin.nbt-cache-size", 256);
        NBTJsonCache.configure(NBT_CACHE_SIZE);
        WATCHER_INTERVAL = config.getInt("json.watcher.interval", 100);
        WATCHER_REFRESH_RATE = config.getInt("json.watcher.refresh-rate", 50);
        WATCHER_MAX_THREADS = config.getInt("json.watcher.max-threads", 2);
        WATCHER_WATCH_TYPE = get("json.watcher.watch-type", JsonWatchType.class);
        if (WATCHER_WATCH_TYPE == null) {
            WATCHER_WATCH_TYPE = JsonWatchType.DEFAULT;
        }
        HTTP_MAX_THREADS = config.getInt("plugin.max-threads", 2);
        HTTP_REQUEST_TIMEOUT_SEC = config.getInt("plugin.http-request-timeout-seconds", 30);
        HTTP_CONNECT_TIMEOUT_SEC = config.getInt("plugin.http-connect-timeout-seconds", 10);
        CONFIG_VERSION = config.getInt("plugin.config-version", 5);
        ENABLED_AUTO_UPDATER = config.getBoolean("plugin.enabled-auto-updater", false);
        PLUGIN_FALLBACK_ENABLED = config.getBoolean("plugin.enabled-fallback", true);

        if (PLUGIN_FALLBACK_ENABLED) {
            SkJson.info("&a✓ Fallback enabled.");
        }

        if (CONFIG_VERSION != 5) {
            throw new IllegalAccessException("Plugin config version is not 5! Config version: " + CONFIG_VERSION);
        }

        switch (WATCHER_WATCH_TYPE) {
            case WSL -> SkJson.info("&7&lWATCHER* &rWatch type set to WSL - optimized for Windows Subsystem for Linux");
            case BOTH -> {
                SkJson.warning("&7&lWATCHER* &rWatch type set to BOTH - using both watch methods");
                SkJson.warning("&7&lWATCHER* &rThis mode may impact system performance");
            }
            case DEFAULT -> SkJson.info("&7&lWATCHER* &rWatch type set to DEFAULT - standard file watching");
        }

        if (containsForbiddenDelimiterChar(PROJECT_DELIM)) {
            SkJson.severe("Path delimiter '%s' contains forbidden characters (%s). Use a safe delimiter such as '.'",
                    PROJECT_DELIM, FORBIDDEN_DELIMITER_CHARS);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private static boolean containsForbiddenDelimiterChar(String delimiter) {
        for (int i = 0; i < delimiter.length(); i++) {
            if (FORBIDDEN_DELIMITER_CHARS.indexOf(delimiter.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    public void reloadConfig() {
        if (configFile.exists()) {
            try {
                this.config = YamlConfiguration.loadConfiguration(configFile);
                SkJson.info("Config reloaded from file.");
                loadRecords();
            } catch (Exception e) {
                SkJson.severe("Config file is invalid. Regenerating config file!");
                regenerateConfig();
                try {
                    loadRecords();
                } catch (Exception reloadError) {
                    SkJson.exception(reloadError, "Failed to load regenerated config");
                }
            }
        } else {
            SkJson.warning("Config file does not exist! It cannot be reloaded.");
        }
    }
}
