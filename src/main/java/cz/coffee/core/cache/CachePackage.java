package cz.coffee.core.cache;

import cz.coffee.core.annotation.Used;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
public class CachePackage<JsonElement, File> {

    protected JsonElement json;
    protected File file;

    public CachePackage(JsonElement json, File file) {
        this.file = file;
        this.json = json;
    }

    @Used
    public JsonElement getJson() {
        return json;
    }

    @Used
    public File getFile() {
        return file;
    }


    public static class HotLink {
        protected com.google.gson.JsonElement json;
        protected UUID uuid;

        public HotLink(@NotNull com.google.gson.JsonElement json, UUID uuid) {
            this.json = json;
            if (uuid == null) {
                this.uuid = UUID.randomUUID();
            } else {
                this.uuid = uuid;
            }
        }

        @Used
        public com.google.gson.JsonElement getJson() {
            return json;
        }

        @Used
        public UUID getUuid() {
            return uuid;
        }
    }

}
