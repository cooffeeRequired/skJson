package cz.coffeerequired.api;

import com.google.gson.JsonElement;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.CacheStorageWatcher;
import cz.coffeerequired.api.json.CachedStorage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Api {

    @Getter
    public static final ConcurrentHashMap<File, CacheStorageWatcher> watchers = new ConcurrentHashMap<>();
    @Getter
    public static final CachedStorage<String, JsonElement, File> cache = new CachedStorage<>();

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
        }

        SkJson.logger().info("Hooking into server " + serverName + " " + version + " Found 1. ...");
        SkJson.logger().info("Server type: " + type);
        SkJson.logger().info("Server version: " + version);
        SkJson.logger().info("Server name: " + serverName);

        return canInstantiateServer(type);
    }

    static boolean canInstantiateServer(ServerType type) {
        return type == ServerType.PURPUR_CORE || type == ServerType.SPIGOT_CORE || type == ServerType.SPONGE_CORE || type == ServerType.PAPER_CORE;
    }

    @SuppressWarnings("unused")
    public static class Records {
        public static final @NotNull String PROJECT_PERMISSION = "skjson.use";
        public static boolean PROJECT_DEBUG = true;
        public static boolean PROJECT_VERSION;
        public static String PROJECT_DELIM = "."; // will be loaded


        @Getter
        public static final HashMap<String, String> mapping = new HashMap<>(Map.ofEntries(
                Map.entry("PROJECT_PERMISSION", "plugin.permission"),
                Map.entry("PROJECT_DEBUG", "plugin.debug"),
                Map.entry("PROJECT_VERSION", "plugin.version"),
                Map.entry("PROJECT_DELIM", "json.path-delimiter")
        ));
    }
}
