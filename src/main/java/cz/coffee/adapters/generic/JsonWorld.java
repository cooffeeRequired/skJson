/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.adapters.generic;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.utils.Type;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static cz.coffee.utils.json.JsonUtils.check;

public class JsonWorld implements JsonGenericAdapter<World> {

    @Override
    public @NotNull JsonElement toJson(World object) {
        HashMap<String, Object> serializedWorld = new HashMap<>();
        serializedWorld.put(GSON_GENERIC_ADAPTER_KEY, object.getClass().getSimpleName());
        serializedWorld.put("world-name", object.getName());
        serializedWorld.put("world-difficulty", object.getDifficulty());
        serializedWorld.put("world-border", object.getWorldBorder().getSize());
        return new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization().create().toJsonTree(serializedWorld);
    }

    @Override
    public World fromJson(JsonElement json) {
        World world = null;
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.has(GSON_GENERIC_ADAPTER_KEY)) {
                if (object.get(GSON_GENERIC_ADAPTER_KEY).getAsString().contains(World.class.getSimpleName())) {
                    world = Bukkit.getWorld(object.get("world-name").getAsString());
                    if (world == null) return null;

                    Difficulty difficulty = Difficulty.valueOf(object.get("world-difficulty").getAsString());
                    WorldBorder worldBorder = world.getWorldBorder();
                    worldBorder.setSize(object.get("world-border").getAsDouble());
                    world.setDifficulty(difficulty);
                }
            }
        }
        return world;
    }

    @Override
    public Class<? extends World> typeOf(JsonElement json) {
        if (check(json, World.class.getSimpleName(), Type.KEY)) {
            return World.class;
        }
        return null;
    }
}
