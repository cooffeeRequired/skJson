package cz.coffeerequired.api.json;

import cz.coffeerequired.api.annotators.ExternalAPI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ExternalAPI
public class JsonCache<S, J, F> extends ConcurrentHashMap<S, ConcurrentHashMap<J, F>> {

    public void addValue(S id, J jsonElement, F file) {
        CompletableFuture.runAsync(() -> {
           ConcurrentHashMap<J, F> inner = getOrDefault(id, new ConcurrentHashMap<>());
           inner.put(jsonElement, file);
           put(id, inner);
        });
    }

    public CompletableFuture<ConcurrentHashMap<J, F>> getValuesById(final S id) {
        return CompletableFuture.supplyAsync(() -> get(id));
    }

}
