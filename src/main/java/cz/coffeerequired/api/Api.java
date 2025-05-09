package cz.coffeerequired.api;

import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.cache.CacheStorageWatcher;
import cz.coffeerequired.api.cache.CachedStorage;
import cz.coffeerequired.api.cache.JsonWatchType;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public abstract class Api {

    @Getter
    public static final ConcurrentHashMap<File, CacheStorageWatcher> watchers = new ConcurrentHashMap<>();
    @Getter
    public static final CachedStorage<String, JsonElement, File> cache = new CachedStorage<>();

    public static String getServerName() {
        return Bukkit.getServer().getName().toLowerCase();
    }

    /**
     * This method will also check if the given server supports all necessary requirements. <br />
     * Checking {@link org.bukkit.Server} server's version and server's name
     */
    public static boolean canInstantiateSafety() {
        ServerType type = ServerType.UNKNOWN;

        var server = Bukkit.getServer();
        final String version = server.getVersion().toLowerCase();

        String serverName = server.getName().toLowerCase();

        if (version.contains("spigot") || serverName.contains("spigot")) {
            type = ServerType.SPIGOT_CORE;
        } else if (version.contains("craftbukkit") || serverName.contains("bukkit")) {
            type = ServerType.BUKKIT_CORE;
        } else if (version.contains("sponge") || serverName.contains("sponge")) {
            type = ServerType.SPONGE_CORE;
        } else if (version.contains("purpur") || serverName.contains("purpur")) {
            type = ServerType.PURPUR_CORE;
        } else if (version.contains("paper") || serverName.contains("paper")) {
            type = ServerType.PAPER_CORE;
        } else if (version.contains("leaf") || serverName.contains("leaf")) {
            type = ServerType.LEAF;
        }
        SkJson.info("Hooking into server %s %s", serverName, version);
        SkJson.info("Server type: %s", type);
        SkJson.info("Server version: %s", version);
        SkJson.info("Server name: %s", serverName);

        if (Records.PROJECT_ENABLED_NBT) {
            if (!NBT.preloadApi()) {
                SkJson.warning("NBT API not available");
            } else {
                new NBTContainer("{A: 1.0f}");
                SkJson.info("&bNBT API&r enabled");
            }
        }

        return canInstantiateServer(type);
    }

    static boolean canInstantiateServer(ServerType type) {
        return type == ServerType.PURPUR_CORE ||
                type == ServerType.SPIGOT_CORE ||
                type == ServerType.SPONGE_CORE ||
                type == ServerType.LEAF ||
                type == ServerType.PAPER_CORE;
    }

    public static class Records {
        @Getter
        public static final HashMap<String, String> mapping = new HashMap<>(Map.ofEntries(
                Map.entry("PROJECT_PERMISSION", "plugin.permission"),
                Map.entry("PROJECT_DEBUG", "plugin.debug"),
                Map.entry("CONFIG_VERSION", "plugin.config_version"),

                Map.entry("PROJECT_DELIM", "json.path-delimiter"),

                Map.entry("PROJECT_ENABLED_HTTP", "plugin.enabled-http"),
                Map.entry("PROJECT_ENABLED_NBT", "plugin.enabled-nbt"),
                Map.entry("HTTP_MAX_THREADS", "plugin.http-max-threads"),

                Map.entry("WATCHER_INTERVAL", "json.watcher.interval"),
                Map.entry("WATCHER_REFRESH_RATE", "json.watcher.refresh-rate"),
                Map.entry("WATCHER_WATCH_TYPE", "json.watcher.watch-type"),
                Map.entry("WATCHER_MAX_THREADS", "json.watcher.max-threads"),

                Map.entry("DISABLED_UPDATE", "plugin.enabled-auto-updater"),
                Map.entry("PLUGIN_FALLBACK_ENABLED", "plugin.enabled-fallback")
        ));

        public static @NotNull String PROJECT_PERMISSION = "skjson.use";
        public static boolean PROJECT_DEBUG = true;
        public static boolean PROJECT_ENABLED_HTTP;
        public static boolean PROJECT_ENABLED_NBT;
        public static String PROJECT_DELIM = "."; // will be loaded
        public static Integer HTTP_MAX_THREADS;
        public static int WATCHER_INTERVAL;
        public static int WATCHER_MAX_THREADS;
        public static int WATCHER_REFRESH_RATE;
        public static JsonWatchType WATCHER_WATCH_TYPE;
        public static Integer CONFIG_VERSION;
        public static boolean DISABLED_UPDATE;
        public static boolean PLUGIN_FALLBACK_ENABLED;
    }
}
