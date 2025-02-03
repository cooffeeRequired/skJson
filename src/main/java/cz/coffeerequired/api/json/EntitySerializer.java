package cz.coffeerequired.api.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class EntitySerializer implements JsonSerializer<Entity>, JsonDeserializer<Entity> {

    @Override
    public JsonElement serialize(Entity src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        // Uložíme UUID entity
        obj.addProperty("uuid", src.getUniqueId().toString());
        // Uložíme název světa, kde se entita nachází
        obj.addProperty("world", src.getWorld().getName());
        // Uložíme typ entity
        obj.addProperty("type", src.getType().toString());

        // Uložíme pozici entity
        Location loc = src.getLocation();
        obj.addProperty("x", loc.getX());
        obj.addProperty("y", loc.getY());
        obj.addProperty("z", loc.getZ());
        // Uložíme natočení entity (yaw a pitch)
        obj.addProperty("yaw", loc.getYaw());
        obj.addProperty("pitch", loc.getPitch());

        // Uložíme jméno entity – pokud má custom name, uložíme jej;
        // pokud jde o hráče, můžeme uložit jeho jméno (pozn.: hráčovo jméno se zpravidla nemění)
        if (src.getCustomName() != null) {
            obj.addProperty("customName", src.getCustomName());
        } else if (src instanceof org.bukkit.entity.Player) {
            obj.addProperty("name", ((org.bukkit.entity.Player) src).getName());
        }

        // Serializace persistent data – předpokládáme, že hodnoty jsou typu String
        if (src.getPersistentDataContainer() != null && !src.getPersistentDataContainer().getKeys().isEmpty()) {
            JsonObject persistentDataObj = new JsonObject();
            for (NamespacedKey key : src.getPersistentDataContainer().getKeys()) {
                // Zde předpokládáme, že data jsou uložena jako String; v případě jiných typů je třeba přidat podporu
                String value = src.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                if (value != null) {
                    // Uložíme klíč ve formátu "namespace:key"
                    persistentDataObj.addProperty(key.getNamespace() + ":" + key.getKey(), value);
                }
            }
            obj.add("persistentData", persistentDataObj);
        }

        return obj;
    }

    @Override
    public Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        // Načteme UUID entity
        String uuidStr = obj.get("uuid").getAsString();
        UUID uuid = UUID.fromString(uuidStr);

        // Načteme název světa
        String worldName = obj.get("world").getAsString();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new JsonParseException("Svět " + worldName + " nebyl nalezen!");
        }

        // Pokusíme se získat entitu podle UUID
        Entity entity = world.getEntity(uuid);
        if (entity == null) {
            throw new JsonParseException("Entita s UUID " + uuid + " nebyla ve světě " + worldName + " nalezena!");
        }
        if (obj.has("customName")) {
            entity.setCustomName(obj.get("customName").getAsString());
        }
        if (obj.has("persistentData")) {
            JsonObject persistentDataObj = obj.getAsJsonObject("persistentData");
            for (Map.Entry<String, JsonElement> entry : persistentDataObj.entrySet()) {
                String keyStr = entry.getKey(); // formát "namespace:key"
                String value = entry.getValue().getAsString();
                String[] parts = keyStr.split(":");
                if (parts.length == 2) {
                    String namespace = parts[0];
                    String keyName = parts[1];
                    Plugin plugin = Bukkit.getPluginManager().getPlugin(namespace);
                    if (plugin != null) {
                        NamespacedKey key = new NamespacedKey(plugin, keyName);
                        entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
                    }
                }
            }
        }

        return entity;
    }
}

