package cz.coffeerequired.api.nbts;

import com.google.gson.JsonElement;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache for NBT → JSON conversions to avoid repeated deep walks on hot paths.
 */
public final class NBTJsonCache {

    private static final int DEFAULT_LIMIT = 256;
    private static volatile int cacheLimit = DEFAULT_LIMIT;

    private static final LinkedHashMap<Integer, JsonElement> CACHE = new LinkedHashMap<>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, JsonElement> eldest) {
            return size() > cacheLimit;
        }
    };

    private NBTJsonCache() {
    }

    public static void configure(int limit) {
        cacheLimit = Math.max(32, limit);
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    public static JsonElement get(ReadableNBT compound) {
        if (compound == null) {
            return null;
        }
        int key = compoundHash(compound);
        synchronized (CACHE) {
            return CACHE.get(key);
        }
    }

    public static void put(ReadableNBT compound, JsonElement json) {
        if (compound == null || json == null) {
            return;
        }
        synchronized (CACHE) {
            CACHE.put(compoundHash(compound), json);
        }
    }

    private static int compoundHash(ReadableNBT compound) {
        return compound.hashCode();
    }
}
