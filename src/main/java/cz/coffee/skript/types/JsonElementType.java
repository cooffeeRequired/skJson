package cz.coffee.skript.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.core.AdapterUtils;
import cz.coffee.core.Util;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cz.coffee.core.AdapterUtils.parseItem;
import static cz.coffee.core.JsonUtils.convert;

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
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Saturday (3/4/2023)
 */

@Since("2.8.0")
@SuppressWarnings("unused")
public class JsonElementType {
    public static final Collection<Class<?>> allowedTypes = List.of(ItemStack.class, Location.class, World.class, Block.class, Chunk.class, Inventory.class, ConfigurationSerializable.class);

    static {
        allowedTypes.forEach(clazz -> Converters.registerConverter(JsonElement.class, clazz, AdapterUtils::assignFrom));


        Classes.registerClass(
                new ClassInfo<>(JsonElement.class, "json")
                        .user("json")
                        .name("json element")
                        .description("Represent the json class")
                        .since("2.8.0 - performance & clean")
                        .defaultExpression(new SimpleLiteral<>(JsonNull.INSTANCE, true))
                        .parser(new Parser<>() {
                            @Override
                            public @NotNull String toString(JsonElement o, int flags) {
                                return o.toString();
                            }

                            @Override
                            public @NotNull String toVariableNameString(JsonElement o) {
                                return toString(o, 0);
                            }

                            @Override
                            public boolean canParse(@NotNull ParseContext context) {
                                return false;
                            }
                        })
                        .serializer(new Serializer<>() {
                            @Override
                            public @NotNull Fields serialize(JsonElement o) {
                                Fields fields = new Fields();
                                fields.putObject("json", o.toString());
                                return fields;
                            }

                            @Override
                            public void deserialize(JsonElement o, @NotNull Fields f) {
                                assert false;
                            }

                            @Override
                            public JsonElement deserialize(@NotNull Fields fields) throws StreamCorruptedException {
                                Object field = fields.getObject("json");
                                if (field == null) return JsonNull.INSTANCE;
                                fields.removeField("json");
                                return convert(field);
                            }

                            @Override
                            public boolean mustSyncDeserialization() {
                                return true;
                            }

                            @Override
                            protected boolean canBeInstantiated() {
                                return false;
                            }
                        })
                        .changer(new Changer<>() {
                            @SuppressWarnings("NullableProblems")
                            @Override
                            public @Nullable Class<?>[] acceptChange(@NotNull ChangeMode mode) {
                                return switch (mode) {
                                    case ADD, REMOVE -> CollectionUtils.array(Object[].class, JsonElement.class);
                                    default -> null;
                                };
                            }

                            @Override
                            public void change(JsonElement @NotNull [] what, @Nullable Object @NotNull [] delta, @NotNull ChangeMode mode) {
                                for (JsonElement json : what) {
                                    switch (mode) {
                                        case ADD -> {
                                            for (Object o : delta) {
                                                if (o != null) {
                                                    if (json.isJsonObject()) {
                                                        Skript.error(Util.color(String.format("&6Item: &fadd %s to %s", Arrays.toString(delta).replaceAll("[\\[\\]]", ""), json)));
                                                        Skript.error(Util.color("&cYou cannot add items to the json-object\n\t  &cDidn't you mean add %objects% with key to json."), ErrorQuality.NOT_AN_EXPRESSION);
                                                        return;
                                                    } else if (json.isJsonArray())
                                                        ((JsonArray) json).add(parseItem(o, o.getClass()));
                                                }
                                            }
                                        }
                                        case REMOVE -> {
                                            for (Object o : delta) {
                                                if (o != null) {
                                                    try {
                                                        if (o instanceof Number) {
                                                            if (json.isJsonArray()) {
                                                                if (((Number) o).intValue() <= (json.isJsonArray() ? json.getAsJsonArray().size() : json.getAsJsonObject().size())) {
                                                                    ((JsonArray) json).remove(((Number) o).intValue());
                                                                }
                                                            }
                                                        } else if (o instanceof String) {
                                                            if (json.isJsonObject()) {
                                                                for (String key : json.getAsJsonObject().keySet()) {
                                                                    if (key.equals(o)) {
                                                                        json.getAsJsonObject().remove(key);
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            if (json.isJsonArray()) {
                                                                for (JsonElement element : json.getAsJsonArray()) {
                                                                    if (element.equals(parseItem(o, o.getClass()))) {
                                                                        ((JsonArray) json).remove(parseItem(o, o.getClass()));
                                                                    }
                                                                }
                                                            } else {
                                                                String found = null;
                                                                for (Map.Entry<String, JsonElement> map : json.getAsJsonObject().entrySet()) {
                                                                    if (map.getValue().equals(parseItem(o, o.getClass()))) {
                                                                        found = map.getKey();
                                                                    }
                                                                }
                                                                json.getAsJsonObject().remove(found);
                                                            }
                                                        }
                                                    } catch (Exception ex) {
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        })

        );
    }
}
