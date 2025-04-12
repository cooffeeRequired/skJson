package cz.coffeerequired.api.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import cz.coffeerequired.api.annotators.ExternalAPI;
import cz.coffeerequired.skript.core.bukkit.JsonFileChanged;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * This class handles all changes in the JSON files located in a given folder.
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * String folderPath = "/path/to/json/folder";
 * JsonFileWatcher watcher = new JsonFileWatcher(folderPath);
 * watcher.startWatching();
 * }
 * </pre>
 *
 * <p>This will start watching the specified folder for any JSON file changes
 * (creation or modification) and process them according to your implementation
 * in the processJsonFile method.</p>
 */


public class CacheStorageWatcher {
    private static final ScheduledExecutorService service;
    private static final long DEFAULT_INTERVAL = 1000;

    static {
        service = Executors.newSingleThreadScheduledExecutor();
    }

    @Getter
    private final UUID uuid;
    private final ScheduledFuture<?> future;
    private final String parentID;
    @Getter
    private final String id;
    @Getter
    private final File file;
    private JsonFileChanged event;
    private WatchService watchService;

    public CacheStorageWatcher(File file, String id, String parentID, long interval) {
        this.uuid = UUID.randomUUID();
        this.parentID = parentID;
        this.id = id;
        this.file = file;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            file.toPath().getParent().register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            SkJson.exception(e, e.getMessage(), e);
        }

        this.future = service.scheduleAtFixedRate(this::watch, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void watch() {
        CachedStorage<String, JsonElement, File> cache = Api.getCache();
        JsonElement jsonifyFile = null;
        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(file.toPath()));
            if (!isValidJson(jsonContent)) {
                SkJson.warning("Invalid JSON content in " + file.getName());
                return;
            }
            jsonifyFile = JsonParser.parseString(jsonContent);
        } catch (IOException e) {
            SkJson.exception(e, e.getMessage(), e);
        }

        String[] splitParentID = parentID.split(";");
        ConcurrentHashMap<JsonElement, File> fromCache;
        JsonElement potentialJsonContent;

        if (parentID.contains(";")) {
            fromCache = cache.getValuesById(splitParentID[0]).join();
            potentialJsonContent = ((JsonElement) fromCache.keySet().toArray()[0]).getAsJsonObject().get(splitParentID[1]);
        } else {
            fromCache = cache.getValuesById(id).join();
            potentialJsonContent = ((JsonElement) fromCache.keySet().toArray()[0]).getAsJsonObject();
        }

        if (!Objects.equals(potentialJsonContent, jsonifyFile)) {
            assert jsonifyFile != null;
            var map = new ConcurrentHashMap<>(Map.of(jsonifyFile, file));
            this.event.setJson(jsonifyFile);
            cache.replace(id, map);
            //SkJson.debug("File %s was modified", file.getName());
        }

        WatchKey key;
        try {
            key = watchService.poll(10, TimeUnit.MILLISECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().equals(file.toPath().getFileName())) {
                        SkJson.debug("WatchService detected change in %s", file.getName());
                    }
                    break;
                }
                SkJson.debug("before call");
                this.event.callEvent();
                key.reset();
            }
        } catch (InterruptedException e) {
            SkJson.exception(e, String.format("An error occurred while watching file: %s, exception: %s", file, e.getCause().getLocalizedMessage()), e);
        }
    }

    private boolean isActive() {
        return future != null && !future.isCancelled();
    }

    private void invokeEvent(JsonFileChanged event) {
        assert event != null;
        this.event = event;
    }

    public boolean isCancelled() {
        return future != null && future.isCancelled();
    }

    public void cancel() {
        future.cancel(true);
    }

    public boolean isDone() {
        return future != null && future.isDone();
    }

    private boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }


    // STATIC SECTION

    @ExternalAPI
    public static class Extern {
        public static void register(String id, File file, String... parentCaches) {
            String parent = parentCaches != null && parentCaches.length > 0 && parentCaches[0] != null ? parentCaches[0] : null;
            Boolean[] found = new Boolean[]{false};

            var watchers = Api.getWatchers();

            watchers.forEachKey(1, f -> {
                if (f.equals(file)) {
                    SkJson.warning(String.format("Watcher for file %s is already registered", file));
                    found[0] = true;
                }
            });

            SkJson.debug("Was found: %s", found[0]);

            synchronized (watchers) {
                if (! found[0]) {
                    SkJson.debug("Registering watcher for file " + file.getName());

                    String parentFile = parent != null ? parent : file.toString();
                    CacheStorageWatcher watcher = new CacheStorageWatcher(file, id, parentFile, DEFAULT_INTERVAL);
                    watcher.invokeEvent(new JsonFileChanged(watcher.file, watcher.id, watcher.uuid, new JsonObject()));

                    watchers.put(file, watcher);

                    SkJson.debug("Watcher: %s ", watcher);

                    if (watcher.isActive())
                        SkJson.info(String.format("Registered with uuid: %s &f for file &7(&e%s&7)", watcher.uuid, watcher.file));
                }
            }
        }

        public static void unregister(final File file) {
            Api.getWatchers().computeIfPresent(file, (f, w) -> {
                if (w.isActive()) {
                    w.cancel();
                    if (w.isCancelled() && w.isDone())
                        SkJson.info(String.format("File &7(&e%s&7) was &asuccessfully &7unlinked frm watcher{%s}", f, w.getUuid()));
                }
                return null;
            });
        }

        public static void unregisterAll() {
            try {
                SkJson.info("Unregistering all watchers...");
                Api.getWatchers().forEach((f, c) -> unregister(f));
                SkJson.info("Unregistering all watchers &asuccessfully!");
            } catch (Exception e) {
                SkJson.warning("Unregistering all watchers &cfailed!");
            }
        }

        public static boolean hasRegistered(final File file) {
            return Api.getWatchers().values().stream().anyMatch(w -> w.getFile().equals(file) && w.isActive());
        }
    }
}
