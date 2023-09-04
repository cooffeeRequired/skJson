package storages;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class JsonStorage1<K, V, F> extends ConcurrentHashMap<K, ConcurrentHashMap<V, F>> {

    public void addValue(K key, V innerKey, F value) {
        CompletableFuture.runAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = getOrDefault(key, new ConcurrentHashMap<>());
            innerMap.put(innerKey, value);
            put(key, innerMap);
        });
    }

    public CompletableFuture<F> getValue(K key, V innerKey) {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = get(key);
            if (innerMap != null) {
                return innerMap.get(innerKey);
            }
            return null;
        });
    }

    public void removeValue(K key, V innerKey) {
        CompletableFuture.runAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = get(key);
            if (innerMap != null) {
                innerMap.remove(innerKey);
                if (innerMap.isEmpty()) {
                    remove(key);
                }
            }
        });
    }

    public void updateValue(K key, V innerKey, F value) {
        CompletableFuture.runAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = get(key);
            if (innerMap != null) {
                innerMap.put(innerKey, value);
            }
        });
    }

    public boolean containsKeyAndValue(K key, V innerKey) {
        ConcurrentHashMap<V, F> innerMap = get(key);
        if (innerMap != null) {
            return innerMap.containsKey(innerKey);
        }
        return false;
    }

    public boolean containsValue(K key, F value) {
        ConcurrentHashMap<V, F> innerMap = get(key);
        if (innerMap != null) {
            return innerMap.containsValue(value);
        }
        return false;
    }

    public CompletableFuture<V> getValueByFile(K key, File file) {
        return CompletableFuture.supplyAsync(() -> {
            ConcurrentHashMap<V, F> innerMap = get(key);
            if (innerMap != null) {
                for (Map.Entry<V, F> entry : innerMap.entrySet()) {
                    if (entry.getValue() instanceof File && entry.getValue().equals(file)) {
                        return entry.getKey();
                    }
                }
            }
            return null;
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
