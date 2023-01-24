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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.SkJson;
import cz.coffee.utils.Type;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static cz.coffee.adapters.generic.JsonInventory.*;
import static cz.coffee.utils.SimpleUtil.gsonAdapter;
import static cz.coffee.utils.json.JsonUtils.check;


public class JsonItemStack implements JsonGenericAdapter<ItemStack> {

    final private String[] IGNORED_CLASSES = {
            "KnowledgeBookMeta",
            "LeatherArmorMeta",
            "MusicInstrumentMeta", // musicInstrumental -> Version depend.
            "PotionMeta", // Already Fully serialized by Bukkit.
            "Repairable", // Already Fully serialized by Bukkit.
            "SkullMeta", // Already Fully serialized by Bukkit.
            "SpawnEggMeta", // Paper deprecated, Already Fully serialized by Bukkit.
    };

    private ItemStack itemStack;

    @Override
    public @NotNull JsonElement toJson(ItemStack object) {
        if (object != null) {
            if (object.getItemMeta().getClass().getSimpleName().equals("CraftMetaTropicalFishBucket")) {
                final JsonElement json =  gsonAdapter.toJsonTree(object, ItemStack.class);
                final JsonObject customJson = new JsonObject();
                if (json.getAsJsonObject().has("meta")) {
                    TropicalFishBucketMeta tfbm = (TropicalFishBucketMeta) object.getItemMeta();
                    final JsonElement jsonMeta = json.getAsJsonObject().get("meta");
                    customJson.addProperty("pattern", tfbm.getPattern().name());
                    customJson.addProperty("pattern-color", tfbm.getPatternColor().name());
                    customJson.addProperty("body-color", tfbm.getBodyColor().name());
                    jsonMeta.getAsJsonObject().add("custom-fish", customJson);
                    return json;
                }
            } else {
                return gsonAdapter.toJsonTree(object, ItemStack.class);
            }
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public ItemStack fromJson(JsonElement json) {
        return analyse(json.getAsJsonObject());
    }

    private boolean isTypeMeta(JsonElement meta, String str) {
        return meta.getAsJsonObject().get("meta-type").getAsString().equals(str);
    }


    @SuppressWarnings({"UnstableApiUsage", "unchecked", "deprecation"})
    private ItemStack analyse(JsonObject json) {
        final String _META = "meta";
        final String _META_TYPE = "meta-type";

        // BANNER - Item-Meta
        final String _BANNER_PATTERNS = "patterns";

        // AXOLOTL_BUCKET - Item-Meta
        final String _AXOLOTL_B_VARIANT = "axolotl-variant";
        final HashMap<Integer, Axolotl.Variant> axolotlVariants = new HashMap<>() {{
            put(0, Axolotl.Variant.LUCY);
            put(1, Axolotl.Variant.WILD);
            put(2, Axolotl.Variant.GOLD);
            put(3, Axolotl.Variant.CYAN);
            put(4, Axolotl.Variant.BLUE);
        }};

        // BUNDLE - Item-Meta
        final String _BUNDLE_ITEMS = "items";

        // COMPASS - Item-Meta
        final String _COMPASS_P_WORLD = "LodestonePosWorld";
        final String _COMPASS_P_X = "LodestonePosX";
        final String _COMPASS_P_Y = "LodestonePosY";
        final String _COMPASS_P_Z = "LodestonePosZ";
        final String _COMPASS_P_TRACKED = "LodestoneTracked";

        // CROSSBOW - Item-Meta
        final String _CROSSBOW_PROJECTILES = "charged-projectiles";

        // DAMAGEABLE - Item-Meta
        final String _DMG_DAMAGE = "Damage";

        // FIREWORK - Item-Meta
        final String _FIREWORK_EFFECTS = "firework-effects";
        final String _FIREWORK_POWER = "power";

        // Map - Item-Meta
        final String _MAP_ID = "map-id";

        // SUSPICIOUS_STEW - Item-Meta
        final String _S_STEW_EFFECTS = "effects";

        // TropicalFishBucketMeta - Item-Meta
        final String _FISH_MODEL = "custom-fish";
        final String _FISH_PATTERN = "pattern";
        final String _FISH_PATTERN_COLOR = "pattern-color";
        final String _FISH_B_COLOR = "body-color";



        if (json.has(_META)) {
            JsonObject jsonMeta = json.getAsJsonObject(_META);

            boolean isIgnored = Arrays.stream(IGNORED_CLASSES).anyMatch(n -> jsonMeta.get("meta-type").getAsString().equals(n));

            if (isIgnored) {
                itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                setOthers(json);
                return getItemStack();
            }


            if (isTypeMeta(jsonMeta, "BANNER")) {
                if (jsonMeta.has(_BANNER_PATTERNS)) {
                    JsonArray patternsArray = jsonMeta.getAsJsonArray(_BANNER_PATTERNS);
                    jsonMeta.remove(_BANNER_PATTERNS);
                    ArrayList<Pattern> patterns = new ArrayList<>();

                    for (JsonElement p : patternsArray) {
                        patterns.add(new Pattern(
                                DyeColor.valueOf(p.getAsJsonObject().get("color").getAsString()),
                                Objects.requireNonNull(PatternType.getByIdentifier(p.getAsJsonObject().get("pattern").getAsString()))
                        ));
                    }

                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                    BannerMeta newMeta = ((BannerMeta) itemStack.getItemMeta());
                    newMeta.setPatterns(patterns);
                    itemStack.setItemMeta(newMeta);
                }
            } else if(isTypeMeta(jsonMeta,"AXOLOTL_BUCKET")) {
                if (jsonMeta.has(_AXOLOTL_B_VARIANT)) {
                    Integer variantInt = jsonMeta.get(_AXOLOTL_B_VARIANT).getAsInt();
                    jsonMeta.remove(_AXOLOTL_B_VARIANT);
                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                    AxolotlBucketMeta newMeta = ((AxolotlBucketMeta) itemStack.getItemMeta());
                    newMeta.setVariant(axolotlVariants.get(variantInt));
                    itemStack.setItemMeta(newMeta);
                }
            } else if (jsonMeta.get(_META_TYPE).getAsString().equals("BUNDLE")) {
                if (jsonMeta.has(_BUNDLE_ITEMS)) {
                    JsonArray jsonItems = jsonMeta.getAsJsonArray(_BUNDLE_ITEMS);
                    jsonMeta.remove(_BUNDLE_ITEMS);
                    ArrayList<ItemStack> items = new ArrayList<>();
                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                    for (JsonElement jsonItem : jsonItems) {
                        items.add(gsonAdapter.fromJson(jsonItem, ItemStack.class));
                    }

                    BundleMeta newMeta = ((BundleMeta) itemStack.getItemMeta());
                    newMeta.setItems(items);
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta,"COMPASS")) {
                if (jsonMeta.has(_COMPASS_P_WORLD) && jsonMeta.has(_COMPASS_P_TRACKED)) {
                    Location loc = new Location(
                            SkJson.getInstance().getServer().getWorlds().get(0),
                            jsonMeta.get(_COMPASS_P_X).getAsDouble(),
                            jsonMeta.get(_COMPASS_P_Y).getAsDouble(),
                            jsonMeta.get(_COMPASS_P_Z).getAsDouble()
                    );
                    boolean tracked = jsonMeta.get(_COMPASS_P_TRACKED).getAsBoolean();
                    jsonMeta.remove(_COMPASS_P_X);
                    jsonMeta.remove(_COMPASS_P_Y);
                    jsonMeta.remove(_COMPASS_P_Z);
                    jsonMeta.remove(_COMPASS_P_WORLD);
                    jsonMeta.remove(_COMPASS_P_TRACKED);

                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);

                    CompassMeta newMeta = ((CompassMeta) itemStack.getItemMeta());
                    newMeta.setLodestone(loc);
                    newMeta.setLodestoneTracked(tracked);
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta,"CROSSBOW")) {
                if (jsonMeta.has(_CROSSBOW_PROJECTILES)) {
                    ArrayList<ItemStack> projectiles = new ArrayList<>();
                    jsonMeta.get(_CROSSBOW_PROJECTILES).getAsJsonArray().forEach(p -> projectiles.add(gsonAdapter.fromJson(p, ItemStack.class)));

                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);

                    CrossbowMeta newMeta = ((CrossbowMeta) itemStack.getItemMeta());
                    newMeta.setChargedProjectiles(projectiles);
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta,"DAMAGEABLE") || isTypeMeta(jsonMeta, "UNSPECIFIC")) {
                if (jsonMeta.has(_DMG_DAMAGE)) {
                    int damage = jsonMeta.get(_DMG_DAMAGE).getAsInt();
                    jsonMeta.remove(_DMG_DAMAGE);

                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);

                    Damageable newMeta = ((Damageable) itemStack.getItemMeta());
                    newMeta.setDamage(damage);
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta,"FIREWORK")) {
                if (jsonMeta.has(_FIREWORK_EFFECTS)) {
                    int power = jsonMeta.get(_FIREWORK_POWER).getAsInt();
                    JsonArray effects = jsonMeta.getAsJsonArray(_FIREWORK_EFFECTS);
                    jsonMeta.remove(_FIREWORK_POWER);
                    jsonMeta.remove(_FIREWORK_EFFECTS);


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
                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);

                    FireworkMeta newMeta = ((FireworkMeta) itemStack.getItemMeta());
                    newMeta.addEffects(fireworkEffectList);
                    newMeta.setPower(power);
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta, "MAP")) {
                if (jsonMeta.has(_MAP_ID)) {
                    int mapID = jsonMeta.get(_MAP_ID).getAsInt();
                    jsonMeta.remove(_MAP_ID);
                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);

                    MapMeta newMeta = ((MapMeta) itemStack.getItemMeta());
                    newMeta.setMapId(mapID);
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta, "SUSPICIOUS_STEW")) {
                if (jsonMeta.has(_S_STEW_EFFECTS)) {
                    JsonArray jsonEffects = jsonMeta.getAsJsonArray(_S_STEW_EFFECTS);
                    ArrayList<PotionEffect> potionEffects = new ArrayList<>();
                    jsonEffects.forEach(e -> potionEffects.add(new PotionEffect(
                            Objects.requireNonNull(PotionEffectType.getById(e.getAsJsonObject().get("effect").getAsInt())),
                            e.getAsJsonObject().get("duration").getAsInt(),
                            e.getAsJsonObject().get("amplifier").getAsInt(),
                            e.getAsJsonObject().get("ambient").getAsBoolean(),
                            e.getAsJsonObject().get("has-particles").getAsBoolean(),
                            e.getAsJsonObject().get("has-icon").getAsBoolean()
                    )));

                    jsonMeta.remove(_S_STEW_EFFECTS);
                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);

                    SuspiciousStewMeta newMeta = ((SuspiciousStewMeta) itemStack.getItemMeta());
                    potionEffects.forEach(e -> newMeta.addCustomEffect(e, true));
                    itemStack.setItemMeta(newMeta);
                }
            } else if (isTypeMeta(jsonMeta, "TROPICAL_FISH_BUCKET")) {
                if (jsonMeta.has(_FISH_MODEL)) {
                    JsonObject fishModel = jsonMeta.get(_FISH_MODEL).getAsJsonObject();
                    jsonMeta.remove(_FISH_MODEL);
                    jsonMeta.remove("fish-variant");

                    itemStack = gsonAdapter.fromJson(json, ItemStack.class);
                    TropicalFishBucketMeta newMeta = ((TropicalFishBucketMeta) itemStack.getItemMeta());
                    newMeta.setPattern(TropicalFish.Pattern.valueOf(fishModel.get(_FISH_PATTERN).getAsString()));
                    newMeta.setPatternColor(DyeColor.legacyValueOf(fishModel.get(_FISH_PATTERN_COLOR).getAsString()));
                    newMeta.setBodyColor(DyeColor.legacyValueOf(fishModel.get(_FISH_B_COLOR).getAsString()));
                    itemStack.setItemMeta(newMeta);
                }
            } else {
                itemStack = gsonAdapter.fromJson(json, ItemStack.class);
            }
            setOthers(json);
            return getItemStack();
        } else {
            return gsonAdapter.fromJson(json, ItemStack.class);
        }
    }


    @SuppressWarnings("deprecation")
    public void setOthers(JsonElement element) {
        if (itemStack != null) {
            if (element.getAsJsonObject().has(CONTENTS_KEY_META)) {
                boolean hasEnchants = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).has(CONTENTS_KEY_ENCHANTS);
                boolean hasModifiers = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).has(CONTENTS_KEY_MODIFIERS);

                if (hasEnchants) {
                    JsonObject jsonEnchantments = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).getAsJsonObject(CONTENTS_KEY_ENCHANTS);

                    for (Map.Entry<String, JsonElement> mapOfEnchantments : jsonEnchantments.entrySet()) {
                        int enchantmentPower = mapOfEnchantments.getValue().getAsInt();
                        String enchantmentName = mapOfEnchantments.getKey();

                        //noinspection deprecation
                        Enchantment enchantment = Enchantment.getByName(enchantmentName.toUpperCase());
                        if (enchantment != null) {
                            itemStack.addUnsafeEnchantment(enchantment, enchantmentPower);
                        }
                    }
                }
                if (hasModifiers) {
                    JsonObject jsonModifiers = element.getAsJsonObject().getAsJsonObject(CONTENTS_KEY_META).getAsJsonObject(CONTENTS_KEY_MODIFIERS);
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    for (Map.Entry<String, JsonElement> mapOfModifiers : jsonModifiers.entrySet()) {
                        Attribute attribute = Attribute.valueOf(mapOfModifiers.getKey().toUpperCase());
                        for (JsonElement modifier : mapOfModifiers.getValue().getAsJsonArray()) {
                            AttributeModifier attributeModifier = gsonAdapter.fromJson(modifier, AttributeModifier.class);
                            itemMeta.addAttributeModifier(attribute, attributeModifier);
                            itemStack.setItemMeta(itemMeta);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get ItemStack from JsonItemStack
     *
     * @return itemStack.
     */

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public Class<? extends ItemStack> typeOf(JsonElement json) {
        if (check(json, ItemStack.class.getSimpleName(), Type.KEY)) {
            return ItemStack.class;
        }
        return null;
    }
}