package cz.coffee.core.cache;

import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.core.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static cz.coffee.SkJson.*;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: pondělí (13.03.2023)
 */

public class JsonWatcher {
    private static Logger LOGGER;
    private final File file;
    private final String identifier;
    private static UUID uuid;
    private final ScheduledFuture<?> future;
    private static ScheduledExecutorService service;

    public static void init() {
        LOGGER = LoggerFactory.getLogger("AsyncJsonWatcher");
        service = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread(runnable, "AsyncJsonWatcher"));
        service.isShutdown();
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
            SkJson.console("JsonWatcher registered with id&a: "+uuid+"&f for file &7(&e"+file+"&7)");
        }
    }

    public static void unregister(File file) {
        JsonWatcher assignedWatcher = null;
        for (Map.Entry<File, JsonWatcher> entry : WATCHERS.entrySet()) {
            if (file.equals(entry.getKey())) {
                assignedWatcher = entry.getValue();break;
            }
        }
        if (assignedWatcher != null && assignedWatcher.isActive()) {
            assignedWatcher.setCancelled(true);
            if (assignedWatcher.isCancelled() && assignedWatcher.isDone()) {
                SkJson.console("File &7(&e"+file+"&7)&7 was &fsuccessfully &aunlinked&7 from JsonWatcher ("+uuid+")");
            }
        }
    }

    public static void shutdown() {
        service.shutdown();
    }

    public JsonWatcher (File fileInput, String identifier) {
        long interval = 1L;
        this.file = fileInput;
        this.identifier = identifier;
        future = service.scheduleAtFixedRate(this::watch, 0, interval, TimeUnit.SECONDS);
    }

    private final Map<File,JsonElement> cachedData = new ConcurrentHashMap<>();
    private long lastCheckedTimeStamp;
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

    public void setCancelled(boolean cancelled) {
        future.cancel(cancelled);
    }
    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isActive() {
        return !future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }
}