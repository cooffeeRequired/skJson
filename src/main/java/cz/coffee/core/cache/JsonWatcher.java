package cz.coffee.core.cache;

import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.core.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static cz.coffee.SkJson.*;

public class JsonWatcher {
    private static Logger LOGGER;
    private static UUID uuid;
    private static ScheduledExecutorService service;
    private final File file;
    private final String identifier;
    private final ScheduledFuture<?> future;
    private final Map<File, JsonElement> cachedData = new ConcurrentHashMap<>();
    private long lastCheckedTimeStamp;

    public JsonWatcher(File fileInput, String identifier) {
        long interval = 300L;
        this.file = fileInput;
        this.identifier = identifier;
        future = service.scheduleAtFixedRate(this::watch, 0, interval, TimeUnit.MILLISECONDS);
    }

    public static void init() {
        LOGGER = LoggerFactory.getLogger("AsyncJsonWatcher");
        service = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "AsyncJsonWatcher"));
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static boolean isRegistered(File file) {
        for (Map.Entry<File, JsonWatcher> fileJsonWatcherEntry : WATCHERS.entrySet()) {
            if (fileJsonWatcherEntry.getKey().equals(file) && fileJsonWatcherEntry.getValue().isActive()) return true;
        }
        return false;
    }

    public static void register(String identifier, File file) {
        uuid = UUID.randomUUID();
        JsonWatcher watcher = new JsonWatcher(file, identifier);
        WATCHERS.put(file, watcher);
        if (watcher.isActive()) {
            SkJson.console("JsonWatcher registered with id&a: " + uuid + "&f for file &7(&e" + file + "&7)");
        }
    }

    public static void unregister(File file) {
        JsonWatcher assignedWatcher = null;
        for (Map.Entry<File, JsonWatcher> entry : WATCHERS.entrySet()) {
            if (file.equals(entry.getKey())) {
                assignedWatcher = entry.getValue();
                break;
            }
        }
        if (assignedWatcher != null && assignedWatcher.isActive()) {
            assignedWatcher.setCancelled(true);
            if (assignedWatcher.isCancelled() && assignedWatcher.isDone()) {
                SkJson.console("File &7(&e" + file + "&7)&7 was &fsuccessfully &aunlinked&7 from JsonWatcher (" + uuid + ")");
            }
        }
    }

    public static void shutdown() {
        service.shutdown();
    }

    private void watch() {
        try {
            long fileModifiedTime = file.lastModified();
            if (fileModifiedTime > lastCheckedTimeStamp) {
                JsonElement updatedJson;
                if (cachedData.containsKey(file)) {
                    updatedJson = cachedData.get(file);
                } else {
                    updatedJson = FileUtils.get(file);
                    cachedData.replace(file, updatedJson);
                }
                assert updatedJson != null;
                Map<JsonElement, File> map = Map.of(updatedJson, file);
                JSON_STORAGE.replace(identifier, map);
                if (PROJECT_DEBUG) LOGGER.info("File Modified: {}, Watcher ID: {}", file, uuid);
            }
            lastCheckedTimeStamp = System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.error("An error occurred while watching file: {}", file, e);
        }
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public void setCancelled(boolean cancelled) {
        future.cancel(cancelled);
    }

    public boolean isActive() {
        return !future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }
}