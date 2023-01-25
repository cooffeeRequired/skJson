package cz.coffee.adapter;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.slot.Slot;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import cz.coffee.SkJson;
import cz.coffee.utils.Type;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cz.coffee.adapter.DefaultAdapter.SERIALIZED_JSON_TYPE_KEY;
import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.SimpleUtil.printPrettyStackTrace;
import static cz.coffee.utils.json.JsonUtils.*;
import static org.bukkit.Bukkit.createInventory;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;
//

@SuppressWarnings("unused")
public class DefaultAdapters {
    public void DefaultAdapter(){}

    /**
     * <p>
     * Serializer / Deserializer for any Bukkit/Skript Type. {@link JsonElement}
     * </p>
     */
    static class TypeAdapter {
        /**
         * <p>
         * Serializer / Deserializer for any Bukkit Type. {@link ConfigurationSerializable}
         * </p>
         */
        public static class Bukkit implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {
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
    }

    @SuppressWarnings("unchecked")
    public static <T> T assignFrom(JsonElement json) {
        Class<?> clazz = null;
        String potentialClass = null;
        if (json.getAsJsonObject().has(SERIALIZED_JSON_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get("..").getAsString();
        else if (json.getAsJsonObject().has(SERIALIZED_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get(SERIALIZED_TYPE_KEY).getAsString();

        try {
            clazz = Class.forName(potentialClass);
        } catch (ClassNotFoundException notFoundException) {
            printPrettyStackTrace(notFoundException, 5);
        }

        if (clazz != null) {
            try {
                if (World.class.isAssignableFrom(clazz))
                    return (T) new _World().fromJson(json.getAsJsonObject());
                else if (Chunk.class.isAssignableFrom(clazz))
                    return (T) new _Chunk().fromJson(json.getAsJsonObject());
                else if (ItemStack.class.isAssignableFrom(clazz))
                    return (T) new _ItemStack().fromJson(json.getAsJsonObject());
                else if (Inventory.class.isAssignableFrom(clazz))
                    return (T) new _Inventory().fromJson(json.getAsJsonObject());
                else if (ConfigurationSerializable.class.isAssignableFrom(clazz))
                    return (T) gsonAdapter.fromJson(json, clazz);
                else
                    return null;
            } catch (Exception ex) {
                printPrettyStackTrace(ex, 7);
            }
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    static JsonElement parse(Object skriptItem, Expression<?> expression, Event event) {
        if (skriptItem instanceof JsonElement) return (JsonElement) skriptItem;
        else if (isClassicType(skriptItem))
            return fromString2JsonElement(skriptItem);
        else
            if (skriptItem instanceof ItemType || skriptItem instanceof Slot || skriptItem instanceof ItemStack)
                return new DefaultAdapters._ItemStack().toJson(parseItem(skriptItem, expression, event));
        return null;
    }

    /**
     * <p>
     * Parser for ItemStack
     * </p>
     */
    @SuppressWarnings("unchecked")
    static ItemStack parseItem(Object item, Expression<?> expression, Event event) {
        Expression<?> expr;
        if (item instanceof ItemStack) return (ItemStack) item;
        else if (item instanceof Slot) {
            if ((expr = expression.getConvertedExpression(Slot.class)) != null) {
                Slot slot;
                if ((slot = (Slot) expr.getSingle(event)) != null)
                    return slot.getItem();
            }
        }
        else if (item instanceof ItemType) {
            if ((expr = expression.getConvertedExpression(ItemType.class)) != null) {
                ItemType itemType;
                if ((itemType = (ItemType) expr.getSingle(event)) != null)
                    return itemType.getRandom();
            }
        }
        return null;
    }


    /**
     * <p>
     * Serializer / Deserializer for Chunks. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from chunk at the player </code> </p>
     * <p> <code> * Deserialize   -> {_} parsed as a chunk ({_} represent serialized json) </code> </p>
     *
     * </p>
     */
    public static class _Chunk implements DefaultAdapter<Chunk> {

        @Override
        public @NotNull JsonElement toJson(org.bukkit.Chunk source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            o.addProperty("world", source.getWorld().getName());
            o.addProperty("x", source.getX());
            o.addProperty("z", source.getZ());
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public org.bukkit.Chunk fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                org.bukkit.World world;
                if ((world = org.bukkit.Bukkit.getWorld(json.get("world").getAsString())) != null) {
                    return world.getChunkAt(
                            json.get("x").getAsInt(),
                            json.get("y").getAsInt()
                    );
                }
            }
            return null;
        }

        @Override
        public Class<? extends org.bukkit.Chunk> typeOf(JsonObject json) {
            if (check(json, org.bukkit.Chunk.class.getName(), Type.KEY)) return org.bukkit.Chunk.class;
            return null;
        }
    }

    /**
     * <p>
     * Serializer / Deserializer for World. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from world at the player </code> </p>
     * <p> <code> * Deserialize   -> {_} parsed as a world ({_} represent serialized json) </code> </p>
     *
     * </p>
     */
    public static class _World implements DefaultAdapter<org.bukkit.World> {
        @Override
        public @NotNull JsonElement toJson(org.bukkit.World source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            o.addProperty("name", source.getName());
            o.addProperty("difficulty", source.getDifficulty().name());
            o.addProperty("border-size", source.getWorldBorder().getSize());
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public org.bukkit.World fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                org.bukkit.World world;
                if ((world = org.bukkit.Bukkit.getWorld(json.get("name").getAsString())) != null) {
                    world.setDifficulty(Difficulty.valueOf(json.get("difficulty").getAsString()));
                    return world;
                }
            }
            return null;
        }

        @Override
        public Class<? extends org.bukkit.World> typeOf(JsonObject json) {
            if (check(json, org.bukkit.World.class.getName(), Type.KEY)) return org.bukkit.World.class;
            return null;
        }
    }

    /**
     * <p>
     * Serializer / Deserializer for ItemStack. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from diamond sword named "&cTest" </code> </p>
     * <p> <code> * Deserialize   -> {_} parsed as a item ({_} represent serialized json) </code> </p>
     *
     * </p>
     */
    public static class _ItemStack implements DefaultAdapter<org.bukkit.inventory.ItemStack> {

        final private String[] IGNORED_CLASSES = {
                "KnowledgeBookMeta",
                "LeatherArmorMeta",
                "MusicInstrumentMeta",
                "PotionMeta",
                "Repairable",
                "SkullMeta",
                "SpawnEggMeta"
        };

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

        final private String META_ = "meta";
        final private String META_TYPE = "meta-type";

        private org.bukkit.inventory.ItemStack itemStack;

        @Override
        public @NotNull JsonElement toJson(org.bukkit.inventory.ItemStack source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            if (source.getItemMeta().getClass().getSimpleName().equals("CraftMetaTropicalFishBucket")) {
                final JsonObject fishMeta = new JsonObject();
                final JsonObject metaJson = gsonAdapter.toJsonTree(source, ItemStack.class).getAsJsonObject();
                if(metaJson.has("meta")) {
                    TropicalFishBucketMeta tropicalFishBucketMeta = (TropicalFishBucketMeta) source.getItemMeta();
                    fishMeta.addProperty("pattern", tropicalFishBucketMeta.getPattern().name());
                    fishMeta.addProperty("pattern-color", tropicalFishBucketMeta.getPatternColor().name());
                    fishMeta.addProperty("body-color", tropicalFishBucketMeta.getBodyColor().name());
                    metaJson.add("fish-model", fishMeta);
                    return metaJson;
                }
            } else {
                return gsonAdapter.toJsonTree(source, ItemStack.class);
            }
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @SuppressWarnings({"UnstableApiUsage", "unchecked", "deprecation"})
        @Override
        public org.bukkit.inventory.ItemStack fromJson(JsonObject json) {
            if (json.has(META_)) {
                final JsonObject JSON_META = json.getAsJsonObject(META_);
                boolean isIgnored = Arrays.stream(IGNORED_CLASSES).anyMatch(ignored -> JSON_META.get(META_TYPE).getAsString().equals(ignored));

                if (isIgnored) {
                    itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
                    setOthers(json);
                    return getItemStack();
                }

                if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(0))) {
                    final String _BANNER_PATTERNS = "patterns";

                    if (JSON_META.has(_BANNER_PATTERNS)) {
                        JsonArray _PATTERN_ARRAY = JSON_META.getAsJsonArray(_BANNER_PATTERNS);
                        JSON_META.remove(_BANNER_PATTERNS);
                        ArrayList<Pattern> _PATTERNS = new ArrayList<>();

                        for (JsonElement pattern : _PATTERN_ARRAY) {
                            _PATTERNS.add(new org.bukkit.block.banner.Pattern(
                                    DyeColor.valueOf(pattern.getAsJsonObject().get("color").getAsString()),
                                    Objects.requireNonNull(PatternType.getByIdentifier(pattern.getAsJsonObject().get("pattern").getAsString()))
                            ));
                        }
                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
                        BannerMeta meta = (BannerMeta) itemStack.getItemMeta();
                        meta.setPatterns(_PATTERNS);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(1))) {
                    final String _AXOLOTL_B_VARIANT = "axolotl-variant";
                    final HashMap<Integer, Axolotl.Variant> axolotlVariants = new HashMap<>() {{
                        put(0, Axolotl.Variant.LUCY);
                        put(1, Axolotl.Variant.WILD);
                        put(2, Axolotl.Variant.GOLD);
                        put(3, Axolotl.Variant.CYAN);
                        put(4, Axolotl.Variant.BLUE);
                    }};

                    if (JSON_META.has(_AXOLOTL_B_VARIANT)) {
                        Integer variant = JSON_META.get(_AXOLOTL_B_VARIANT).getAsInt();
                        JSON_META.remove(_AXOLOTL_B_VARIANT);

                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
                        AxolotlBucketMeta meta = ((AxolotlBucketMeta) itemStack.getItemMeta());
                        meta.setVariant(axolotlVariants.get(variant));
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(2))) {
                    final String _BUNDLE_ITEMS = "items";

                    if (JSON_META.has(_BUNDLE_ITEMS)) {
                        final JsonArray JSON_ITEMS_ = JSON_META.getAsJsonArray(_BUNDLE_ITEMS);
                        JSON_META.remove(_BUNDLE_ITEMS);
                        final ArrayList<org.bukkit.inventory.ItemStack> items = new ArrayList<>();
                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
                        for (JsonElement jsonItem : JSON_ITEMS_) {
                            items.add(gsonAdapter.fromJson(jsonItem, org.bukkit.inventory.ItemStack.class));
                        }

                        BundleMeta meta = ((BundleMeta) itemStack.getItemMeta());
                        meta.setItems(items);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(3))) {
                    final String _COMPASS_P_WORLD = "LodestonePosWorld";
                    final String _COMPASS_P_X = "LodestonePosX";
                    final String _COMPASS_P_Y = "LodestonePosY";
                    final String _COMPASS_P_Z = "LodestonePosZ";
                    final String _COMPASS_P_TRACKED = "LodestoneTracked";

                    if(JSON_META.has(_COMPASS_P_WORLD) && JSON_META.has(_COMPASS_P_TRACKED)) {
                        Location loc = new Location(
                                SkJson.getInstance().getServer().getWorlds().get(0),
                                JSON_META.get(_COMPASS_P_X).getAsDouble(),
                                JSON_META.get(_COMPASS_P_Y).getAsDouble(),
                                JSON_META.get(_COMPASS_P_Z).getAsDouble()
                        );
                        boolean tracked = JSON_META.get(_COMPASS_P_TRACKED).getAsBoolean();
                        JSON_META.remove(_COMPASS_P_X);
                        JSON_META.remove(_COMPASS_P_Y);
                        JSON_META.remove(_COMPASS_P_Z);
                        JSON_META.remove(_COMPASS_P_WORLD);
                        JSON_META.remove(_COMPASS_P_TRACKED);

                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);

                        CompassMeta meta = ((CompassMeta) itemStack.getItemMeta());
                        meta.setLodestone(loc);
                        meta.setLodestoneTracked(tracked);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(4))) {
                    final String _CROSSBOW_PROJECTILES = "charged-projectiles";

                    if (JSON_META.has(_CROSSBOW_PROJECTILES)) {
                        ArrayList<org.bukkit.inventory.ItemStack> _PROJECTILES = new ArrayList<>();
                        JSON_META.get(_CROSSBOW_PROJECTILES).getAsJsonArray().forEach(p -> _PROJECTILES.add(gsonAdapter.fromJson(p, org.bukkit.inventory.ItemStack.class)));

                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);

                        CrossbowMeta meta = ((CrossbowMeta) itemStack.getItemMeta());
                        meta.setChargedProjectiles(_PROJECTILES);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(5))) {
                    final String _DMG_DAMAGE = "Damage";

                    if (JSON_META.has(_DMG_DAMAGE)) {
                        int damage = JSON_META.get(_DMG_DAMAGE).getAsInt();
                        JSON_META.remove(_DMG_DAMAGE);

                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);

                        Damageable meta = ((Damageable) itemStack.getItemMeta());
                        meta.setDamage(damage);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(6))) {
                    final String _FIREWORK_EFFECTS = "firework-effects";
                    final String _FIREWORK_POWER = "power";

                    if (JSON_META.has(_FIREWORK_EFFECTS)) {
                        int power = JSON_META.get(_FIREWORK_POWER).getAsInt();
                        JsonArray effects = JSON_META.getAsJsonArray(_FIREWORK_EFFECTS);
                        JSON_META.remove(_FIREWORK_POWER);
                        JSON_META.remove(_FIREWORK_EFFECTS);


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
                                        map.getValue().getAsJsonArray().forEach(c -> colorList.add(Color.deserialize(gsonAdapter.fromJson(c, HashMap.class))));
                                    if (map.getKey().equals("type"))
                                        fType = FireworkEffect.Type.valueOf(map.getValue().getAsString());
                                    if (map.getKey().equals("flicker"))
                                        fFlicker = map.getValue().getAsBoolean();
                                    if (map.getKey().equals("trail"))
                                        fTrail = map.getValue().getAsBoolean();
                                    if (map.getKey().equals("fade-colors"))
                                        map.getValue().getAsJsonArray().forEach(c -> fadeColorList.add(Color.deserialize(gsonAdapter.fromJson(c, HashMap.class))));
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
                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);

                        FireworkMeta meta = ((FireworkMeta) itemStack.getItemMeta());
                        meta.addEffects(fireworkEffectList);
                        meta.setPower(power);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(7))) {
                    final String _MAP_ID = "map-id";

                    if (JSON_META.has(_MAP_ID)) {
                        int mapID = JSON_META.get(_MAP_ID).getAsInt();
                        JSON_META.remove(_MAP_ID);
                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);

                        MapMeta meta = ((MapMeta) itemStack.getItemMeta());
                        meta.setMapId(mapID);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(8))) {
                    final String _S_STEW_EFFECTS = "effects";

                    if (JSON_META.has(_S_STEW_EFFECTS)) {
                        JsonArray jsonEffects = JSON_META.getAsJsonArray(_S_STEW_EFFECTS);
                        ArrayList<PotionEffect> potionEffects = new ArrayList<>();
                        jsonEffects.forEach(e -> potionEffects.add(new PotionEffect(
                                Objects.requireNonNull(PotionEffectType.getById(e.getAsJsonObject().get("effect").getAsInt())),
                                e.getAsJsonObject().get("duration").getAsInt(),
                                e.getAsJsonObject().get("amplifier").getAsInt(),
                                e.getAsJsonObject().get("ambient").getAsBoolean(),
                                e.getAsJsonObject().get("has-particles").getAsBoolean(),
                                e.getAsJsonObject().get("has-icon").getAsBoolean()
                        )));

                        JSON_META.remove(_S_STEW_EFFECTS);
                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);

                        SuspiciousStewMeta newMeta = ((SuspiciousStewMeta) itemStack.getItemMeta());
                        potionEffects.forEach(e -> newMeta.addCustomEffect(e, true));
                        itemStack.setItemMeta(newMeta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(9))) {
                    final String _FISH_MODEL = "custom-fish";
                    final String _FISH_PATTERN = "pattern";
                    final String _FISH_PATTERN_COLOR = "pattern-color";
                    final String _FISH_B_COLOR = "body-color";

                    if (JSON_META.has(_FISH_MODEL)) {
                        final JsonObject fishModel = JSON_META.get(_FISH_MODEL).getAsJsonObject();
                        JSON_META.remove(_FISH_MODEL);
                        JSON_META.remove("fish-variant");

                        itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
                        TropicalFishBucketMeta meta = ((TropicalFishBucketMeta) itemStack.getItemMeta());
                        meta.setPattern(TropicalFish.Pattern.valueOf(fishModel.get(_FISH_PATTERN).getAsString()));
                        meta.setPatternColor(DyeColor.legacyValueOf(fishModel.get(_FISH_PATTERN_COLOR).getAsString()));
                        meta.setBodyColor(DyeColor.legacyValueOf(fishModel.get(_FISH_B_COLOR).getAsString()));
                        itemStack.setItemMeta(meta);
                    }

                } else {
                    itemStack = gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
                }
                setOthers(json);
                return getItemStack();
            }
            return gsonAdapter.fromJson(json, org.bukkit.inventory.ItemStack.class);
        }

