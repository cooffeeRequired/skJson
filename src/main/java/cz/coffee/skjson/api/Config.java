package cz.coffee.skjson.api;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import com.google.gson.JsonElement;
import cz.coffee.skjson.api.Cache.JsonCache;
import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Update.UpdateCheck;
import cz.coffee.skjson.utils.Util;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Config.
 */
@SuppressWarnings("ALL")
public class Config {
    /**
     * The constant SERVER_TYPE.
     */
    public static String SERVER_TYPE;
    /**
     * The constant RUN_TEST_ON_START.
     */
    public static boolean RUN_TEST_ON_START;
    /**
     * The constant TEST_START_UP_DELAY.
     * Can be only 1, 2, 3
     */
    public static int TEST_START_UP_DELAY;

    /**
     * The constant TESTS_ALOWED.
     */
    public static boolean TESTS_ALOWED;
    /**
     * The constant PATH_VARIABLE_DELIMITER.
     */
    public static String PATH_VARIABLE_DELIMITER;

    /**
     * The constant PROJECT_DEBUG.
     */
    public static boolean PROJECT_DEBUG;
    /**
     * The constant LOGGING_LEVEL.
     */
    public static int LOGGING_LEVEL;
    /**
     * The constant PLUGIN_PREFIX.
     */
    public static String PLUGIN_PREFIX;
    /**
     * The constant ERROR_PREFIX.
     */
    public static String ERROR_PREFIX;
    /**
     * The constant WATCHER_PREFIX.
     */
    public static String WATCHER_PREFIX;

    /**
     * The constant DEFAULT_WATCHER_INTERVAL.
     */
    public static Long DEFAULT_WATCHER_INTERVAL;

    /**
     * The constant REQUESTS_PREFIX.
     */
    public static String REQUESTS_PREFIX;

    /**
     * The constant WEBHOOK_PREFIX.
     */
    public static String WEBHOOK_PREFIX;

    /**
     * The constant ALLOWED_MULTILINE_LITERAL.
     */
    public static Boolean ALLOWED_MULTILINE_LITERAL;
    /**
     * The constant ALLOWED_LINE_LITERAL.
     */
    public static Boolean ALLOWED_LINE_LITERAL;
    /**
     * The constant ALLOWED_IMPLICIT_REQUEST_RETURN.
     */
    public static Boolean ALLOWED_IMPLICIT_REQUEST_RETURN;


    /**
     * The Logger.
     */
    final SkJsonLogger logger;
    /**
     * The Plugin.
     */
    final JavaPlugin plugin;
    /**
     * The Manager.
     */
    PluginManager manager;

    /**
     * The Version.
     */
    int version;

    /**
     * The constant watcherCache.
     */
    public static ConcurrentHashMap<File, JsonWatcher> watcherCache = new ConcurrentHashMap<>();
    private boolean ready = true;
    private final ArrayList<String> errors = new ArrayList<>();
    private File configFile;
    private FileConfiguration config;
    private static Config staticConfig;
    /**
     * The constant pluginYaml.
     */
    public static YamlConfiguration pluginYaml;


