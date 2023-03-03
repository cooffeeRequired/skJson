package cz.coffee.core.cache;

import com.google.gson.JsonElement;
import cz.coffee.core.annotation.Used;

import java.io.File;
import java.util.*;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */

@Used
public class Cache {

    private final static TreeMap<String, CachePackage<JsonElement, File>> map;
    private final static HashMap<String, CachePackage.HotLink> hotMap;

    static {
        map = new TreeMap<>();
        hotMap = new HashMap<>();
    }

    public static void addToHot(String identifier, JsonElement json, UUID uuid) {
        if (uuid != null) {
            if (!hotMap.isEmpty()) {
                hotMap.forEach((str, hot) -> {
                    if (hot.getUuid() != uuid) {
                        CachePackage.HotLink hotLink = new CachePackage.HotLink(json, uuid);
                        hotMap.put(identifier, hotLink);
                    }
                });
            } else {
                CachePackage.HotLink hotLink = new CachePackage.HotLink(json, uuid);
                hotMap.put(identifier, hotLink);
            }
        } else {
            CachePackage.HotLink hotLink = new CachePackage.HotLink(json, null);
            hotMap.put(identifier, hotLink);
        }
    }

    public static void addTo(String identifier, JsonElement json, File file) {
        if (!map.containsKey(identifier)) {
            CachePackage<JsonElement, File> ch = new CachePackage<>(json, file);
            map.put(identifier, ch);
        }
    }

    public static void addTo(String identifier, JsonElement json, String file) {
        addTo(identifier, json, new File(file));
    }

    public static TreeMap<String, CachePackage<JsonElement, File>> getAll() {
        return map;
    }

    public static HashMap<String, CachePackage.HotLink> getHotAll() {
        return hotMap;
    }

    public static void remove(String identifier) {
        map.remove(identifier);
    }

    public static void hotRemove(String identifier) {
        hotMap.remove(identifier);
    }

    public static CachePackage<JsonElement, File> getPackage(String identifier) {
        if (map.containsKey(identifier)) {
            return map.get(identifier);
        }
        return null;
    }

    public static CachePackage.HotLink getHotPackage(String identifier) {
        if (hotMap.containsKey(identifier)) {
            return hotMap.get(identifier);
        }
        return null;
    }

    public static CachePackage.HotLink getHotPackage(UUID uuid, String identifier) {
        if (uuid != null) {
            for (Map.Entry<String, CachePackage.HotLink> map : hotMap.entrySet()) {
                if (Objects.equals(map.getValue().getUuid(), uuid) && Objects.equals(identifier, map.getKey())) {
                    return map.getValue();
                }
            }
        }
        return null;
    }


    public static boolean contains(String identifier) {
        return map.containsKey(identifier);
    }

    public static boolean hotContains(String identifier) {
        return hotMap.containsKey(identifier);
    }

    public static boolean isEmpty() {
        return map.isEmpty();
    }

    public static int size() {
        return map.size();
    }
}
