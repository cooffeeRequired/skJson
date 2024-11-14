package cz.coffeerequired.support;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final Logger logger;
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public PluginConfigHandler(JavaPlugin plugin) {
        this(plugin, "config.yml");
    }

    public PluginConfigHandler(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.configFile = new File(plugin.getDataFolder(), fileName);
        loadOrRegenerateConfig();
    }


    private void loadOrRegenerateConfig() {
        try {
            if (!configFile.exists()) {
                if (plugin.getResource(configFile.getName()) != null) {
                    plugin.saveResource(configFile.getName(), false);
                    logger.info("Config file created at: " + configFile.getAbsolutePath());
                } else {
                    configFile.createNewFile();
                    logger.info("Empty config file created at: " + configFile.getAbsolutePath());
                }
            }

            this.config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Config file is invalid. Regenerating config file!", e);
            regenerateConfig();
        }
    }


    private void regenerateConfig() {
        try {
            if (configFile.exists()) {

                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                File brokenFile = new File(configFile.getParent(), configFile.getName().replace(".yml", ".broken-" + timestamp + ".yml"));
                if (configFile.renameTo(brokenFile)) {
                    logger.info("Renamed broken config file to: " + brokenFile.getAbsolutePath());
                } else {
                    logger.warning("Could not rename broken config file.");
                }
            }

            if (plugin.getResource(configFile.getName()) != null) {
                plugin.saveResource(configFile.getName(), false);
                logger.info("Config file regenerated at: " + configFile.getAbsolutePath());
            } else {
                configFile.createNewFile();
                logger.info("Empty config file regenerated at: " + configFile.getAbsolutePath());
            }
            this.config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not regenerate config file!", e);
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
            return (T) Enum.valueOf((Class<Enum>) type, value);
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
            logger.log(Level.SEVERE, "Could not save config to file!", e);
        }
    }


    public void reloadConfig() {
        if (configFile.exists()) {
            try {
                this.config = YamlConfiguration.loadConfiguration(configFile);
                logger.info("Config reloaded from file.");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Config file is invalid. Regenerating config file!", e);
                regenerateConfig();
            }
        } else {
            logger.log(Level.WARNING, "Config file does not exist! It cannot be reloaded.");
        }
    }
}

