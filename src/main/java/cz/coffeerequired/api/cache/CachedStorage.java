package cz.coffeerequired.api.cache;

import com.google.gson.JsonElement;
import cz.coffeerequired.api.annotators.ExternalAPI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;



/**
 * <b>CacheStorage Representation</b><br />
 * {@link String} - is generic S, represent ID <br />
 * {@link com.google.gson.JsonElement} - is generic J, represent Json <br />
 * {@link java.io.File} - is generic F, represent File of JSON cached <br />
 */
@ExternalAPI
@SuppressWarnings({"unused", "WeakerAccess"})
public class CachedStorage<S, J, F> extends ConcurrentHashMap<S, CacheLink<J, F>> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void addValue(S id, J jsonElement, F file) {
        lock.writeLock().lock();
        try {
            computeIfAbsent(id, k -> new CacheLink<>(jsonElement, file));
        } finally {
            lock.writeLock().unlock();
        }
    }


    public void addValue(S id, CacheLink<J, F> cacheLink) {
        lock.writeLock().lock();
        try {
            computeIfAbsent(id, k -> cacheLink);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public JsonElement[] getJsons() {
        return values()
                .stream()
                .map(CacheLink::jsonElement)
                .map(json -> (JsonElement) json)
                .toArray(JsonElement[]::new);
    }

    public CompletableFuture<CacheLink<J, F>> getValuesById(final S id) {
        return CompletableFuture.supplyAsync(() -> {
            lock.readLock().lock();
            try {
                return get(id);
            } finally {
                lock.readLock().unlock();
            }
        });
    }


    public void removeIfPresent(final S id) {
        CompletableFuture.runAsync(() -> {
           lock.readLock().lock();
           try {
               if (containsKey(id)) remove(id);
           } finally {
               lock.readLock().unlock();
           }
        });
    }

    public void free() {
        lock.writeLock().lock();
        try {
            clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
