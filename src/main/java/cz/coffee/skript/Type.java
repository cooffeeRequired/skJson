package cz.coffee.skript;


import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.annotation.Used;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.StreamCorruptedException;
import java.util.List;

import static cz.coffee.adapter.DefaultAdapter.convert;

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
 */

@Used
public class Type {

    private static final List<Class<?>> allowedClass = List.of(ItemStack.class, Location.class, World.class, Chunk.class, Inventory.class, ConfigurationSerializable.class);

    static {
        allowedClass.forEach(class0 -> {
            Converters.registerConverter(JsonElement.class, class0, DefaultAdapter::assignFrom);
        });

        Classes.registerClass(
                new ClassInfo<>(JsonElement.class, "json")
                        .user("json")
                        .name("json")
                        .description("Represents a json element and its class `JsonElement.class`.")
                        .since("2.5.0, 2.6.2 - Fix ItemStack Converter")
                        .parser(
                                new Parser<>() {
                                    @Override
                                    public @NotNull String toString(JsonElement element, int i) {
                                        return element.toString();
                                    }

                                    @Override
                                    public @NotNull String toVariableNameString(JsonElement element) {
                                        return toString(element, 0);
                                    }

                                    @Override
                                    public boolean canParse(@NotNull ParseContext context) {
                                        return false;
                                    }

                                }
                        )
                        .defaultExpression(new SimpleLiteral<>(JsonNull.INSTANCE, true))
                        .serializer(
                                new Serializer<>() {
                                    @Override
                                    public @NotNull Fields serialize(JsonElement element) {
                                        Fields fields = new Fields();
                                        fields.putObject("json", element.toString());
                                        return fields;
                                    }

                                    @Override
                                    public void deserialize(JsonElement element, @NotNull Fields fields) {
                                        assert false;
                                    }

                                    @Override
                                    public JsonElement deserialize(@NotNull Fields fields) throws StreamCorruptedException {
                                        Object fieldContent = fields.getObject("json");
                                        if (fieldContent == null) return JsonNull.INSTANCE;
                                        fields.removeField("json");
                                        return convert(fieldContent);
                                    }

                                    @Override
                                    public boolean mustSyncDeserialization() {
                                        return false;
                                    }

                                    @Override
                                    protected boolean canBeInstantiated() {
                                        return false;
                                    }
                                }
                        )
        );
    }

}
