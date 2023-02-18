package cz.coffee.adapter;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.shanebeestudios.skbee.api.NBT.*;
import cz.coffee.SkJson;
import cz.coffee.utils.Type;
import cz.coffee.utils.github.Version;
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
import static cz.coffee.utils.config.Config._NBT_SUPPORTED;
import static cz.coffee.utils.config.Config._STACKTRACE_LENGTH;
import static cz.coffee.utils.json.JsonUtils.*;
import static org.bukkit.Bukkit.createInventory;
import static org.bukkit.Bukkit.getWorld;
import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;
//

@SuppressWarnings("unused")
public class DefaultAdapters {

    /**
     * <p>
     * Serializer / Deserializer for Chunks. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from chunk at the player </code> </p>
     * <p> <code> * Deserialize   -> send raw {_} (Represent a element of json) </code> </p>
     *
     * </p>
     */

    private static final DefaultAdapter<Chunk> CHUNK_ADAPTER = new DefaultAdapter<>() {
        @Override
        public @NotNull JsonElement toJson(Chunk source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            o.addProperty("world", source.getWorld().getName());
            o.addProperty("x", source.getX());
            o.addProperty("z", source.getZ());
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public Chunk fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                World world;
                if ((world = getWorld(json.get("world").getAsString())) != null) {
                    return world.getChunkAt(
                            json.get("x").getAsInt(),
                            json.get("z").getAsInt()
                    );
                }
            }
            return null;
        }

