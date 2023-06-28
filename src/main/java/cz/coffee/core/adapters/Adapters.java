package cz.coffee.core.adapters;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import cz.coffee.SkJson;
import de.tr7zw.nbtapi.NBTContainer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cz.coffee.core.utils.AdapterUtils.parseItem;
import static cz.coffee.core.utils.Util.GSON_ADAPTER;
import static org.bukkit.Bukkit.getWorld;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

@SuppressWarnings("ALL")
public abstract class Adapters {
    public final static Adapter<World> WorldAdapter = new Adapter<>() {
        @Override
        public @NotNull JsonElement toJson(World source) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            jsonObject.addProperty("name", source.getName());
            return !jsonObject.isEmpty() ? jsonObject : JsonNull.INSTANCE;
        }

        @Override
        public World fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                World world;
                if ((world = getWorld(json.get("name").getAsString())) != null) return world;
            }
            return null;
        }
    };
    public final static Adapter<Chunk> ChunkAdapter = new Adapter<>() {
        @Override
        public @NotNull JsonElement toJson(Chunk source) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            jsonObject.addProperty("world", source.getWorld().getName());
            jsonObject.addProperty("x", source.getX());
            jsonObject.addProperty("z", source.getZ());
            return !jsonObject.isEmpty() ? jsonObject : JsonNull.INSTANCE;
        }

        @Override
        public Chunk fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                World world;
                if ((world = getWorld(json.get("world").getAsString())) != null) {
                    return world.getChunkAt(json.get("x").getAsInt(), json.get("z").getAsInt());
                }
            }
            return null;
        }
    };
    public final static Adapter<NBTContainer> NBTContainerAdapter = new Adapter<>() {
        @Override
        public @NotNull JsonElement toJson(NBTContainer source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            o.addProperty("nbt", source.toString());
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public NBTContainer fromJson(JsonObject json) {
            return new NBTContainer(json.get("nbt").getAsString());
        }
    };

    public static class TypeAdapter {
        public static class BukkitAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {
            final java.lang.reflect.Type objectStringMapType = new TypeToken<Map<String, Object>>() {
            }.getType();

            @Override
            public ConfigurationSerializable deserialize(
                    JsonElement json,
                    java.lang.reflect.Type typeOfT,
                    JsonDeserializationContext context) throws JsonParseException {


                final Map<String, Object> map = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                    final JsonElement value = entry.getValue();
                    final String name = entry.getKey();

                    if (value.isJsonObject() && value.getAsJsonObject().has(SERIALIZED_TYPE_KEY)) {
                        map.put(name, this.deserialize(value, value.getClass(), context));
                    } else {
                        map.put(name, context.deserialize(value, Object.class));
                    }
                }

                return ConfigurationSerialization.deserializeObject(map);
            }

            @Override
            public JsonElement serialize(
                    ConfigurationSerializable src,
                    java.lang.reflect.Type typeOfSrc,
                    JsonSerializationContext context) {
                final Map<String, Object> map = new LinkedHashMap<>();
                map.put(SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
                map.putAll(src.serialize());
                return context.serialize(map, objectStringMapType);
            }
        }
    }    @SuppressWarnings("deprecation")
    public final static Adapter<Block> BlockAdapter = new Adapter<>() {
        @Override
        public @NotNull JsonElement toJson(Block source) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            jsonObject.addProperty("type", source.getType().name());
            jsonObject.addProperty("data", source.getData());
            jsonObject.add("location", parseItem(source.getLocation(), null, null));
            jsonObject.addProperty("world", source.getWorld().getName());
            return !jsonObject.isEmpty() ? jsonObject : JsonNull.INSTANCE;
        }

        @Override
        public Block fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                World world;
                if ((world = getWorld(json.get("world").getAsString())) != null) {
                    Block block = world.getBlockAt(GSON_ADAPTER.fromJson(json.get("location"), Location.class));
                    block.setType(Material.valueOf(json.get("type").getAsString()));
                    return block;
                }
            }
            return null;
        }
    };



    @SuppressWarnings("deprecation")
    public final static Adapter<ItemStack> ItemStackAdapter = new Adapter<>() {
        private ItemStack setEnchants(final ItemStack i, final JsonObject META) {
            final String enchants = "enchants";
            if (i == null) return null;
            if (META.has(enchants)) {
                final Set<Map.Entry<String, JsonElement>> jsonEnchants = META.getAsJsonObject(enchants).entrySet();
                for (Map.Entry<String, JsonElement> mapOfEnchantments : jsonEnchants) {
                    i.addUnsafeEnchantment(
                            Objects.requireNonNull(Enchantment.getByName(mapOfEnchantments.getKey().toUpperCase())),
                            mapOfEnchantments.getValue().getAsInt()
                    );
                }
            }
            return i;
        }

        @Override
        public @NotNull JsonElement toJson(ItemStack source) {
            if (source.getItemMeta() == null) {
                return GSON_ADAPTER.toJsonTree(source, ItemStack.class);
            }
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            if (source.getItemMeta().getClass().getSimpleName().equals("CraftMetaTropicalFishBucket")) {
                final JsonObject fishMeta = new JsonObject();
                final JsonObject metaJson = GSON_ADAPTER.toJsonTree(source, ItemStack.class).getAsJsonObject();
                if (metaJson.has("meta")) {
                    TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) source.getItemMeta();
                    fishMeta.addProperty("pattern", tropicalFishBucketMeta.getPattern().name());
                    fishMeta.addProperty("pattern-color", tropicalFishBucketMeta.getPatternColor().name());
                    fishMeta.addProperty("body-color", tropicalFishBucketMeta.getBodyColor().name());
                    metaJson.add("fish-model", fishMeta);
                    return metaJson;
                }
            } else if (source.hasItemMeta() && source.getItemMeta().hasCustomModelData()) {
                final JsonObject metaJson = GSON_ADAPTER.toJsonTree(source, ItemStack.class).getAsJsonObject();
                metaJson.getAsJsonObject("meta").addProperty("custom-model-data", source.getItemMeta().getCustomModelData());
                return metaJson;
            } else {
                return GSON_ADAPTER.toJsonTree(source, ItemStack.class);
            }
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public ItemStack fromJson(JsonObject json) {
            if (json.has("meta")) {
                ItemMeta im = ItemMetaAdapter.fromJson(json);
                final JsonObject meta = json.getAsJsonObject("meta");
                json.remove("meta");
                ItemStack stack = GSON_ADAPTER.fromJson(json, ItemStack.class);
                stack.setItemMeta(im);
                stack = setEnchants(stack, meta);
                return stack;
            }
            return GSON_ADAPTER.fromJson(json, ItemStack.class);
        }
    };

    public final static Adapter<ItemMeta> ItemMetaAdapter = new Adapter<>() {

        final HashMap<Integer, String> metaTypes = new HashMap<>() {{
            put(0, "BANNER");
            put(1, "AXOLOTL_BUCKET");
            put(2, "BUNDLE");
            put(3, "COMPASS");
            put(4, "CROSSBOW");
            put(5, "DAMAGEABLE");
            put(6, "FIREWORK");
            put(7, "Map");
            put(8, "SUSPICIOUS_STEW");
            put(9, "TropicalFishBucketMeta");
        }};

        private BannerMeta bannerMeta(final JsonObject rawMeta) {
            final String _BANNER_PATTERNS = "patterns";
            if (rawMeta.has(_BANNER_PATTERNS)) {
                JsonArray _PATTERN_ARRAY = rawMeta.getAsJsonArray(_BANNER_PATTERNS);
                rawMeta.remove(_BANNER_PATTERNS);
                ArrayList<Pattern> _PATTERNS = new ArrayList<>();

                for (JsonElement pattern : _PATTERN_ARRAY) {
                    _PATTERNS.add(new org.bukkit.block.banner.Pattern(
                            DyeColor.valueOf(pattern.getAsJsonObject().get("color").getAsString()),
                            Objects.requireNonNull(PatternType.getByIdentifier(pattern.getAsJsonObject().get("pattern").getAsString()))
                    ));
                }
                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                BannerMeta bannerMeta = (BannerMeta) meta;
                bannerMeta.setPatterns(_PATTERNS);
                return bannerMeta;
            }

            return null;
        }

        private AxolotlBucketMeta axolotlMeta(final JsonObject rawMeta) {
            final String _AXOLOTL_B_VARIANT = "axolotl-variant";
            final HashMap<Integer, Axolotl.Variant> axolotlVariants = new HashMap<>() {{
                put(0, Axolotl.Variant.LUCY);
                put(1, Axolotl.Variant.WILD);
                put(2, Axolotl.Variant.GOLD);
                put(3, Axolotl.Variant.CYAN);
                put(4, Axolotl.Variant.BLUE);
            }};
            if (rawMeta.has(_AXOLOTL_B_VARIANT)) {
                Integer variant = rawMeta.get(_AXOLOTL_B_VARIANT).getAsInt();
                rawMeta.remove(_AXOLOTL_B_VARIANT);

                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                AxolotlBucketMeta Axolotlmeta = ((AxolotlBucketMeta) meta);
                Axolotlmeta.setVariant(axolotlVariants.get(variant));
                return Axolotlmeta;
            }
            return null;
        }

        @SuppressWarnings("UnstableApiUsage")
        private BundleMeta bundleMeta(final JsonObject rawMeta) {
            final String _BUNDLE_ITEMS = "items";
            if (rawMeta.has(_BUNDLE_ITEMS)) {
                final JsonArray JSON_ITEMS_ = rawMeta.getAsJsonArray(_BUNDLE_ITEMS);
                rawMeta.remove(_BUNDLE_ITEMS);
                final ArrayList<ItemStack> items = new ArrayList<>();
                JSON_ITEMS_.forEach(item -> items.add(ItemStackAdapter.fromJson(item.getAsJsonObject())));
                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                BundleMeta bundleMeta = ((BundleMeta) meta);
                bundleMeta.setItems(items);
                return bundleMeta;
            }
            return null;
        }

        private CompassMeta compassMeta(final JsonObject rawMeta) {
            final String _COMPASS_P_WORLD = "LodestonePosWorld";
            final String _COMPASS_P_X = "LodestonePosX";
            final String _COMPASS_P_Y = "LodestonePosY";
            final String _COMPASS_P_Z = "LodestonePosZ";
            final String _COMPASS_P_TRACKED = "LodestoneTracked";

            if (rawMeta.has(_COMPASS_P_WORLD) && rawMeta.has(_COMPASS_P_TRACKED)) {
                Location loc = new Location(
                        SkJson.getInstance().getServer().getWorlds().get(0),
                        rawMeta.get(_COMPASS_P_X).getAsDouble(),
                        rawMeta.get(_COMPASS_P_Y).getAsDouble(),
                        rawMeta.get(_COMPASS_P_Z).getAsDouble()
                );
                boolean tracked = rawMeta.get(_COMPASS_P_TRACKED).getAsBoolean();
                rawMeta.remove(_COMPASS_P_X);
                rawMeta.remove(_COMPASS_P_Y);
                rawMeta.remove(_COMPASS_P_Z);
                rawMeta.remove(_COMPASS_P_WORLD);
                rawMeta.remove(_COMPASS_P_TRACKED);

                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                CompassMeta CompassMeta = ((CompassMeta) meta);
                CompassMeta.setLodestone(loc);
                CompassMeta.setLodestoneTracked(tracked);
                return CompassMeta;
            }
            return null;
        }

        private CrossbowMeta crossbowMeta(final JsonObject rawMeta) {
            final String _CROSSBOW_PROJECTILES = "charged-projectiles";
            if (rawMeta.has(_CROSSBOW_PROJECTILES)) {
                ArrayList<ItemStack> _PROJECTILES = new ArrayList<>();
                rawMeta.get(_CROSSBOW_PROJECTILES).getAsJsonArray().forEach(p -> _PROJECTILES.add(ItemStackAdapter.fromJson(p.getAsJsonObject())));
                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                CrossbowMeta CrossBowMeta = ((CrossbowMeta) meta);
                CrossBowMeta.setChargedProjectiles(_PROJECTILES);
                return CrossBowMeta;
            }
            return null;
        }

        private Damageable damageableMeta(final JsonObject rawMeta) {
            final String _DMG_DAMAGE = "Damage";
            if (rawMeta.has(_DMG_DAMAGE)) {
                int damage = rawMeta.get(_DMG_DAMAGE).getAsInt();
                rawMeta.remove(_DMG_DAMAGE);
                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                Damageable DamageableMeta = ((Damageable) meta);
                DamageableMeta.setDamage(damage);
                return DamageableMeta;
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private FireworkMeta fireworkMeta(final JsonObject rawMeta) {
            final String _FIREWORK_EFFECTS = "firework-effects";
            final String _FIREWORK_POWER = "power";

            if (rawMeta.has(_FIREWORK_EFFECTS)) {
                int power = rawMeta.get(_FIREWORK_POWER).getAsInt();
                JsonArray effects = rawMeta.getAsJsonArray(_FIREWORK_EFFECTS);
                rawMeta.remove(_FIREWORK_POWER);
                rawMeta.remove(_FIREWORK_EFFECTS);


                ArrayList<FireworkEffect> fireworkEffectList = new ArrayList<>();

                for (JsonElement effect : effects) {
                    ArrayList<Color> colorList = new ArrayList<>();
                    ArrayList<Color> fadeColorList = new ArrayList<>();
                    FireworkEffect.Type fType = null;
                    boolean fTrail = false;
                    boolean fFlicker = false;

                    for (Map.Entry<String, JsonElement> map : effect.getAsJsonObject().entrySet()) {
                        if (!map.getKey().equals("==")) {
                            if (map.getKey().equals("colors"))
                                map.getValue().getAsJsonArray().forEach(c -> colorList.add(Color.deserialize(GSON_ADAPTER.fromJson(c, HashMap.class))));
                            if (map.getKey().equals("type"))
                                fType = FireworkEffect.Type.valueOf(map.getValue().getAsString());
                            if (map.getKey().equals("flicker"))
                                fFlicker = map.getValue().getAsBoolean();
                            if (map.getKey().equals("trail"))
                                fTrail = map.getValue().getAsBoolean();
                            if (map.getKey().equals("fade-colors"))
                                map.getValue().getAsJsonArray().forEach(c -> fadeColorList.add(Color.deserialize(GSON_ADAPTER.fromJson(c, HashMap.class))));
                        }
                    }
                    assert fType != null;
                    FireworkEffect fireworkEffect = FireworkEffect.builder()
                            .with(fType)
                            .withColor(colorList)
                            .withFade(fadeColorList)
                            .trail(fTrail)
                            .flicker(fFlicker)
                            .build();

                    fireworkEffectList.add(fireworkEffect);
                }

                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                FireworkMeta FireworkMeta = ((FireworkMeta) meta);
                FireworkMeta.addEffects(fireworkEffectList);
                FireworkMeta.setPower(power);
                return FireworkMeta;
            }
            return null;
        }

        @SuppressWarnings("deprecation")
        private MapMeta mapMeta(final JsonObject rawMeta) {
            final String _MAP_ID = "map-id";

            if (rawMeta.has(_MAP_ID)) {
                int mapID = rawMeta.get(_MAP_ID).getAsInt();
                rawMeta.remove(_MAP_ID);

                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                MapMeta mapMeta = ((MapMeta) meta);
                mapMeta.setMapId(mapID);
                return mapMeta;
            }
            return null;
        }

        @SuppressWarnings("deprecation")
        private SuspiciousStewMeta suspiciousStewMeta(final JsonObject rawMeta) {
            final String _S_STEW_EFFECTS = "effects";

            if (rawMeta.has(_S_STEW_EFFECTS)) {
                JsonArray jsonEffects = rawMeta.getAsJsonArray(_S_STEW_EFFECTS);
                ArrayList<PotionEffect> potionEffects = new ArrayList<>();
                jsonEffects.forEach(e -> potionEffects.add(new PotionEffect(
                        Objects.requireNonNull(PotionEffectType.getById(e.getAsJsonObject().get("effect").getAsInt())),
                        e.getAsJsonObject().get("duration").getAsInt(),
                        e.getAsJsonObject().get("amplifier").getAsInt(),
                        e.getAsJsonObject().get("ambient").getAsBoolean(),
                        e.getAsJsonObject().get("has-particles").getAsBoolean(),
                        e.getAsJsonObject().get("has-icon").getAsBoolean()
                )));

                rawMeta.remove(_S_STEW_EFFECTS);

                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                SuspiciousStewMeta newMeta = ((SuspiciousStewMeta) meta);
                potionEffects.forEach(e -> newMeta.addCustomEffect(e, true));
                return newMeta;
            }
            return null;
        }

        @SuppressWarnings("deprecation")
        private TropicalFishBucketMeta tropicalFishBucketMeta(final JsonObject rawMeta) {
            final String _FISH_MODEL = "custom-fish";
            final String _FISH_PATTERN = "pattern";
            final String _FISH_PATTERN_COLOR = "pattern-color";
            final String _FISH_B_COLOR = "body-color";

            if (rawMeta.has(_FISH_MODEL)) {
                final JsonObject fishModel = rawMeta.get(_FISH_MODEL).getAsJsonObject();
                rawMeta.remove(_FISH_MODEL);
                rawMeta.remove("fish-variant");

                ItemMeta meta = GSON_ADAPTER.fromJson(rawMeta, ItemMeta.class);
                TropicalFishBucketMeta Newmeta = ((TropicalFishBucketMeta) meta);
                Newmeta.setPattern(TropicalFish.Pattern.valueOf(fishModel.get(_FISH_PATTERN).getAsString()));
                Newmeta.setPatternColor(DyeColor.legacyValueOf(fishModel.get(_FISH_PATTERN_COLOR).getAsString()));
                Newmeta.setBodyColor(DyeColor.legacyValueOf(fishModel.get(_FISH_B_COLOR).getAsString()));
                return Newmeta;
            }
            return null;
        }

        private void setModel(int model, ItemMeta meta) {
            if (model != -999) {
                meta.setCustomModelData(model);
            }
        }

        private void setModifiers(JsonObject JSON_META, final ItemMeta i) {
            final String _MODIFIERS = "attribute-modifiers";
            if (JSON_META.has(_MODIFIERS)) {
                final Set<Map.Entry<String, JsonElement>> _JSON_MODIFIERS = JSON_META.getAsJsonObject(_MODIFIERS).entrySet();
                for (Map.Entry<String, JsonElement> mapOfModifiers : _JSON_MODIFIERS) {
                    Attribute attr = Attribute.valueOf(mapOfModifiers.getKey().toUpperCase());
                    for (JsonElement modifier : mapOfModifiers.getValue().getAsJsonArray()) {
                        AttributeModifier attrModifier = GSON_ADAPTER.fromJson(modifier, AttributeModifier.class);
                        i.addAttributeModifier(attr, attrModifier);
                    }
                }
            }
        }

        @Override
        public @NotNull JsonElement toJson(ItemMeta source) {
            return JsonNull.INSTANCE;
        }

        @Override
        public ItemMeta fromJson(JsonObject json) {
            String META_ = "meta";
            JsonObject JsonMeta = json.getAsJsonObject(META_);
            ItemMeta meta;
            int CustomModelData = -999;

            if (JsonMeta.has("custom-model-data")) {
                CustomModelData = JsonMeta.get("custom-model-data").getAsInt();
                JsonMeta.remove("custom-model-data");
            }

            String metaType = "meta-type";

            if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(0))) {
                meta = bannerMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(1))) {
                meta = axolotlMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(2))) {
                meta = bundleMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(3))) {
                meta = compassMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(4))) {
                meta = crossbowMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(5))) {
                meta = damageableMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(6))) {
                meta = fireworkMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(7))) {
                meta = mapMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(8))) {
                meta = suspiciousStewMeta(JsonMeta);
            } else if (JsonMeta.get(metaType).getAsString().equals(metaTypes.get(9))) {
                meta = tropicalFishBucketMeta(JsonMeta);
            } else {
                meta = GSON_ADAPTER.fromJson(JsonMeta, ItemMeta.class);
                setModel(CustomModelData, meta);
                setModifiers(JsonMeta, meta);
                return meta;
            }
            setModel(CustomModelData, meta);
            setModifiers(JsonMeta, meta);
            return meta;
        }
    };

    public final static Adapter<Inventory> InventoryAdapter = new Adapter<>() {
        @Override
        public @NotNull JsonElement toJson(Inventory source) {
            final String sourceType = source.getType().name();
            String sourceTitle;
            String sourceHolder;
            if (source.getViewers().isEmpty()) {
                sourceHolder = "DEFAULT";
                sourceTitle = source.getType().getDefaultTitle();
            } else {
                sourceHolder = source.getViewers().get(0).getName();
                sourceTitle = source.getViewers().get(0).getOpenInventory().getTitle();
            }

            final JsonObject object = new JsonObject();
            final JsonObject jsonInventory = new JsonObject();
            object.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            for (ItemStack item : source.getContents()) {
                String slot = "Slot " + jsonInventory.size();
                jsonInventory.add(slot, (item != null ? ItemStackAdapter.toJson(item) : JsonNull.INSTANCE));
            }
            object.addProperty("title", sourceTitle);
            object.addProperty("rows", source.getSize() / 9);
            object.addProperty("size", source.getSize());
            object.addProperty("type", sourceType);
            object.addProperty("holder", sourceHolder);
            object.add("contents", jsonInventory);
            return object;
        }

        @Override
        public Inventory fromJson(JsonObject json) {
            final String jsonTitle = json.get("title").getAsString();
            final String jsonType = json.get("type").getAsString();
            final String jsonHolder = json.get("holder").getAsString();
            final int jsonSize = json.get("size").getAsInt();
            final ArrayList<ItemStack> items = new ArrayList<>();
            Inventory inventory;
            if (jsonHolder.equals("DEFAULT")) {
                inventory = Bukkit.createInventory(null, jsonSize, jsonTitle);
            } else {
                Player p = Bukkit.getPlayer(jsonHolder);
                inventory = Bukkit.createInventory(p, jsonSize, jsonTitle);
            }
            json.getAsJsonObject("contents").entrySet().forEach(entry -> {
                JsonElement jElement = entry.getValue();
                if (jElement == JsonNull.INSTANCE) {
                    items.add(new ItemStack(Material.AIR));
                } else {
                    items.add(ItemStackAdapter.fromJson(jElement.getAsJsonObject()));
                }
            });
            inventory.setContents(items.toArray(new ItemStack[0]));
            return inventory;
        }
    };


}
