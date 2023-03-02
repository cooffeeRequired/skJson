package cz.coffee.core;

import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.core.annotation.Used;

import static cz.coffee.SkJson.getDescriptionFile;

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
public class Updater {
    private static final String apiLink = "https://api.github.com/repos/cooffeeRequired/" + getDescriptionFile().getName() + "/releases/latest";
    private static final int currentVersion = Integer.parseInt(getDescriptionFile().getVersion().replaceAll("[.]", ""));

    static {
        HTTPHandler http = new HTTPHandler(apiLink, "GET");
        http.setProperty("Accept", "application/vnd.github+json");
        http.connect();
        boolean outOfConnection = http.getResponse() == 200;
        boolean beta = getDescriptionFile().getVersion().endsWith("-B");

        if (!beta) {
            if (!outOfConnection) {
                SkJson.console("Do you have internet connection?");
                SkJson.console("Version check &cfailed");
            } else {
                JsonElement json = (JsonElement) http.getContents(true);
                int latestVersion = Integer.parseInt(json.getAsJsonObject().get("tag_name").getAsString().replaceAll("[.]", ""));

                if (latestVersion == currentVersion) {
                    SkJson.console("You're running on &alatest stable &fversion!");
                } else if (latestVersion > currentVersion) {
                    SkJson.console("&cskJson is not up to date!");
                    SkJson.console("&8 > &7Current version: &cv" + currentVersion);
                    SkJson.console("&8 > &7Available version: &av" + latestVersion);
                    SkJson.console("&8 > &7Download available at link: &bhttps://github.com/cooffeeRequired/skJson/releases/latest");
                } else {
                    SkJson.console("You're running on non-public version, so checking is not necessary &bv" + currentVersion + "&r!");
                }
            }
        } else {
            SkJson.console("You're running on beta version, so checking is not necessary &bv" + currentVersion + "&r!");
        }

    }


}