        @Override
        public Class<? extends Chunk> typeOf(JsonObject json) {
            if (check(json, Chunk.class.getName(), Type.KEY)) return Chunk.class;
            return null;
        }
    };
    /**
     * <p>
     * Serializer / Deserializer for Worlds. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from world at the player </code> </p>
     * <p> <code> * Deserialize   -> send raw {_} (Represent a element of json) </code> </p>
     *
     * </p>
     */
    private static final DefaultAdapter<World> WORLD_ADAPTER = new DefaultAdapter<>() {
        @Override
        public @NotNull JsonElement toJson(World source) {
            JsonObject o = new JsonObject();
            o.addProperty(SERIALIZED_JSON_TYPE_KEY, source.getClass().getName());
            o.addProperty("name", source.getName());
            o.addProperty("difficulty", source.getDifficulty().name());
            o.addProperty("border-size", source.getWorldBorder().getSize());
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }

        @Override
        public World fromJson(JsonObject json) {
            if (json.has(SERIALIZED_JSON_TYPE_KEY)) {
                World world;
                if ((world = getWorld(json.get("name").getAsString())) != null) {
                    world.setDifficulty(Difficulty.valueOf(json.get("difficulty").getAsString()));
                    return world;
                }
            }
            return null;
        }

        @Override
        public Class<? extends World> typeOf(JsonObject json) {
            if (check(json, World.class.getName(), Type.KEY)) return World.class;
            return null;
        }
    };
    /**
     * <p>
     * Serializer / Deserializer for ItemStack. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from player's tool </code> </p>
     * <p> <code> * Deserialize   -> send raw {_} (Represent a element of json) </code> </p>
     *
     * </p>
     */
    private static final DefaultAdapter<ItemStack> ITEMSTACK_ADAPTER = new DefaultAdapter<ItemStack>() {
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

        private boolean hasCustomModel = false;

        private ItemStack itemStack;

        @Override
        public @NotNull JsonElement toJson(ItemStack source) {

            if (source.getItemMeta() == null) {
                return gsonAdapter.toJsonTree(source, ItemStack.class);
            }
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


        private ItemStack hasModel(JsonObject itemMeta, JsonElement mainJson) {
            ItemStack TempitemStack = null;
            if (itemMeta.has("custom-model-data")) {
                hasCustomModel = true;
                int customModelData = itemMeta.get("custom-model-data").getAsInt();
                itemMeta.remove("custom-model-data");
                TempitemStack = gsonAdapter.fromJson(mainJson, ItemStack.class);
                ItemMeta im = TempitemStack.getItemMeta();
                im.setCustomModelData(customModelData);
                TempitemStack.setItemMeta(im);
                return TempitemStack;
            }
            return gsonAdapter.fromJson(mainJson, ItemStack.class);
        }

        @SuppressWarnings({"UnstableApiUsage", "unchecked", "deprecation"})
        @Override
        public ItemStack fromJson(JsonObject json) {

            if (json.has(META_)) {
                final JsonObject JSON_META = json.getAsJsonObject(META_);
                boolean isIgnored = Arrays.stream(IGNORED_CLASSES).anyMatch(ignored -> JSON_META.get(META_TYPE).getAsString().equals(ignored));

                if (isIgnored) {
                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                    setOthers(json);
                    return getItemStack();
                }
                itemStack = hasModel(JSON_META, json);

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
                        itemStack = hasModel(JSON_META, json);
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

                        itemStack = hasModel(JSON_META, json);
                        AxolotlBucketMeta meta = ((AxolotlBucketMeta) itemStack.getItemMeta());
                        meta.setVariant(axolotlVariants.get(variant));
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(2))) {
                    final String _BUNDLE_ITEMS = "items";

                    if (JSON_META.has(_BUNDLE_ITEMS)) {
                        final JsonArray JSON_ITEMS_ = JSON_META.getAsJsonArray(_BUNDLE_ITEMS);
                        JSON_META.remove(_BUNDLE_ITEMS);
                        final ArrayList<ItemStack> items = new ArrayList<>();
                        itemStack = hasModel(JSON_META, json);
                        for (JsonElement jsonItem : JSON_ITEMS_) {
                            items.add(gsonAdapter.fromJson(jsonItem, ItemStack.class));
                        }

                        BundleMeta meta = ((BundleMeta) itemStack.getItemMeta());
                        meta.setItems(items);
                        itemStack.setItemMeta(meta);


                        System.out.println(itemStack);
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

                        itemStack = hasModel(JSON_META, json);

                        CompassMeta meta = ((CompassMeta) itemStack.getItemMeta());
                        meta.setLodestone(loc);
                        meta.setLodestoneTracked(tracked);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(4))) {
                    final String _CROSSBOW_PROJECTILES = "charged-projectiles";

                    if (JSON_META.has(_CROSSBOW_PROJECTILES)) {
                        ArrayList<ItemStack> _PROJECTILES = new ArrayList<>();
                        JSON_META.get(_CROSSBOW_PROJECTILES).getAsJsonArray().forEach(p -> _PROJECTILES.add(gsonAdapter.fromJson(p, ItemStack.class)));

                        itemStack = hasModel(JSON_META, json);

                        CrossbowMeta meta = ((CrossbowMeta) itemStack.getItemMeta());
                        meta.setChargedProjectiles(_PROJECTILES);
                        itemStack.setItemMeta(meta);
                    }

                } else if (JSON_META.get(META_TYPE).getAsString().equals(metaTypes.get(5))) {
                    final String _DMG_DAMAGE = "Damage";

                    if (JSON_META.has(_DMG_DAMAGE)) {
                        int damage = JSON_META.get(_DMG_DAMAGE).getAsInt();
                        JSON_META.remove(_DMG_DAMAGE);

                        itemStack = hasModel(JSON_META, json);

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
                        itemStack = hasModel(JSON_META, json);

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
                        itemStack = hasModel(JSON_META, json);

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
                        itemStack = hasModel(JSON_META, json);

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

                        itemStack = hasModel(JSON_META, json);
                        TropicalFishBucketMeta meta = ((TropicalFishBucketMeta) itemStack.getItemMeta());
                        meta.setPattern(TropicalFish.Pattern.valueOf(fishModel.get(_FISH_PATTERN).getAsString()));
                        meta.setPatternColor(DyeColor.legacyValueOf(fishModel.get(_FISH_PATTERN_COLOR).getAsString()));
                        meta.setBodyColor(DyeColor.legacyValueOf(fishModel.get(_FISH_B_COLOR).getAsString()));
                        itemStack.setItemMeta(meta);
                    }
                } else
                    if (!hasCustomModel) {
                        itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                    }
                setOthers(json);
                return getItemStack();
            }
            return gsonAdapter.fromJson(json, ItemStack.class);
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
                        itemStack.addUnsafeEnchantment(
                                Objects.requireNonNull(Enchantment.getByName(mapOfEnchantments.getKey().toUpperCase())),
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
        public ItemStack getItemStack() {
            return this.itemStack;
        }
    };
    /**
     * <p>
     * Serializer / Deserializer for Inventory. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from player's inventory </code> </p>
     * <p> <code> * Deserialize   -> send raw {_} (Represent a element of json) </code> </p>
     *
     * </p>
     */
    private static final DefaultAdapter<Inventory> INVENTORY_ADAPTER = new DefaultAdapter<>() {

        final static String CONTENTS_KEY_META = "meta";

        @Override
        public @NotNull JsonElement toJson(Inventory source) {
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
            for (ItemStack i : source.getContents()) {
                String slot = "Slot "+_SERIALIZED_ITEMS.size();
                if (i != null) {
                    _SERIALIZED_ITEMS.add(slot, ITEMSTACK_ADAPTER.toJson(i));
                } else {
                    _SERIALIZED_ITEMS.add(slot, JsonNull.INSTANCE);
                }
            }
            _SERIALIZED_INV.add("contents", _SERIALIZED_ITEMS);
            o.add("inventory", _SERIALIZED_INV);
            return !o.isEmpty() ? o : JsonNull.INSTANCE;
        }


        @SuppressWarnings("deprecation")
        @Override
        public Inventory fromJson(JsonObject json) {
            final String INVENTORY_KEY = "inventory";
            final String CONTENTS_KEY = "contents";
            final String HOLDER_KEY = "holder";
            final String TYPE_KEY = "type";
            final String TITLE_KEY = "title";
            final String SIZE_KEY = "size";

            Version version = new Version(Bukkit.getBukkitVersion());
            InventoryData inventoryData = new InventoryData(version);
            inventoryData.setType(InventoryType.valueOf(json.getAsJsonObject(INVENTORY_KEY).get(TYPE_KEY).getAsString().toUpperCase()));
            inventoryData.setHolder(inventoryData.getType().equals(InventoryType.PLAYER) ? Bukkit.getPlayer(json.getAsJsonObject(INVENTORY_KEY).get(HOLDER_KEY).getAsString()) : null);

            inventoryData.setSize(json.getAsJsonObject(INVENTORY_KEY).get(SIZE_KEY) != null ? json.getAsJsonObject(INVENTORY_KEY).get(SIZE_KEY).getAsInt() : 0);
            inventoryData.setTitle(json.getAsJsonObject(INVENTORY_KEY).get(TITLE_KEY).getAsString());
            inventoryData.setContents(json.getAsJsonObject(INVENTORY_KEY).getAsJsonObject(CONTENTS_KEY).asMap());




            final ArrayList<ItemStack> _ITEMS = new ArrayList<>();
            //noinspection deprecation
            final Inventory _INV = inventoryData.getType() == InventoryType.PLAYER ?
                    createInventory
                            (inventoryData.getHolder(), inventoryData.getType(), inventoryData.getTitle()):
                    createInventory
                            (null, inventoryData.getSize(), inventoryData.getTitle());

            inventoryData.getContents().forEach((key, value) -> _ITEMS.add(
                    value == JsonNull.INSTANCE ? new ItemStack(Material.AIR) : ITEMSTACK_ADAPTER.fromJson(value.getAsJsonObject())));
            _INV.setContents(_ITEMS.toArray(new ItemStack[0]));
            return _INV;
        }

        @Override
        public Class<? extends Inventory> typeOf(JsonObject json) {
            if (check(json, Inventory.class.getName(), Type.KEY)) return Inventory.class;
            return null;
        }

        class InventoryData {
            private InventoryHolder holder;
            private InventoryType type;
            private int size;
            private String title;
            /**
             *
             * Setters / Getters
             */

            private final Version version;
            private Map<String, JsonElement> contents;

            protected InventoryData(Version version) {
                this.version = version;
            }

            protected InventoryHolder getHolder() {
                return holder;
            }

            protected void setHolder(InventoryHolder holder) {
                this.holder = holder;
            }

            protected InventoryType getType() {
                return type;
            }

            protected void setType(InventoryType type) {
                this.type = type;
            }

            protected int getSize() {
                return size;
            }

            protected void setSize(int size) {
                this.size = size;
            }

            protected String getTitle() {
                return title;
            }

            protected void setTitle(String title) {
                this.title = title;
            }

            protected Map<String, JsonElement> getContents() {
                return contents;
            }

            protected void setContents(Map<String, JsonElement> contents) {
                this.contents = contents;
            }
        }
    };

    /**
     * <p>
     * Serializer / Deserializer for NBT. {@link JsonElement}
     * </p>
     * <p>
     * <b>Example</b>
     * </p>
     *
     * <p> <code> * Serialize     -> json from nbt compound of player's inventory </code> </p>
     * <p> <code> * Deserialize   -> send raw {_} (Represent a element of json) </code> </p>
     *
     * </p>
     */

    private static final DefaultAdapter<NBTContainer> NBT_ADAPTER = new DefaultAdapter<>() {
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

        @Override
        public Class<? extends NBTContainer> typeOf(JsonObject json) {
            if (check(json, NBTContainer.class.getName(), Type.KEY)) return NBTContainer.class;
            return null;
        }
    };

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

    @SuppressWarnings("unchecked")
    public static <T> T assignFrom(JsonElement json) {
        Class<?> clazz = null;
        String potentialClass = null;
        if (json.getAsJsonObject().has(SERIALIZED_JSON_TYPE_KEY))
            potentialClass = json.getAsJsonObject().get("..").getAsString();
        else if (json.getAsJsonObject().has(SERIALIZED_TYPE_KEY)) {
            potentialClass = json.getAsJsonObject().get(SERIALIZED_TYPE_KEY).getAsString();
        }

        try {
            clazz = Class.forName(potentialClass);
        } catch (ClassNotFoundException notFoundException) {
            printPrettyStackTrace(notFoundException, _STACKTRACE_LENGTH);
        }
        if (clazz != null) {
            try {
                if (World.class.isAssignableFrom(clazz))
                    return (T) WORLD_ADAPTER.fromJson(json.getAsJsonObject());
                else if (Chunk.class.isAssignableFrom(clazz))
                    return (T) CHUNK_ADAPTER.fromJson(json.getAsJsonObject());
                else if (ItemStack.class.isAssignableFrom(clazz))
                    return ((T) ITEMSTACK_ADAPTER.fromJson(json.getAsJsonObject()));
                else if (Inventory.class.isAssignableFrom(clazz))
                    return (T) INVENTORY_ADAPTER.fromJson(json.getAsJsonObject());
                else if (ConfigurationSerializable.class.isAssignableFrom(clazz))
                    return (T) gsonAdapter.fromJson(json, clazz);
                else if (NBTContainer.class.isAssignableFrom(clazz)) {
                    if (_NBT_SUPPORTED) {
                        return (T) NBT_ADAPTER.fromJson(json.getAsJsonObject());
                    }
                }
                else {
                    return null;
                }
            } catch (Exception ex) {
                printPrettyStackTrace(ex, _STACKTRACE_LENGTH);
            }
        }
        return null;
    }

    public static <T> JsonElement assignTo(T object) {
        if (object == null) return JsonNull.INSTANCE;
        boolean isSerializable = (object instanceof YggdrasilSerializable || object instanceof ConfigurationSerializable);
        boolean isNBT = false;

        if (object instanceof World) return WORLD_ADAPTER.toJson((World) object);
        else if (object instanceof ItemStack) return ITEMSTACK_ADAPTER.toJson((ItemStack) object);
        else if (object instanceof Chunk) return CHUNK_ADAPTER.toJson((Chunk) object);
        else if (object instanceof NBTCompound) {
            if (_NBT_SUPPORTED) return NBT_ADAPTER.toJson(new NBTContainer(object.toString()));
            return null;
        }
        else if (object instanceof Inventory) return INVENTORY_ADAPTER.toJson((Inventory) object);
        else if (isSerializable) return gsonAdapter.toJsonTree(object, ConfigurationSerializable.class);
        else return null;
    }

    public static JsonElement parse(Object skriptItem, Expression<?> expression, Event event) {
        if (skriptItem instanceof JsonElement) {
            return (JsonElement) skriptItem;
        }
        else if (isClassicType(skriptItem)) {
            return convert(skriptItem);
        }
        else {
            if (skriptItem instanceof ItemType || skriptItem instanceof Slot || skriptItem instanceof ItemStack) {
                return ITEMSTACK_ADAPTER.toJson(parseItem(skriptItem, expression, event));
            } else {
                return assignTo(skriptItem);
            }
        }
    }
    public static JsonElement parse(Object item, Event e) {
        Object finalI = null;
        if (item instanceof JsonElement) {
            return (JsonElement) item;
        } else if (isClassicType(item)){
            return convert(item);
        }else{
            if (item instanceof ItemType) {
                ItemType i = (ItemType) item;
                finalI = i.getRandom();
            } else if (item instanceof ItemStack) {
                finalI = ((ItemStack) item);
            } else if (item instanceof Slot) {
                finalI = ((Slot) item).getItem();
            } else {
                finalI = item;
            }
        }
        return DefaultAdapters.assignTo(finalI);
    }

    /**
     * <p>
     * Serializer / Deserializer for any Bukkit/Skript Type. {@link JsonElement}
     * </p>
     */
    public static class TypeAdapter {
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
}
