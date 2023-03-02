package cz.coffee.core;

import cz.coffee.SkJson;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

import static cz.coffee.SkJson.pluginManager;

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

public class Hookups {
    public final static String[] hookups = {"NBT-API", "SkBee", "skript-reflect", "Reqn"};

    public static void check() {
        for (String hookup : hookups) {
            if (Objects.equals(hookup, "NBT-API")) {
                SkJson.console("&b&l" + hookup + "&r hook up &asuccesfully");
                new NBTContainer("{\"A\": 0b}");
            } else {
                Plugin plugin = pluginManager().getPlugin(hookup);
                if (!(plugin == null || plugin.isEnabled())) {
                    SkJson.console("&f&l" + hookup + "&r hook up &asuccesfully");
                } else {
                    SkJson.console("&f&l" + hookup + "&r hook up &cfailed");
                }
            }
        }
    }
}
