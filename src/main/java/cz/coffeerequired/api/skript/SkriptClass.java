package cz.coffeerequired.api.skript;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.variables.SerializedVariable;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.coffeerequired.api.json.SafeTypeAdapterFactory;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.Base64;

@Getter
public class SkriptClass {

	private final String type;
	private final String data;

	public SkriptClass(String type, byte[] data) {
		this.type = type;
		this.data = Base64.getEncoder().encodeToString(data);
	}

	public SkriptClass(Object value) {
		SerializedVariable.Value val = Classes.serialize(value);
        assert val != null;
        this.type = val.type;
		this.data = Base64.getEncoder().encodeToString(val.data);
	}

    public Object deserialize() {
		return SkriptClass.deserialize(this.type, this.data);
	}

	public static Object deserialize(String type, String data) {
		return Classes.deserialize(type, Base64.getDecoder().decode(data));
	}

	private static final TypeAdapter<Reference<?>> REFERENCE_TYPE_ADAPTER = new TypeAdapter<>() {
		@Override
		public void write(JsonWriter out, Reference<?> value) throws IOException {
			if (value == null) out.nullValue();
			Object obj;
			assert value != null;
			if ((obj = value.get()) != null) {
				gson.toJson(obj, obj.getClass(), out);
			}
		}

		@Override
		public Reference<?> read(JsonReader in) {
			throw new UnsupportedOperationException("Deserializing references is not supported");
		}
	};

	private static final ExclusionStrategy INACCESSIBLE_EXCLUSION = new ExclusionStrategy() {
		private final String[] EXCLUDED_PACKAGES = {
				"java.util.Optional",
				"java.lang.ref",
				"java.util.concurrent",
				"java.util.TimeZone",
				"java.lang.Thread",
				"java.lang.reflect",
				"sun.",
				"jdk.internal",
				"java.security",
				"javax.security",
				"java.util.logging",
				"java.util.regex.Pattern",
				"java.io",
				"java.nio",
				"java.net",
				"net.minecraft.server"
		};

		private boolean isExcluded(String className) {
			if (className == null) return false;
			for (String pkg : EXCLUDED_PACKAGES) {
				if (className.startsWith(pkg)) return true;
			}
			return false;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			try {
				return isExcluded(f.getDeclaringClass().getName());
			} catch (Exception e) {
				return true;
			}
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			try {
				return clazz != null && isExcluded(clazz.getName());
			} catch (Exception e) {
				return true;
			}
		}
	};
	private static final ExclusionStrategy REFERENCE_EXCLUSION = new ExclusionStrategy() {
		@Override
		public boolean shouldSkipField(FieldAttributes f) {
			return Reference.class.isAssignableFrom(f.getDeclaringClass());
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return Reference.class.isAssignableFrom(clazz);
		}
	};

	@Getter
	private static final Gson gson = new GsonBuilder()
			.serializeNulls()
			.enableComplexMapKeySerialization()
			.disableHtmlEscaping()
			.setStrictness(Strictness.LENIENT)
			.registerTypeAdapter(Date.class, new SkriptClassesConverter.DateAdapter())
			.registerTypeAdapter(Time.class, new SkriptClassesConverter.TimeAdapter())
			.registerTypeAdapter(Timespan.class, new SkriptClassesConverter.TimespanAdapter())
			.registerTypeAdapter(WeatherType.class, new SkriptClassesConverter.WeatherTypeAdapter())
			.registerTypeHierarchyAdapter(Inventory.class, new SkriptClassesConverter.InventoryAdapter())
			.registerTypeHierarchyAdapter(InventoryHolder.class, new SkriptClassesConverter.InventoryHolderAdapter())
			.registerTypeHierarchyAdapter(Slot.class, new SkriptClassesConverter.SlotAdapter())
			.registerTypeAdapter(Vector.class, new SkriptClassesConverter.VectorAdapter())
			.registerTypeHierarchyAdapter(ItemType.class, new SkriptClassesConverter.ItemTypeAdapter())
			.registerTypeAdapter(Location.class, new SkriptClassesConverter.LocationAdapter())
			.registerTypeHierarchyAdapter(BlockData.class, new SkriptClassesConverter.BlockDataAdapter())
			.registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
			.registerTypeAdapter(SkriptClass.class, new SkriptClassesConverter.SkriptClassAdapter())
			.registerTypeHierarchyAdapter(Player.class, new SkriptClassesConverter.PlayerAdapter())
			.registerTypeHierarchyAdapter(World.class, new SkriptClassesConverter.WorldAdapter())
			.registerTypeHierarchyAdapter(Block.class, new SkriptClassesConverter.BlockAdapter())
			.registerTypeHierarchyAdapter(Chunk.class, new SkriptClassesConverter.ChunkAdapter())
			.addSerializationExclusionStrategy(INACCESSIBLE_EXCLUSION)
			.addDeserializationExclusionStrategy(INACCESSIBLE_EXCLUSION)
			.addSerializationExclusionStrategy(REFERENCE_EXCLUSION)
			.addDeserializationExclusionStrategy(REFERENCE_EXCLUSION)
			.registerTypeAdapterFactory(new SafeTypeAdapterFactory())
			.registerTypeAdapterFactory(new TypeAdapterFactory() {
				@Override
				@SuppressWarnings("unchecked")
				public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
					if (Reference.class.isAssignableFrom(type.getRawType())) {
						return (TypeAdapter<T>) REFERENCE_TYPE_ADAPTER;
					}
					return null;
				}
			})
			.setPrettyPrinting()
			.create();
}
