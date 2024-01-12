package cz.coffee.skjson.api.Cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JsonCache<K, V, F> extends ConcurrentHashMap<K, ConcurrentHashMap<V, F>> {

    public void addValue(K key, V innerKey, F value) {
        CompletableFuture.runAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = getOrDefault(key, new ConcurrentHashMap<>());
            innerMap.put(innerKey, value);
            put(key, innerMap);
        });
    }

    public CompletableFuture<Map<V, F>> getValuesByKey(K key) {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = get(key);
            if (innerMap != null) {
                return innerMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }
            return null;
        });
    }
}
