package cz.coffeerequired.api.json;

import cz.coffeerequired.api.annotators.ExternalAPI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ExternalAPI
public class JsonCache<S, J, F> extends ConcurrentHashMap<S, ConcurrentHashMap<J, F>> {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    public synchronized void addValue(S id, J jsonElement, F file) {
        lock.writeLock().lock();
        CompletableFuture.runAsync(() -> {
           ConcurrentHashMap<J, F> inner = getOrDefault(id, new ConcurrentHashMap<>());
           inner.put(jsonElement, file);
           put(id, inner);
        });
    }

    public synchronized CompletableFuture<ConcurrentHashMap<J, F>> getValuesById(final S id) {
        lock.readLock().lock();
        return CompletableFuture.supplyAsync(() -> get(id));
    }

}
