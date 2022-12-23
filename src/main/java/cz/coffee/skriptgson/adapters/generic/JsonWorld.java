package cz.coffee.skriptgson.adapters.generic;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static cz.coffee.skriptgson.utils.GsonUtils.check;

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
        if (json instanceof JsonObject object) {
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
        if (check(json, World.class.getSimpleName(), GsonUtils.Type.KEY)) {
            return World.class;
        }
        return null;
    }
}
