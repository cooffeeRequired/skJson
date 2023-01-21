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
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.adapters.generic;

import ch.njol.skript.doc.Since;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.utils.Type;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.utils.json.JsonUtils.check;

@Since("2.5")


public class JsonChunk implements JsonGenericAdapter<Chunk> {
    @Override
    public @NotNull JsonElement toJson(Chunk object) {
        JsonObject chunkObject = new JsonObject();
        chunkObject.add("chunk-info", new JsonObject());
        chunkObject.addProperty("??", Chunk.class.getName());
        chunkObject.getAsJsonObject("chunk-info").addProperty("X", object.getX());
        chunkObject.getAsJsonObject("chunk-info").addProperty("Z", object.getZ());
        chunkObject.getAsJsonObject("chunk-info").addProperty("World", object.getWorld().getName());
        return chunkObject;
    }

    @Override
    public Chunk fromJson(JsonElement json) {
        int X = json.getAsJsonObject().getAsJsonObject("chunk-info").get("X").getAsInt();
        int Z = json.getAsJsonObject().getAsJsonObject("chunk-info").get("Z").getAsInt();
        String world = json.getAsJsonObject().getAsJsonObject("chunk-info").get("World").getAsString();
        World w = Bukkit.getWorld(world);
        if (w != null) {
            return w.getChunkAt(X, Z);
        }
        return null;
    }

    @Override
    public Class<? extends Chunk> typeOf(JsonElement json) {
        if (check(json, Chunk.class.getSimpleName(), Type.KEY)) {
            return Chunk.class;
        }
        return null;
    }
}
