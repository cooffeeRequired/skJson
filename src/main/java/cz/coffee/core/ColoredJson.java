package cz.coffee.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

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

public class ColoredJson {
    final static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private String finalOutput;

    public ColoredJson(JsonElement input) {
        process(input);
    }

    private void process(JsonElement input) {
        if (input == null) {
            return;
        }
        StringBuilder jsonString = new StringBuilder(gson.toJson(input));
        finalOutput = new String(jsonString)
                        .replaceAll("(?<=\\W)([+]?([0-9]*[.])?[0-9]+)", "§b$1§f")
                        .replaceAll("(?i:true)", "§a$0§f")
                        .replaceAll("(?i:false)", "§c$0§f")
                        .replaceAll("(\")(.)", "§8$1§f$2§f")
                        .replaceAll("([{}])|([\\[\\]])", "§7$1§e$2§f"
                        );
    }


    public String getOutput() {
       return finalOutput;
    }

}
