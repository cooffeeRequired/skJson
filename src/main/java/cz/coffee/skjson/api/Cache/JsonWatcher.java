package cz.coffee.skjson.api.Cache;

import com.google.gson.JsonElement;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.FileHandler;
import cz.coffee.skjson.skript.events.bukkit.EventWatcherSave;

import java.io.File;
import java.nio.file.*;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static cz.coffee.skjson.api.Config.getCache;
import static cz.coffee.skjson.api.Config.watcherCache;
import static cz.coffee.skjson.api.ConfigRecords.DEFAULT_WATCHER_INTERVAL;
import static cz.coffee.skjson.api.ConfigRecords.PROJECT_DEBUG;
import static cz.coffee.skjson.utils.Logger.watcherLog;

/**
 * The type Json watcher.
 */
public class JsonWatcher {
    private final File file;
    private final String id;
    private final String parentID;
    private EventWatcherSave event;
    private final WatchService watchService;
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "JsonWatcher"));
    private final ScheduledFuture<?> future;
    private final UUID uuid;
    /**
     * The Config.
     */
    static Config config;

    /**
     * Gets id.
     *
     * @return the id
     */
    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    /**
     * Instantiates a new Json watcher.
     *
     * @param file     the file
     * @param id       the id
     * @param interval the interval
     */
    public JsonWatcher(File file, String id, String parentID, long interval) {
        this.file = file;
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.parentID = parentID;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            file.toPath().getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize file watch service.", e);
        }

        this.future = service.scheduleAtFixedRate(this::watch, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Init.
     *
     * @param config the config
     */
    public static void init(Config config) {
        JsonWatcher.config = config;
    }

    /**
     * Is registered boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isRegistered(File file) {
        return watcherCache.values().stream()
                .anyMatch(watcher -> watcher.getFile().equals(file) && watcher.isActive());
    }

    public void setEvent(EventWatcherSave event) {
        this.event = event;
    }

    /**
     * Register.
     *
     * @param id   the id
     * @param file the file
     */
    public static void register(String id, File file, String... orgParentCache) {
        String parent = orgParentCache != null && orgParentCache.length > 0 && orgParentCache[0] != null ? orgParentCache[0] : null;
        AtomicBoolean found = new AtomicBoolean(false);
        watcherCache.forEachKey(1, file_ -> {
            if (file_.equals(file)) {
                watcherLog("Watcher for file " + file + " is already registered!");
                found.set(true);
            }
        });
        if (!found.get()) {
            String parentFile = parent != null ? parent : file + "";
            JsonWatcher watcher = new JsonWatcher(file, id, parentFile, DEFAULT_WATCHER_INTERVAL);
            watcher.setEvent(new EventWatcherSave(file, id, watcher.getUuid()));
            watcherCache.put(file, watcher);
            if (watcher.isActive()) {
                watcherLog("Registered with id: &a" + watcher.getUuid() + "&f for file &7(&e" + file + "&7)");
            }
        }
    }


    /**
     * Unregister.
     *
     * @param file the file
     */
    public static void unregister(File file) {
        watcherCache.computeIfPresent(file, (f, watcher) -> {
            if (watcher.isActive()) {
                watcher.setCancelled(true);
                if (watcher.isCancelled() && watcher.isDone()) {
                    watcherLog("File &7(&e" + file + "&7)&7 was &fsuccessfully &aunlinked&7 from JsonWatcher (" + watcher.getUuid() + ")");
                }
            }
            return null;
        });
    }

    /**
     * Watch.
     */
    public void watch() {
        try {
            JsonCache<String, JsonElement, File> cache = getCache();
            FileHandler.get(file).whenComplete((jsonfile, error) -> {
                var splitParentID = parentID.split(";");
                Map<JsonElement, File> fromCache;
                JsonElement potentialJson;
                if (parentID.contains(";")) {
                    fromCache = cache.getValuesByKey(splitParentID[0]).join();
                    potentialJson = ((JsonElement) fromCache.keySet().toArray()[0]).getAsJsonObject().get(splitParentID[1]);
                } else {
                    fromCache = cache.getValuesByKey(id).join();
                    potentialJson = ((JsonElement) fromCache.keySet().toArray()[0]);
                }

                WatchKey key;
                while ((key = watchService.poll()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().equals(file.toPath().getFileName())) {
                            if (!Objects.equals(potentialJson.toString(), jsonfile.toString())) {
                                ConcurrentHashMap<JsonElement, File> map = new ConcurrentHashMap<>(Map.of(jsonfile, file));
                                this.event.setJson(jsonfile);
                                cache.replace(id, map);
                                if (PROJECT_DEBUG)
                                    watcherLog(String.format("File Modified: %s, Watcher ID: %s", file, uuid));
                            } else {
                                if (PROJECT_DEBUG)
                                    watcherLog("Is cached!  : " + potentialJson + "--> : " + jsonfile);
                            }
                            break;
                        }
                    }
                    this.event.callEvent();
                    key.reset();
                }
            });
        } catch (Exception e) {
            watcherLog(String.format("An error occurred while watching file: %s, exception: %s", file, e));
        }
    }

    /**
     * Is cancelled boolean.
     *
     * @return the boolean
     */
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * Sets cancelled.
     *
     * @param cancelled the cancelled
     */
    public void setCancelled(boolean cancelled) {
        future.cancel(cancelled);
    }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public boolean isActive() {
        return !future.isCancelled();
    }

    /**
     * Is done boolean.
     *
     * @return the boolean
     */
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets config.
     *
     * @return the config
     */
    public static Config getConfig() {
        return JsonWatcher.config;
    }

    /**
     * Unregister all.
     */
    public static void unregisterAll() {
        try {
            watcherLog("Trying to unregister all watchers!");
            watcherCache.forEach((file, v_) -> unregister(file));
        } catch (Exception e) {
            watcherLog("Unregistering all watchers &cFailed!");
        } finally {
            watcherLog("Unregistering all watchers was &asuccessfully!");
        }
    }
}