    /**
     * Instantiates a new Config.
     *
     * @param plugin the plugin
     */
    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        logger = SkJsonLogger.getLogger("");
        MinecraftVersion.replaceLogger(this.logger);
        Config.staticConfig = this;
    }


    /**
     * Load config file.
     *
     * @param replace the replace
     * @return the void
     */
    public void loadConfigFile(boolean replace) {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", replace);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        matchConfig();
        loadConfigs();
    }

    /**
     * Load tests void.
     *
     * @param skFile the sk file
     * @return the void
     * @throws IOException the io exception
     */
    public void loadTests(String skFile) throws IOException {
        try {
            var file = new File(plugin.getDataFolder(), "..tests");
            if (!file.exists()) {
                if (file.mkdir()) {
                    var in = plugin.getResource("tests/" + skFile);
                    // Vytvoření instance Runtime
                    Runtime runtime = Runtime.getRuntime();
                    // Spuštění příkazu atributu pro skrytí složky
                    File tempFile = new File(plugin.getDataFolder() + "/" + "..tests", skFile);
                    Process process = runtime.exec("attrib +h " + file);
                    file.createNewFile();
                    var os = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
                    os.close();
                    in.close();

                } else if (file.isDirectory()) {
                    var in = plugin.getResource("tests/" + skFile);
                    // Vytvoření instance Runtime
                    Runtime runtime = Runtime.getRuntime();
                    // Spuštění příkazu atributu pro skrytí složky
                    File tempFile = new File(plugin.getDataFolder() + "/" + "..tests", skFile);
                    Process process = runtime.exec("attrib +h " + file);
                    file.createNewFile();
                    var os = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
                    os.close();
                    in.close();
                }
            } else {
                var in = plugin.getResource("tests/" + skFile);
                // Vytvoření instance Runtime
                Runtime runtime = Runtime.getRuntime();
                // Spuštění příkazu atributu pro skrytí složky
                File tempFile = new File(plugin.getDataFolder() + "/" + "..tests", skFile);
                Process process = runtime.exec("attrib +h " + file);
                file.createNewFile();
                var os = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
                os.close();
                in.close();
            }
        } catch (Exception e) {
            //
        }
    }

    /**
     * Gets plugin config.
     *
     * @param path the path
     * @return the plugin config
     */
    public static Object getPluginConfig(String path) {
        if (Config.pluginYaml != null) {
            return Config.pluginYaml.get(path);
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    private void matchConfig() {
        try {
            boolean hasUpdated = false;
            InputStream stream = plugin.getResource(configFile.getName());
            assert stream != null;
            InputStreamReader is = new InputStreamReader(stream);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(is);
            for (String key : defConfig.getConfigurationSection("").getKeys(true)) {
                if (!config.contains(key)) {
                    config.set(key, defConfig.get(key));
                    hasUpdated = true;
                }
            }
            for (String key : config.getConfigurationSection("").getKeys(true)) {
                if (!defConfig.contains(key)) {
                    config.set(key, null);
                    hasUpdated = true;
                }
            }
            if (hasUpdated) config.save(configFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getSetting(String setting) {
        return this.config.getBoolean("settings." + setting);
    }

    private String getString(String setting) {
        return this.config.getString("settings." + setting);
    }

    private int getInt(String setting) {
        return this.config.getInt("settings." + setting);
    }

    private long getLong(String setting) {
        return this.config.getLong("settings." + setting);
    }

    private String getPrefix(String setting) {
        return this.config.getString("settings.prefixes." + setting);
    }
    private Boolean getFeatures(String feature) {return this.config.getBoolean("settings.features." + feature);}

    private void loadConfigs() {
        try {
            PROJECT_DEBUG = getSetting("debug");
            LOGGING_LEVEL = getInt("logging-level");
            DEFAULT_WATCHER_INTERVAL = getLong("watcher-interval");
            PLUGIN_PREFIX = getPrefix("plugin");
            ERROR_PREFIX = getPrefix("error");
            WATCHER_PREFIX = getPrefix("watcher");
            REQUESTS_PREFIX = getPrefix("request");
            WEBHOOK_PREFIX = getPrefix("webhook");
            PATH_VARIABLE_DELIMITER = getString("path-delimiter");
            TESTS_ALOWED = getSetting("test-allowed");
            RUN_TEST_ON_START = getSetting("run-tests-on-startup");
            ALLOWED_LINE_LITERAL = getFeatures("literal-parsing-single-line");
            ALLOWED_MULTILINE_LITERAL = getFeatures("literal-parsing-multi-line");
            ALLOWED_IMPLICIT_REQUEST_RETURN = getFeatures("force-async-return");
            int delayStart = getInt("startup-tests-delay");
            if (delayStart == 1) {
                TEST_START_UP_DELAY = 10_000;
            } else if (delayStart == 2) {
                TEST_START_UP_DELAY = 25_000;
            } else if (delayStart == 3) {
                TEST_START_UP_DELAY = 35_000;
            } else {
                TEST_START_UP_DELAY = 5000;
            }

            if (PATH_VARIABLE_DELIMITER.matches("[$#^\\/\\[\\]\\{\\}_-]")) {
                Util.error("The delimiter contains not allowed unicodes.. '$#^\\/[]{}_-'");
                Util.error("Restart server and change the path-delimiter to something what doesn't contains this characters '$#^\\/[]{}'");
                manager.disablePlugin(plugin);
            }
        } catch (Exception ignored) {
            try {
                Util.log("&e&lConfig.yaml was fixed... Cause missing entry");
                loadConfigFile(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * The constant cache.
     */
    final static JsonCache<String, JsonElement, File> cache = new JsonCache<>();

    /**
     * Init.
     *
     * @return the void
     * @throws IOException the io exception
     */
    public void init() throws IOException {
        try {
            loadConfigFile(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            InputStream stream = Config.getConfig().plugin.getResource("plugin.yml");
            if (stream != null) {
                InputStreamReader is = new InputStreamReader(stream);
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(is);
                Config.pluginYaml = yml;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        manager = Bukkit.getPluginManager();

        try {
            new NBTContainer("{a:0}");
            logger.info("[NBTAPI] Was loaded &asuccessfully.");
        } catch (Exception ignored) {
            ready = false;
            Util.error("&#adfa6eN&#53db88B&#00b797T&#009294A&#006c7eP&#2a4858I &r Wasn't load &successfully");
        }


        try {
            if (!Util.versionError(Skript.getVersion(), new Version("2.7.0-beta3"), true, manager, plugin)) return;

            ready = classesRegistration(plugin);
            String metricsPrefix = "&#e3e512M&#a6e247e&#6cda6et&#2ece8dr&#00bfa4i&#00afafc&#329dads&r ";
            setupMetrics(17374, (JavaPlugin) plugin);
            Util.log(metricsPrefix + "Was loaded &asuccessfully.");
        } catch (Exception ignored) {
            ready = false;
            errors.add("Couldn't initialize Metrics'");
        }
        ready = initializeSkript(manager, plugin.getDescription().getDepend().get(0));


        if (errors.size() > 0) {
            Util.error("&cFound errors while skJson starting, SkJson will be &cdisabled");
            for (int i = 0; i < errors.size(); i++) {
                String error = errors.get(i);
                Util.log(String.format("&7→ %s. &c%s", i, error));
            }
            manager.disablePlugin(plugin);
        }

        try {
            JsonWatcher.init(this);
            Util.watcherLog("was &ainitialized.");
        } catch (Exception ignored) {
            errors.add("JsonWatcher Couldn't been &cinitialized.");
        }

        ready = new UpdateCheck((JavaPlugin) plugin, this).getReady();
        ready = registerCommand(plugin, "skjson");
        loadTests("changer-skjson.sk");
    }


    /**
     * Gets config.
     *
     * @return the config
     */
    public static Config getConfig() {
        return staticConfig;
    }

    /**
     * Ready boolean.
     *
     * @return the boolean
     */
    public boolean ready() {
        return ready;
    }

    /**
     * Gets cache.
     *
     * @return the cache
     */
    public static JsonCache<String, JsonElement, File> getCache() {
        return cache;
    }

    /**
     * Sets metrics.
     *
     * @param id     the id
     * @param plugin the plugin
     * @return the metrics
     */
    public void setupMetrics(int id, JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, id);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));
        metrics.addCustomChart(new SimplePie("skjson_version", () -> plugin.getPluginMeta().getVersion()));
    }

    /**
     * Initialize skript boolean.
     *
     * @param manager    the manager
     * @param dependency the dependency
     * @return the boolean
     */
    public boolean initializeSkript(PluginManager manager, String dependency) {
        Plugin plugin = manager.getPlugin(dependency);
        if (plugin == null) {
            errors.add("Dependency [Skript] weren't found. Check your /plugins folder");
            return false;
        } else if (!plugin.isEnabled()) {
            errors.add("&cOpps! Seems like SkJson was loaded before Skript, something delayed the start. Try restart your server");
            return false;
        }
        String skriptPrefix = "&#e3e512S&#9ae150k&#55d57br&#00c59ci&#00b2aep&#329dadt&r";
        Util.log(skriptPrefix + " was found and &ainitialized.");
        return true;
    }

    /**
     * Classes registration boolean.
     *
     * @param plugin the plugin
     * @return the boolean
     */
    public boolean classesRegistration(Plugin plugin) {
        try {
            SkriptAddon addon = Skript.registerAddon((JavaPlugin) plugin);
            addon.setLanguageFileDirectory("lang");
            addon.loadClasses("cz.coffee.skjson.skript");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            errors.add("Skript cannot register SkJson classes!");
            return false;
        }
    }

    /**
     * Sets current version.
     *
     * @param version the version
     * @return the current version
     */
    public void setCurrentVersion(int version) {
        this.version = version;
    }

    /**
     * Register command boolean.
     *
     * @param plugin  the plugin
     * @param command the command
     * @return the boolean
     */
    public boolean registerCommand(JavaPlugin plugin, String command) {
        try {
            plugin.getCommand(command).setExecutor(new SkJsonCommand());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
