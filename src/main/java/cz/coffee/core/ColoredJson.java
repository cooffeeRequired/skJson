package cz.coffee.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import cz.coffee.core.utils.Util;

import static cz.coffee.core.ColoredJson.Colors.*;

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
 * Created: Saturday (3/10/2023)
 */
@SuppressWarnings("unused")
public class ColoredJson {

    enum Colors {
        DARK_BLUE(Util.color("&1")),
        DARK_GREEN(Util.color("&2")),
        DARK_AQUA(Util.color("&3")),
        DARK_RED(Util.color("&4")),
        DARK_PURPLE(Util.color("&5")),
        GOLD(Util.color("&6")),
        GRAY(Util.color("&7")),
        DARK_GRAY(Util.color("&8")),
        BLUE(Util.color("&9")),
        GREEN(Util.color("&a")),
        AQUA(Util.color("&b")),
        RED(Util.color("&c")),
        PURPLE(Util.color("&d")),
        YELLOW(Util.color("&e")),
        WHITE(Util.color("&f"));
        final String color;
        Colors(String st) {
            color = st;
        }

    }

    final static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private String finalOutput;

    public ColoredJson(JsonElement input) {
        process(input);
    }

    private void process(JsonElement input) {

        final String RESET = Util.color("&r");

        if (input == null) {
            return;
        }
        StringBuilder jsonString = new StringBuilder(gson.toJson(input));
        finalOutput = new String(jsonString)
                        .replaceAll("(?<=\\W)([+]?([0-9]*[.])?[0-9]+)", AQUA.color + "$1" + WHITE.color)
                        .replaceAll("(?i:true)", GREEN.color + "$0" + WHITE.color)
                        .replaceAll("(?i:false)", RED.color+"$0"+WHITE.color)
                        .replaceAll("(\")((.)|)", DARK_GRAY.color + "$1" + WHITE.color + "$2" + WHITE.color)
                        .replaceAll("([{}])|([\\[\\]])",GRAY.color + "$1" + YELLOW.color + "$2" + WHITE.color);
    }



    public String getOutput() {
       return finalOutput;
    }

}
