package cz.coffeerequired.api.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.annotators.ExternalAPI;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static cz.coffeerequired.api.Api.Records.*;

public class CacheStorageWatcher {
    @Getter
    private final UUID uuid = UUID.randomUUID();
    private final ScheduledFuture<?> future;
    private final String parentID;
    @Getter
    private final String id;
    @Getter
    private final File file;
    private final WatchKey watchKey;
    private final AtomicLong lastModifiedMillis = new AtomicLong(-1L);
    private final AtomicLong lastContentHash = new AtomicLong(0L);
    private final CachedStorage<String, JsonElement, File> cache;
    private final Supplier<JsonElement> fileSupplier;
    private JsonFileChanged event;

    public CacheStorageWatcher(File file, String id, String parentID, long interval,
                               WatchService watchService,
                               CachedStorage<String, JsonElement, File> cache,
                               Supplier<JsonElement> fileSupplier) throws IOException {
        this.id = id;
        this.file = file;
        this.parentID = parentID;
        this.cache = cache;
        this.fileSupplier = fileSupplier;

        Path parent = file.toPath().getParent();
        if (parent == null) {
            throw new IOException("Cannot watch file without parent directory: " + file);
        }
        this.watchKey = parent.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        this.future = WatcherResources.executor().scheduleWithFixedDelay(this::watch, interval, interval, TimeUnit.MILLISECONDS);
    }

    public CacheStorageWatcher(File file, String id, String parentID, long interval) throws IOException {
        this(file, id, parentID, interval,
                WatcherResources.watchService(),
                Api.getCache(),
                () -> FileHandler.get(file).join());
    }

    private void watch() {
        try {
            WatchKey key = WATCHER_WATCH_TYPE.equals(JsonWatchType.DEFAULT)
                    ? watchService().poll()
                    : watchService().poll(WATCHER_REFRESH_RATE, TimeUnit.MILLISECONDS);

            if (key == null) {
                return;
            }

            boolean relevant = false;
            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                Object context = watchEvent.context();
                if (Objects.equals(context, file.getName())) {
                    relevant = true;
                    break;
                }
            }
            key.reset();

            if (!relevant || !file.exists()) {
                if (!file.exists()) {
                    SkJson.warning("File %s does not exist, unlinking watcher", file);
                    Extern.unregister(file);
                }
                return;
            }

            long modified = Files.getLastModifiedTime(file.toPath()).toMillis();
            if (modified == lastModifiedMillis.get()) {
                return;
            }

            JsonElement jsonifyFile;
            try {
                jsonifyFile = fileSupplier.get();
            } catch (Exception e) {
                SkJson.exception(e, "Failed to read file content: %s", file.getName());
                return;
            }

            if (jsonifyFile == null || jsonifyFile.isJsonNull()) {
                SkJson.warning("File content is null: %s", file.getName());
                return;
            }

            long contentHash = jsonifyFile.hashCode();
            if (contentHash == lastContentHash.get()) {
                lastModifiedMillis.set(modified);
                return;
            }

            JsonElement potentialJsonContent = resolveParentJson();
            if (potentialJsonContent.equals(jsonifyFile)) {
                lastModifiedMillis.set(modified);
                lastContentHash.set(contentHash);
                return;
            }

            lastModifiedMillis.set(modified);
            lastContentHash.set(contentHash);

            var cacheLink = new CacheLink<>(jsonifyFile, file);
            Bukkit.getScheduler().runTask(SkJson.getInstance(), () -> {
                this.event.setJson(jsonifyFile);
                cache.put(id, cacheLink);
                SkJson.debug("File %s was modified", file.getName());
                this.event.callEvent();
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            SkJson.exception(e, "Unexpected error while watching file: %s", file);
        }
    }

    private WatchService watchService() {
        return WatcherResources.watchService();
    }

    private JsonElement resolveParentJson() {
        try {
            if (parentID.contains(";")) {
                String[] split = parentID.split(";");
                var fromCache = cache.getValuesById(split[0]);
                if (fromCache == null) {
                    return new JsonObject();
                }
                return fromCache.jsonElement().getAsJsonObject();
            }
            var fromCache = cache.getValuesById(id);
            if (fromCache == null) {
                return new JsonObject();
            }
            return fromCache.jsonElement();
        } catch (Exception e) {
            SkJson.exception(e, "Error while resolving parent JSON for file: %s", file.getName());
            return new JsonObject();
        }
    }

    public boolean isActive() {
        return future != null && !future.isCancelled();
    }

    public void cancel() {
        if (future != null) {
            future.cancel(true);
        }
        if (watchKey != null) {
            watchKey.cancel();
        }
    }

    public boolean isCancelled() {
        return future != null && future.isCancelled();
    }

    public boolean isDone() {
        return future != null && future.isDone();
    }

    private void invokeEvent(JsonFileChanged event) {
        this.event = event;
    }

    @ExternalAPI
    public static class Extern {
        public static void register(String id, File file, String... parentCaches) throws IOException {
            String parent = (parentCaches != null && parentCaches.length > 0) ? parentCaches[0] : null;

            var watchers = Api.getWatchers();
            if (watchers.containsKey(file)) {
                SkJson.warning("Watcher for file %s is already registered", file);
                return;
            }

            watchers.computeIfAbsent(file, f -> {
                try {
                    SkJson.debug("Registering watcher for file %s", file.getName());
                    String parentFile = parent != null ? parent : file.toString();

                    var watcher = new CacheStorageWatcher(file, id, parentFile, WATCHER_INTERVAL);
                    watcher.invokeEvent(new JsonFileChanged(watcher.file, watcher.id, watcher.uuid, new JsonObject()));

                    if (watcher.isActive()) {
                        SkJson.info("Registered with uuid: %s for file (%s)", watcher.uuid, watcher.file);
                    }
                    return watcher;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to register watcher for " + file, e);
                }
            });
        }

        public static void unregister(File file) {
            Api.getWatchers().computeIfPresent(file, (f, w) -> {
                if (w.isActive()) {
                    w.cancel();
                    SkJson.info("File (%s) was successfully unlinked from watcher{%s}", f, w.getUuid());
                }
                return null;
            });
        }

        public static void unregisterAll() {
            try {
                SkJson.info("Unregistering all watchers...");
                Api.getWatchers().keySet().forEach(Extern::unregister);
                WatcherResources.shutdown();
                SkJson.info("All watchers successfully unregistered!");
            } catch (Exception e) {
                SkJson.warning("Unregistering all watchers failed!");
            }
        }

        public static boolean hasRegistered(File file) {
            return Api.getWatchers().containsKey(file);
        }
    }
}
