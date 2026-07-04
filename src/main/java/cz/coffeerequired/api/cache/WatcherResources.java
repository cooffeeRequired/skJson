package cz.coffeerequired.api.cache;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shared executor and {@link WatchService} for all JSON file watchers.
 */
public final class WatcherResources {

    private static volatile ScheduledExecutorService executor;
    private static volatile WatchService watchService;

    private WatcherResources() {
    }

    public static ScheduledExecutorService executor() {
        if (executor == null) {
            synchronized (WatcherResources.class) {
                if (executor == null) {
                    int threads = Math.max(1, Api.Records.WATCHER_MAX_THREADS);
                    executor = Executors.newScheduledThreadPool(threads, r -> {
                        Thread thread = new Thread(r, "SkJson-Watcher");
                        thread.setDaemon(true);
                        return thread;
                    });
                    SkJson.debug("Shared watcher executor started with %s thread(s)", threads);
                }
            }
        }
        return executor;
    }

    public static WatchService watchService() {
        if (watchService == null) {
            synchronized (WatcherResources.class) {
                if (watchService == null) {
                    try {
                        watchService = FileSystems.getDefault().newWatchService();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create shared WatchService", e);
                    }
                }
            }
        }
        return watchService;
    }

    public static void shutdown() {
        synchronized (WatcherResources.class) {
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                executor = null;
            }
            if (watchService != null) {
                try {
                    watchService.close();
                } catch (IOException e) {
                    SkJson.exception(e, "Failed to close shared WatchService");
                }
                watchService = null;
            }
        }
    }
}