        @Override
        public Class<? extends ItemStack> typeOf(JsonObject json) {
            if (check(json, ItemStack.class.getName(), Type.KEY)) return ItemStack.class;
            return null;
        }
        private void setOthers(JsonObject element) {
            final String _ENCHANTS = "enchants";
            final String _MODIFIERS = "attribute-modifiers";


            if (itemStack == null) return;
            if (element.has(META_)) {
                final JsonObject _JSON_META = element.getAsJsonObject(META_);
                if (_JSON_META.has(_ENCHANTS)) {
                    final Set<Map.Entry<String, JsonElement>> _JSON_ENCHANTS = _JSON_META.getAsJsonObject(_ENCHANTS).entrySet();
                    for (Map.Entry<String, JsonElement> mapOfEnchantments : _JSON_ENCHANTS) {
                        NamespacedKey key = null;
                        if (mapOfEnchantments.getKey().equalsIgnoreCase("damage_all")) {
                            key = new NamespacedKey("minecraft", "sharpness");
                        } else {
                            key = new NamespacedKey("minecraft", mapOfEnchantments.getKey().toLowerCase());
                        }
                        itemStack.addUnsafeEnchantment(
                                Objects.requireNonNull(Enchantment.getByKey(key)),
                                mapOfEnchantments.getValue().getAsInt()
                        );
                    }
                }
                if (_JSON_META.has(_MODIFIERS)) {
                    final Set<Map.Entry<String, JsonElement>> _JSON_MODIFIERS = _JSON_META.getAsJsonObject(_MODIFIERS).entrySet();
                    final ItemMeta i = itemStack.getItemMeta();
                    for (Map.Entry<String, JsonElement> mapOfModifiers : _JSON_MODIFIERS) {
                        Attribute attr = Attribute.valueOf(mapOfModifiers.getKey().toUpperCase());
                        for (JsonElement modifier : mapOfModifiers.getValue().getAsJsonArray()) {
                            AttributeModifier attrModifier = gsonAdapter.fromJson(modifier, AttributeModifier.class);
                            i.addAttributeModifier(attr, attrModifier);
                            itemStack.setItemMeta(i);
                        }
                    }
                }
            }
        }
        public org.bukkit.inventory.ItemStack getItemStack() {
            return this.itemStack;
        }
    }

    /**
     * <p>
     * Serializer / Deserializer for Inventory. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from inventory of player "&cTest" </code> </p>
     * <p> <code> * Deserialize   -> {_} parsed as a inventory ({_} represent serialized json) </code> </p>
     *
     * </p>
     */

    public static class _Inventory implements DefaultAdapter<org.bukkit.inventory.Inventory> {

        final static String CONTENTS_KEY_META = "meta";

        @Override
        public @NotNull JsonElement toJson(org.bukkit.inventory.Inventory source) {
            final JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            boolean isPlayer = source.getHolder() instanceof Player;

            final JsonObject _SERIALIZED_INV = new JsonObject();
            final JsonObject _SERIALIZED_ITEMS = new JsonObject();

            String _HOLDER = null;
            String _TITLE = "Unknown";
            if (isPlayer) {
                _HOLDER = ((Player) source.getHolder()).getName();
                _TITLE = String.format("Inventory of %s", ((Player) source.getHolder()).getName());
            }

            _SERIALIZED_INV.addProperty("holder", _HOLDER);
            if (isPlayer) _SERIALIZED_INV.addProperty("type", "PLAYER");
            if (!isPlayer) _SERIALIZED_INV.addProperty("size", source.getSize());
            _SERIALIZED_INV.addProperty("title", _TITLE);
            for (org.bukkit.inventory.ItemStack i : source.getContents()) {
                String slot = "Slot "+_SERIALIZED_ITEMS.size();
                if (i != null) {
                    _SERIALIZED_ITEMS.add(slot, new DefaultAdapters._ItemStack().toJson(i));
                } else {
                    _SERIALIZED_ITEMS.add(slot, JsonNull.INSTANCE);
                }
            }
            _SERIALIZED_INV.add("contents", _SERIALIZED_ITEMS);
            o.add("inventory", _SERIALIZED_INV);
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public org.bukkit.inventory.Inventory fromJson(JsonObject json) {
            final String INVENTORY_KEY = "inventory";
            final String CONTENTS_KEY = "contents";
            final String HOLDER_KEY = "holder";
            final String TYPE_KEY = "type";
            final String TITLE_KEY = "title";
            final String SIZE_KEY = "size";

            InventoryData inventoryData = new InventoryData();
            inventoryData.setType(InventoryType.valueOf(json.getAsJsonObject(INVENTORY_KEY).get(TYPE_KEY).getAsString().toUpperCase()));
            inventoryData.setHolder(inventoryData.getType().equals(InventoryType.PLAYER) ? Bukkit.getPlayer(json.getAsJsonObject(INVENTORY_KEY).get(HOLDER_KEY).getAsString()) : null);

            inventoryData.setSize(json.getAsJsonObject(INVENTORY_KEY).get(SIZE_KEY) != null ? json.getAsJsonObject(INVENTORY_KEY).get(SIZE_KEY).getAsInt() : 0);
            inventoryData.setTitle(Component.text(json.getAsJsonObject(INVENTORY_KEY).get(TITLE_KEY).getAsString()));
            inventoryData.setContents(json.getAsJsonObject(INVENTORY_KEY).getAsJsonObject(CONTENTS_KEY).asMap());


            final ArrayList<org.bukkit.inventory.ItemStack> _ITEMS = new ArrayList<>();
            final org.bukkit.inventory.Inventory _INV = inventoryData.getType() == InventoryType.PLAYER ?
                    createInventory
                            (inventoryData.getHolder(), inventoryData.getType(), inventoryData.getTitle()):
                    createInventory
                            (null, inventoryData.getSize(), inventoryData.getTitle());

            inventoryData.getContents().forEach((key, value) -> _ITEMS.add(
                    value == JsonNull.INSTANCE ? new org.bukkit.inventory.ItemStack(Material.AIR) : new _ItemStack().fromJson(value.getAsJsonObject())));
            _INV.setContents(_ITEMS.toArray(new org.bukkit.inventory.ItemStack[0]));
            return _INV;
        }

        @Override
        public Class<? extends org.bukkit.inventory.Inventory> typeOf(JsonObject json) {
            if (check(json, org.bukkit.inventory.Inventory.class.getName(), Type.KEY)) return org.bukkit.inventory.Inventory.class;
            return null;
        }

        static class InventoryData {
            private InventoryHolder holder;
            private InventoryType type;
            private int size;
            private Component title;
            private Map<String, JsonElement> contents;

            /**
             *
             * Setters / Getters
             */

            public void setHolder(InventoryHolder holder) {
                this.holder = holder;
            }

            public void setType(InventoryType type) {
                this.type = type;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public void setTitle(Component title) {
                this.title = title;
            }

            public void setContents(Map<String, JsonElement> contents) {
                this.contents = contents;
            }


            public InventoryHolder getHolder() {
                return holder;
            }

            public InventoryType getType() {
                return type;
            }

            public int getSize() {
                return size;
            }

            public Component getTitle() {
                return title;
            }

            public Map<String, JsonElement> getContents() {
                return contents;
            }
        }
    }
}
