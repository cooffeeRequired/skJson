package cz.coffeerequired.api.json;

import cz.coffeerequired.api.annotators.ExternalAPI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ExternalAPI
public class CachedStorage<S, J, F> extends ConcurrentHashMap<S, ConcurrentHashMap<J, F>> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void addValue(S id, J jsonElement, F file) {
        CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                computeIfAbsent(id, s -> new ConcurrentHashMap<>()).put(jsonElement, file);
            } finally {
                lock.writeLock().unlock();
            }
        });
    }

    public CompletableFuture<ConcurrentHashMap<J, F>> getValuesById(final S id) {
        return CompletableFuture.supplyAsync(() -> {
            lock.readLock().lock();
            try {
                return get(id);
            } finally {
                lock.readLock().unlock();
            }
        });
    }

    public void removeIfPresent(S id) {
        CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            try {
                if (containsKey(id)) {
                    remove(id);
                }
            } finally {
                lock.writeLock().unlock();
            }
        });
    }
}
