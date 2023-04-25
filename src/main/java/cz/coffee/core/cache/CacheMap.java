package cz.coffee.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMap<T, K, V> extends ConcurrentHashMap<T, Map<K,V>> {

}
