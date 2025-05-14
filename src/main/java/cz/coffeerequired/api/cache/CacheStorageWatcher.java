package cz.coffeerequired.api.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.annotators.ExternalAPI;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
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
    private final WatchService watchService;
    private final AtomicReference<JsonElement> lastContent = new AtomicReference<>();
    private final CachedStorage<String, JsonElement, File> cache;
    private final Supplier<JsonElement> fileSupplier;
    private JsonFileChanged event;

    public CacheStorageWatcher(File file, String id, String parentID, long interval,
                               WatchService watchService,
                               ScheduledExecutorService executor,
                               CachedStorage<String, JsonElement, File> cache,
                               Supplier<JsonElement> fileSupplier) throws IOException {
        this.id = id;
        this.file = file;
        this.parentID = parentID;
        this.watchService = watchService;
        this.cache = cache;
        this.fileSupplier = fileSupplier;

        file.toPath().getParent().register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

        this.future = executor.scheduleWithFixedDelay(this::watch, 0, interval, TimeUnit.MILLISECONDS);
    }

    public CacheStorageWatcher(File file, String id, String parentID, long interval) throws IOException {
        this(file, id, parentID, interval,
                createWatchService(file),
                createExecutorService(),
                Api.getCache(),
                () -> FileHandler.get(file).join());
    }

    private static WatchService createWatchService(File ignoredFile) {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            SkJson.exception(e, "Failed to create WatchService", e);
            throw new RuntimeException(e);
        }
    }

    private static ScheduledExecutorService createExecutorService() {
        return Executors.newScheduledThreadPool(WATCHER_MAX_THREADS, r -> {
            Thread thread = new Thread(r);
            thread.setName("CacheStorageWatcher-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        });
    }

    private void watch() {
        try {
            WatchKey key = Api.Records.WATCHER_WATCH_TYPE.equals(JsonWatchType.DEFAULT)
                    ? watchService.poll()
                    : watchService.poll(WATCHER_REFRESH_RATE, TimeUnit.MILLISECONDS);

            if (key != null) {
                JsonElement jsonifyFile;
                try {
                    jsonifyFile = fileSupplier.get();
                } catch (Exception e) {
                    SkJson.exception(e, "Failed to read file content: %s".formatted(file.getName()));
                    key.reset();
                    return;
                }

                if (jsonifyFile == null) {
                    SkJson.warning("File content is null: %s".formatted(file.getName()));
                    key.reset();
                    return;
                }

                var potentialJsonContent = resolveParentJson();

                if (!Objects.equals(jsonifyFile, lastContent.get())) {
                    handleChange(jsonifyFile, potentialJsonContent, key);
                    lastContent.set(jsonifyFile);
                }

                key.reset();
            }
        } catch (InterruptedException e) {
            SkJson.exception(e, "Error while watching file: %s".formatted(file), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            SkJson.exception(e, "Unexpected error while watching file: %s".formatted(file), e);
        }
    }

    private JsonElement resolveParentJson() {
        try {
            if (parentID.contains(";")) {
                String[] split = parentID.split(";");
                var fromCache = cache.getValuesById(split[0]);
                return fromCache.jsonElement().getAsJsonObject();
            } else {
                var fromCache = cache.getValuesById(id);
                return fromCache.jsonElement().getAsJsonObject();
            }
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private void handleChange(JsonElement jsonifyFile, JsonElement potentialJsonContent, WatchKey key) {
        for (WatchEvent<?> event : key.pollEvents()) {
            if (!Objects.equals(event.context(), file.toPath().getFileName()) || jsonifyFile == null) continue;

            if (potentialJsonContent.equals(jsonifyFile)) {
                continue;
            }

            if (! file.exists()) {
                SkJson.warning("file %s does not exist, unlinking watcher", file);
                Extern.unregister(file);
                continue;
            }

            var cacheLink = new CacheLink<>(jsonifyFile, file);
            this.event.setJson(jsonifyFile);
            cache.replace(id, cacheLink);
            SkJson.debug("File %s was modified", file.getName());
            this.event.callEvent();
            break;
        }
    }

    public boolean isActive() {
        return future != null && !future.isCancelled();
    }

    public void cancel() {
        if (future != null) future.cancel(true);
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
                SkJson.warning("Watcher for file %s is already registered".formatted(file));
                return;
            }

            synchronized (watchers) {
                if (!watchers.containsKey(file)) {
                    SkJson.debug("Registering watcher for file %s".formatted(file.getName()));
                    String parentFile = (parent != null) ? parent : file.toString();

                    var watcher = new CacheStorageWatcher(file, id, parentFile, WATCHER_INTERVAL);
                    watcher.invokeEvent(new JsonFileChanged(watcher.file, watcher.id, watcher.uuid, new JsonObject()));
                    watchers.put(file, watcher);

                    if (watcher.isActive()) {
                        SkJson.info("Registered with uuid: %s for file (%s)".formatted(watcher.uuid, watcher.file));
                    }
                }
            }
        }

        public static void unregister(File file) {
            Api.getWatchers().computeIfPresent(file, (f, w) -> {
                if (w.isActive()) {
                    w.cancel();
                    if (w.isCancelled() && w.isDone()) {
                        SkJson.info("File (%s) was successfully unlinked from watcher{%s}".formatted(f, w.getUuid()));
                    }
                }
                return null;
            });
        }

        public static void unregisterAll() {
            try {
                SkJson.info("Unregistering all watchers...");
                Api.getWatchers().keySet().forEach(Extern::unregister);
                SkJson.info("All watchers successfully unregistered!");
            } catch (Exception e) {
                SkJson.warning("Unregistering all watchers failed!");
            }
        }

        public static boolean hasRegistered(File file) {
            return Api.getWatchers().values().stream()
                    .anyMatch(w -> w.getFile().equals(file) && w.isActive());
        }
    }
}
