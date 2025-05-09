package cz.coffeerequired.api.cache;

import com.google.gson.JsonElement;
import cz.coffeerequired.api.annotators.ExternalAPI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * <b>CacheStorage Representation</b><br />
 * {@link String} - is generic S, represent ID <br />
 * {@link com.google.gson.JsonElement} - is generic J, represent Json <br />
 * {@link java.io.File} - is generic F, represent File of JSON cached <br />
 */
@ExternalAPI
public final class CachedStorage<S, J, F> {
    private final Map<S, CacheLink<J, F>> map = new ConcurrentHashMap<>();

    public void addValue(S id, J jsonElement, F file) {
        addValue(id, new CacheLink<>(jsonElement, file));
    }


    public void addValue(S id, CacheLink<J, F> cacheLink) {
        map.computeIfAbsent(id, k -> cacheLink);
    }

    public CacheLink<J, F> getValuesById(S id) {
        return map.get(id);
    }


    public void removeIfPresent(S id) {
        map.remove(id);
    }

    public void free() {
        map.clear();
    }

    
    public JsonElement[] getJsons() {
        return this.map.values()
                .stream()
                .map(CacheLink::jsonElement)
                .map(json -> (JsonElement) json)
                .toArray(JsonElement[]::new);
    }

    public void replace(S id, CacheLink<J, F> cacheLink) {
        map.replace(id, cacheLink);
    }

    public boolean containsKey(S id) {
        return map.containsKey(id);
    }

    public void forEach(BiConsumer<? super S, ? super CacheLink<J, F>> action) {
        map.forEach(action);
    }
}
