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
package cz.coffee.utils.github;

import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.utils.ErrorHandler;
import cz.coffee.utils.HTTPHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cz.coffee.SkJson.CURRENT_HASH;
import static cz.coffee.utils.ErrorHandler.sendMessage;

public class Updater {
    private static final PluginDescriptionFile pdf;

    static {
        pdf = SkJson.getInstance().getDescription();
    }

    private static final String latestLink = "https://api.github.com/repos/cooffeeRequired/" + pdf.getName() + "/releases/latest";
    private static final String userVer = pdf.getVersion();
    private static String latestVersion;
    private static String status;


    private int responseCode;

    public Updater(Version version) {
        SkJson.console("&7Checking for updates..");
        if (SkJson.getInstance().getDescription().getVersion().endsWith("-B")) {
            status = "BETA";
            SkJson.console("You're running on beta version, so checking is not necessary &bv" + userVer + "&r!");
        } else {
            if (version.isLegacy()) {
                SkJson.console("&eYou're running on Legacy minecraft version &6 " + Bukkit.getServer().getVersion());
            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            if (executor.submit(this::init).isDone())
                Thread.currentThread().setName("skJson-Updater");{
                executor.shutdown();
            }
            if (responseCode != 200) {
                SkJson.console("Do you have internet connection?");
                SkJson.console("Version check &cfailed");
            } else {
                if (getStatus().equals("OUTDATED")) {
                    SkJson.console("&cskJson is not up to date!");
                    if (userVer.equals(latestVersion)) {
                        SkJson.console("&8 > &7GitTag: &c@e0291c");
                        SkJson.console("&8 > &7Please download the same release from github...");
                    } if (Integer.parseInt(userVer.replaceAll("[.]", "")) > Integer.parseInt(latestVersion.replaceAll("[.]", ""))) {
                        SkJson.console("&7You are running on non-publish version!");
                        SkJson.console("&8 > &7Current version: #19b0e3v" + userVer);
                    } else {
                        SkJson.console("&8 > &7Current version: &cv" + userVer);
                        SkJson.console("&8 > &7Available version: &av" + latestVersion);
                        SkJson.console("&8 > &7Download available at link: &bhttps://github.com/cooffeeRequired/skJson/releases/latest");
                    }
                } else {
                    SkJson.console("You're running on &alatest stable &fversion!");
                }
            }
        }
    }


    public static String getStatus() {
        return status;
    }


    private void init() {
        HTTPHandler http;
        String link2LatestVersion;
        http = new HTTPHandler(latestLink, "GET");

        http.setProperty("Accept", "application/vnd.github+json");
        http.setTimeout(1000);
        http.connect();
        responseCode = http.getResponse();
        if (responseCode != 200) {
            return;
        }
        JsonElement contents = (JsonElement) http.getContents(true);
        link2LatestVersion = contents.getAsJsonObject().getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();
        latestVersion = contents.getAsJsonObject().get("tag_name").getAsString();
        Hash currentJarHash = CURRENT_HASH;
        Hash gitLatestJarHash = null;
        try {
            gitLatestJarHash = new Hash(new URL(link2LatestVersion), "SHA-256");
        } catch (MalformedURLException mlw) {
            sendMessage(mlw.getMessage(), ErrorHandler.Level.ERROR);
        }
        assert gitLatestJarHash != null;
        if (!Objects.equals(gitLatestJarHash.get(), currentJarHash.get())) {
            status = "OUTDATED";
        } else {
            status = "LATEST";
        }
    }
}
