package cz.coffee.skjson.api;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("World")
public class WorldAdapter implements ConfigurationSerializable {
    private final String worldName;
    public WorldAdapter(World world) {
        this.worldName = world.getName();
    }

    public static World deserialize(Map<String, Object> args) {
        return Bukkit.getWorld(args.get("name").toString());
    }
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", worldName);
        return map;
    }
}
