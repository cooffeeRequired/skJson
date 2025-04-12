package cz.coffeerequired.support;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.cache.JsonWatchType;
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

/**
 * A handler class for managing plugin configuration files.
 * This class provides methods to load, regenerate, save, and retrieve
 * configuration settings from a YAML file associated with a Bukkit/Spigot plugin.<br />
 * <b>Examples</b>
 * Example usage in your main plugin class:</br></br>
 *
 * <pre>
 *  PluginConfigHandler configHandler = new PluginConfigHandler(this);</br>
 *  String someValue = configHandler.getString("some.path", "defaultValue");</br>
 *  configHandler.set("another.path", "newValue");</br>
 *  Enum enumValue = configHandler.get("some.enum.path", Enum);</br>
 *  PluginConfigHandler customConfigHandler = new PluginConfigHandler(this, "data/types.yml");</br>
 *  </pre>
 */
@SuppressWarnings("all")
public class PluginConfigHandler {
    private final File configFile;
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public PluginConfigHandler(JavaPlugin plugin) {
        this(plugin, "config.yml");
        loadRecords();
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
                    SkJson.info("Config file created at: " + configFile.getAbsolutePath());
                } else {
                    configFile.createNewFile();
                    SkJson.info("Empty config file created at: " + configFile.getAbsolutePath());
                }
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
            SkJson.info("Loaded config file: " + configFile.getAbsolutePath());
        } catch (Exception e) {
            SkJson.exception(e, "Config file is invalid. Regenerating config file!");
            regenerateConfig();
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
    public <T> T get(String key, Class<T> type) {
        boolean isEnumClass = type.isEnum();

        if (isEnumClass) {
            String value = (String) config.get(key);
            if (value == null) return null;
            return (T) Enum.valueOf((Class<Enum>) type, value.toUpperCase(Locale.ROOT));
        } else {
            return config.getObject(key, type);
        }
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

    private void loadRecords() {
        PROJECT_DEBUG = get("plugin.debug", Boolean.class);
        PROJECT_PERMISSION = getString("plugin.permission", "");

        PROJECT_ENABLED_NBT = get("plugin.enabled-nbt", Boolean.class);
        PROJECT_ENABLED_HTTP = get("plugin.enabled-http", Boolean.class);

        PROJECT_DELIM = getString("json.path-delimiter", ".");
        WATCHER_INTERVAL = get("json.watcher.interval", Integer.class);
        WATCHER_REFRESH_RATE = get("json.watcher.refresh-rate", Integer.class);

        WATCHER_WATCH_TYPE = get("json.watcher.watch-type", JsonWatchType.class);

        // Informative messages about watch type
        switch (WATCHER_WATCH_TYPE) {
            case WSL:
                SkJson.info("Watch type set to WSL - optimized for Windows Subsystem for Linux");
                break;
            case BOTH:
                SkJson.warning("Watch type set to BOTH - using both watch methods");
                SkJson.warning("This mode may impact system performance");
                break;
            case DEFAULT:
                SkJson.info("Watch type set to DEFAULT - standard file watching");
                break;
        }

        if (PROJECT_DELIM.matches("[$#^\\[\\]{}_-]")) {
            SkJson.info("The delimiter contains not allowed unicodes.. '$#^\\/[]{}_-'");
            SkJson.severe("Restart server and change the path-delimiter to something what doesn't contains this characters '$#^\\/[]{}'");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
    }


    public void reloadConfig() {
        if (configFile.exists()) {
            try {
                this.config = YamlConfiguration.loadConfiguration(configFile);
                SkJson.info("Config reloaded from file.");
                loadRecords();
            } catch (Exception e) {
                SkJson.severe("Config file is invalid. Regenerating config file!", e);
                regenerateConfig();
            }
        } else {
            SkJson.warning("Config file does not exist! It cannot be reloaded.");
        }
    }
}

