package cz.coffee.skjson.api.Cache;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: neděle (03.09.2023)
 */
public class JsonStorage<K, V, F> {
    private final ConcurrentHashMap<K, ConcurrentHashMap<V, F>> cache = new ConcurrentHashMap<>();

    private ConcurrentHashMap<V, F> getInnerMap(K key) {
        return cache.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
    }

    public void setValue(K key, V innerKey, F value) {
        getInnerMap(key).put(innerKey, value);
    }

    public F getValue(K key, V innerKey) {
        return getInnerMap(key).get(innerKey);
    }

    public void removeValue(K key, V innerKey) {
        synchronized (getInnerMap(key)) {
            ConcurrentHashMap<V, F> innerMap = getInnerMap(key);
            innerMap.remove(innerKey);
            if (innerMap.isEmpty()) {
                cache.remove(key);
            }
        }
    }

    public boolean containsKeyAndValue(K key, V innerKey) {
        return getInnerMap(key).containsKey(innerKey);
    }

    public V getValueByFile(K key, File file) {
        if (file != null) {
            ConcurrentHashMap<V, F> innerMap = getInnerMap(key);
            for (Map.Entry<V, F> entry : innerMap.entrySet()) {
                if (file.equals(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public Map<V, F> getValuesByKey(K key) {
        ConcurrentHashMap<V, F> innerMap = getInnerMap(key);
        return innerMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
