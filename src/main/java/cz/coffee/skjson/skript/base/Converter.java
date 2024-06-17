package cz.coffee.skjson.skript.base;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.nbts.NBTConvert;
import cz.coffee.skjson.parser.ParserUtil;
import net.kyori.adventure.text.Component;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.TropicalFish;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cz.coffee.skjson.parser.ParserUtil.GsonConverter;
import static org.bukkit.Bukkit.getWorld;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

/**
 * The type Converter.
 */
@SuppressWarnings("All")
public abstract class Converter {
    /**
     * The constant WebhookConventer.
     */

    public final static SimpleConverter<World> WorldConverter = new SimpleConverter<World>() {
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
    public final static SimpleConverter<ItemStack> ItemStackConverter = new SimpleConverter<ItemStack>() {

        private static ItemStack enchants(ItemStack itemStack, final JsonObject meta) {
            final String enchants = "enchants";
            if (itemStack == null) return new ItemStack(Material.AIR);
            if (meta.has(enchants)) {
                meta.getAsJsonObject(enchants).entrySet().forEach(entry -> itemStack.addUnsafeEnchantment(Enchantment.getByName(entry.getKey().toUpperCase()), entry.getValue().getAsInt()));
            }
            return itemStack;
        }

        @Override
        public @NotNull JsonElement toJson(ItemStack source) {
            if (source.getItemMeta() == null) {
                return GsonConverter.toJsonTree(source, ItemStack.class);
            } else {
                JsonObject o = new JsonObject();
                o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
                JsonElement i = GsonConverter.toJsonTree(source, ItemStack.class);
                return ParserUtil.parseNBTCustom(source, i);
            }
        }

        @Override
        public ItemStack fromJson(JsonObject json) {

            if (json.has("meta")) {
                JsonObject metaJson = json.getAsJsonObject("meta");
                JsonElement customTags = metaJson.remove("custom");
                ItemMeta im = ItemMetaConverter.fromJson(json);
                ItemStack stack = GsonConverter.fromJson(json, ItemStack.class);

                if (!customTags.isJsonNull()) {
                    stack = NBTConvert.parseFromJson(stack, customTags.getAsJsonObject(), false);
                }
                stack = enchants(stack, metaJson);
                stack.setItemMeta(im);
                return stack;
            }

            return GsonConverter.fromJson(json, ItemStack.class);
        }
    };
    public final static SimpleConverter<Chunk> ChunkConverter = new SimpleConverter<Chunk>() {

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
    public final static SimpleConverter<ItemMeta> ItemMetaConverter = new SimpleConverter<ItemMeta>() {

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

            put(10, "ArmorMeta");
            put(11, "ColorableArmorMeta");
            put(12, "MusicInstrumentMeta");
            put(13, "TropicalFishBucketMeta");
            put(14, "CustomModelData");
        }};

        private BannerMeta bannerMeta(final JsonObject rawMeta) {
            if (rawMeta.has("patterns")) {
                JsonArray patterns = rawMeta.remove("patterns").getAsJsonArray();
                ArrayList<Pattern> patternList = new ArrayList<>();

                for (JsonElement pattern : patterns) {
                    final PatternType type = PatternType.getByIdentifier(pattern.getAsJsonObject().get("pattern").getAsString());
                    final DyeColor color = DyeColor.valueOf(pattern.getAsJsonObject().get("color").getAsString());
                    if (type != null) patternList.add(new Pattern(color, type));
                }
                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                BannerMeta bannerMeta = (BannerMeta) meta;
                bannerMeta.setPatterns(patternList);
                return bannerMeta;
            }

            return null;
        }

        private AxolotlBucketMeta axolotlMeta(final JsonObject rawMeta) {
            final HashMap<Integer, Axolotl.Variant> axolotlVariants = new HashMap<>() {{
                put(0, Axolotl.Variant.LUCY);
                put(1, Axolotl.Variant.WILD);
                put(2, Axolotl.Variant.GOLD);
                put(3, Axolotl.Variant.CYAN);
                put(4, Axolotl.Variant.BLUE);
            }};

            if (rawMeta.has("axolotl-variant")) {
                Integer variant = rawMeta.remove("axolotl-variant").getAsInt();
                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                AxolotlBucketMeta Axolotlmeta = ((AxolotlBucketMeta) meta);
                Axolotlmeta.setVariant(axolotlVariants.get(variant));
                return Axolotlmeta;
            }
            return null;
        }

        private BundleMeta bundleMeta(final JsonObject rawMeta) {
            if (rawMeta.has("items")) {
                final JsonArray items = rawMeta.remove("items").getAsJsonArray();
                final ArrayList<ItemStack> itemsList = new ArrayList<>();
                items.forEach(item -> ItemStackConverter.fromJson(item.getAsJsonObject()));
                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                BundleMeta bundleMeta = ((BundleMeta) meta);
                bundleMeta.setItems(itemsList);
                return bundleMeta;
            }
            return null;
        }

        private CompassMeta compassMeta(final JsonObject rawMeta) {
            if (rawMeta.has("LodestonePosWorld") && rawMeta.has("LodestoneTracked")) {
                Location loc = new Location(
                    SkJson.getThisServer().getWorlds().get(0),
                    rawMeta.remove("LodestonePosX").getAsDouble(),
                    rawMeta.remove("LodestonePosY").getAsDouble(),
                    rawMeta.remove("LodestonePosZ").getAsDouble()
                );
                rawMeta.remove("LodestonePosWorld");
                boolean tracked = rawMeta.remove("LodestoneTracked").getAsBoolean();

                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                CompassMeta CompassMeta = ((CompassMeta) meta);
                CompassMeta.setLodestone(loc);
                CompassMeta.setLodestoneTracked(tracked);
                return CompassMeta;
            }
            return null;
        }

        private CrossbowMeta crossbowMeta(final JsonObject rawMeta) {
            if (rawMeta.has("charged-projectiles")) {
                ArrayList<ItemStack> projectiles = new ArrayList<>();
                rawMeta.remove("charged-projectiles").getAsJsonArray().forEach(projectil -> {
                    projectiles.add(ItemStackConverter.fromJson(projectil.getAsJsonObject()));
                });
                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                CrossbowMeta CrossBowMeta = ((CrossbowMeta) meta);
                CrossBowMeta.setChargedProjectiles(projectiles);
                return CrossBowMeta;
            }
            return null;
        }

        private Damageable damageableMeta(final JsonObject rawMeta) {
            if (rawMeta.has("Damage")) {
                int damage = rawMeta.remove("Damage").getAsInt();
                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                Damageable DamageableMeta = ((Damageable) meta);
                DamageableMeta.setDamage(damage);
                return DamageableMeta;
            }
            return null;
        }

        private FireworkMeta fireworkMeta(final JsonObject rawMeta) {
            final String _FIREWORK_EFFECTS = "firework-effects";

            if (rawMeta.has("firework-effects")) {
                int power = rawMeta.remove("power").getAsInt();
                JsonArray effects = rawMeta.remove("firework-effects").getAsJsonArray();
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
                                map.getValue().getAsJsonArray().forEach(c -> colorList.add(Color.deserialize(GsonConverter.fromJson(c, HashMap.class))));
                            if (map.getKey().equals("type"))
                                fType = FireworkEffect.Type.valueOf(map.getValue().getAsString());
                            if (map.getKey().equals("flicker"))
                                fFlicker = map.getValue().getAsBoolean();
                            if (map.getKey().equals("trail"))
                                fTrail = map.getValue().getAsBoolean();
                            if (map.getKey().equals("fade-colors"))
                                map.getValue().getAsJsonArray().forEach(c -> fadeColorList.add(Color.deserialize(GsonConverter.fromJson(c, HashMap.class))));
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

                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                FireworkMeta FireworkMeta = ((FireworkMeta) meta);
                FireworkMeta.addEffects(fireworkEffectList);
                FireworkMeta.setPower(power);
                return FireworkMeta;
            }
            return null;
        }

        private MapMeta mapMeta(final JsonObject rawMeta) {
            final String _MAP_ID = "map-id";

            if (rawMeta.has(_MAP_ID)) {
                int mapID = rawMeta.remove(_MAP_ID).getAsInt();

                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                MapMeta mapMeta = ((MapMeta) meta);
                mapMeta.setMapId(mapID);
                return mapMeta;
            }
            return null;
        }

        private SuspiciousStewMeta suspiciousStewMeta(final JsonObject rawMeta) {

            if (rawMeta.has("effects")) {
                JsonArray jsonEffects = rawMeta.remove("effects").getAsJsonArray();
                ArrayList<PotionEffect> potionEffects = new ArrayList<>();
                jsonEffects.forEach(e -> potionEffects.add(new PotionEffect(
                    Objects.requireNonNull(PotionEffectType.getById(e.getAsJsonObject().get("effect").getAsInt())),
                    e.getAsJsonObject().get("duration").getAsInt(),
                    e.getAsJsonObject().get("amplifier").getAsInt(),
                    e.getAsJsonObject().get("ambient").getAsBoolean(),
                    e.getAsJsonObject().get("has-particles").getAsBoolean(),
                    e.getAsJsonObject().get("has-icon").getAsBoolean()
                )));

                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                SuspiciousStewMeta newMeta = ((SuspiciousStewMeta) meta);
                potionEffects.forEach(e -> newMeta.addCustomEffect(e, true));
                return newMeta;
            }
            return null;
        }

        private TropicalFishBucketMeta tropicalFishBucketMeta(final JsonObject rawMeta) {
            if (rawMeta.has("custom-fish")) {
                final JsonObject fishModel = rawMeta.remove("custom-fish").getAsJsonObject();
                rawMeta.remove("fish-variant");
                ItemMeta meta = GsonConverter.fromJson(rawMeta, ItemMeta.class);
                TropicalFishBucketMeta Newmeta = ((TropicalFishBucketMeta) meta);
                Newmeta.setPattern(TropicalFish.Pattern.valueOf(fishModel.get("pattern").getAsString()));
                Newmeta.setPatternColor(DyeColor.legacyValueOf(fishModel.get("pattern-color").getAsString()));
                Newmeta.setBodyColor(DyeColor.legacyValueOf(fishModel.get("body-color").getAsString()));
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
            if (JSON_META.has("attribute-modifiers")) {
                final Set<Map.Entry<String, JsonElement>> _JSON_MODIFIERS = JSON_META.remove("attribute-modifiers").getAsJsonObject().entrySet();
                for (Map.Entry<String, JsonElement> mapOfModifiers : _JSON_MODIFIERS) {
                    Attribute attr = Attribute.valueOf(mapOfModifiers.getKey().toUpperCase());
                    for (JsonElement modifier : mapOfModifiers.getValue().getAsJsonArray()) {
                        AttributeModifier attrModifier = GsonConverter.fromJson(modifier, AttributeModifier.class);
                        i.addAttributeModifier(attr, attrModifier);
                    }
                }
            }
        }

        @Override
        public @NotNull JsonElement toJson(ItemMeta source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            if (source.getClass().getSimpleName().equals("CraftMetaTropicalFishBucket")) {
                final JsonObject fishMeta = new JsonObject();
                final JsonObject metaJson = GsonConverter.toJsonTree(source, ItemStack.class).getAsJsonObject();
                if (metaJson.has("meta")) {
                    TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) source;
                    fishMeta.addProperty("pattern", tropicalFishBucketMeta.getPattern().name());
                    fishMeta.addProperty("pattern-color", tropicalFishBucketMeta.getPatternColor().name());
                    fishMeta.addProperty("body-color", tropicalFishBucketMeta.getBodyColor().name());
                    metaJson.add("fish-model", fishMeta);
                    return metaJson;
                }
            } else if (source.hasCustomModelData()) {
                final JsonObject metaJson = GsonConverter.toJsonTree(source, ItemStack.class).getAsJsonObject();
                metaJson.getAsJsonObject("meta").addProperty("custom-model-data", source.getCustomModelData());
                return metaJson;
            } else {
                return GsonConverter.toJsonTree(source, ItemMeta.class);
            }
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public ItemMeta fromJson(JsonObject json) {
            JsonObject jsonMeta = json.getAsJsonObject("meta");
            ItemMeta meta = null;
            int customModelData = -999;
            if (jsonMeta.has("custom-model-data")) customModelData = jsonMeta.remove("custom-model-data").getAsInt();

            if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(0))) {
                meta = bannerMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(1))) {
                meta = axolotlMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(2))) {
                meta = bundleMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(3))) {
                meta = compassMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(4))) {
                meta = crossbowMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(6))) {
                meta = fireworkMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(7))) {
                meta = mapMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(8))) {
                meta = suspiciousStewMeta(jsonMeta);
            } else if (jsonMeta.get("meta-type").getAsString().equals(metaTypes.get(9))) {
                meta = tropicalFishBucketMeta(jsonMeta);
            } else {
                if (jsonMeta.has("Damage")) {
                    meta = damageableMeta(jsonMeta);
                } else {
                    meta = GsonConverter.fromJson(jsonMeta, ItemMeta.class);
                }
                setModel(customModelData, meta);
                setModifiers(jsonMeta, meta);
                return meta;
            }
            if (jsonMeta.has("Damage")) {
                meta = damageableMeta(jsonMeta);
            }
            setModel(customModelData, meta);
            setModifiers(jsonMeta, meta);
            return meta;
        }
    };

    public static class BukkitConverter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {

        final Type objectStringMapType = new TypeToken<Map<String, Object>>() {
        }.getType();

        @Override
        public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
            json.getAsJsonObject().entrySet().forEach(entry -> {
                if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has(SERIALIZED_TYPE_KEY)) {
                    map.put(entry.getKey(), this.deserialize(entry.getValue(), entry.getValue().getClass(), context));
                } else {
                    map.put(entry.getKey(), context.deserialize(entry.getValue(), Object.class));
                }
            });
            return ConfigurationSerialization.deserializeObject(map);
        }

        @Override
        public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
            final ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            map.put(SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(src.getClass()));
            map.putAll(src.serialize());
            return context.serialize(map, objectStringMapType);
        }
    }

    public final static SimpleConverter<Block> BlockConverter = new SimpleConverter<Block>() {
        @Override
        public @NotNull JsonElement toJson(Block source) throws Exception {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            jsonObject.addProperty("type", source.getType().name());
            jsonObject.addProperty("data", source.getData());
            jsonObject.add("location", ParserUtil.parse(source.getLocation()));
            jsonObject.addProperty("world", source.getWorld().getName());
            return !jsonObject.isEmpty() ? jsonObject : JsonNull.INSTANCE;
        }

        @Override
        public Block fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                World world;
                if ((world = getWorld(json.get("world").getAsString())) != null) {
                    Block block = world.getBlockAt(GsonConverter.fromJson(json.get("location"), Location.class));
                    block.setType(Material.valueOf(json.get("type").getAsString()));
                    return block;
                }
            }
            return null;
        }
    };
    public final static SimpleConverter<Inventory> InventoryConverter = new SimpleConverter<Inventory>() {

        @Override
        public @NotNull JsonElement toJson(Inventory source) throws Exception {
            final String sourceType = source.getType().toString();
            String invJsonTitle;
            String stringifyInventoryHolder;

            if (source.getViewers().isEmpty()) {
                stringifyInventoryHolder = "DEFAULT";
                invJsonTitle = source.getType().name();
            } else {
                Entity viewer = source.getViewers().get(0);
                stringifyInventoryHolder = viewer.getName();
                invJsonTitle = ((HumanEntity) viewer).getOpenInventory().getTitle();
            }

            final JsonObject outputJson = new JsonObject();
            final JsonObject jsonInventory = new JsonObject();
            outputJson.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            for (ItemStack item : source.getContents()) {
                String slot = "Slot " + jsonInventory.size();
                jsonInventory.add(slot, (item != null ? ItemStackConverter.toJson(item) : JsonNull.INSTANCE));
            }
            outputJson.addProperty("title", invJsonTitle);
            outputJson.addProperty("type", sourceType);
            outputJson.addProperty("holder", stringifyInventoryHolder);
            outputJson.add("contents", jsonInventory);
            return outputJson;
        }

        @Override
        public Inventory fromJson(JsonObject json) {
            final Component jsonTitle = Component.text(json.get("title").getAsString());
            final String jsonHolder = json.get("holder").getAsString();
            final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            Inventory inventory;
            if (jsonHolder.equals("DEFAULT")) {
                inventory = JsonInventory.newInventory(null, InventoryType.CHEST, json.get("title").getAsString());
            } else {
                inventory = JsonInventory.newInventory(jsonHolder, InventoryType.PLAYER, json.get("title").getAsString());
            }
            json.getAsJsonObject("contents").entrySet().forEach(item -> {
                if (item.getValue().equals(JsonNull.INSTANCE)) {
                    items.add(new ItemStack(Material.AIR));
                } else {
                    items.add(ItemStackConverter.fromJson(item.getValue().getAsJsonObject()));
                }
            });
            inventory.setContents(items.toArray(new ItemStack[0]));
            return inventory;
        }
    };


}
