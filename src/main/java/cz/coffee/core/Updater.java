package cz.coffee.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.SkJson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
 * <p>
 * Created: úterý (14.03.2023)
 */

public class Updater {
    private static final String apiLink = "https://api.github.com/repos/cooffeeRequired/" + getDescriptionFile().getName() + "/releases/latest";
    private static final int currentVersion = Integer.parseInt(getDescriptionFile().getVersion().replaceAll("[.]", ""));
    private static final String currVersionString = getDescriptionFile().getVersion();

    private static boolean success = false;

    private static JsonElement getGithubConfig(URL url) {
        if (url == null) return null;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        String st = null;
        try {
            response = client.newCall(request).execute();
            success = response.isSuccessful();
            if (response.isSuccessful()) {
                assert response.body() != null;
                st = response.body().string();
            }
        } catch (IOException urlException) {
            urlException.printStackTrace();
        } finally {
            assert response != null;
            response.close();
        }
        assert st != null;
        return JsonParser.parseString(st);
    }

    static {
        JsonElement apiJson;
        try {
            apiJson = getGithubConfig(new URL(apiLink));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        boolean beta = getDescriptionFile().getVersion().endsWith("-B");

        if (!beta) {
            if (!success) {
                SkJson.console("Do you have internet connection?");
                SkJson.console("Version check &cfailed");
            } else {
                String lVersionString = apiJson.getAsJsonObject().get("tag_name").getAsString();
                int latestVersion = Integer.parseInt(apiJson.getAsJsonObject().get("tag_name").getAsString().replaceAll("[.]", "").replaceAll("[^0-9]", ""));
                if (latestVersion == currentVersion) {
                    SkJson.console("You're running on &alatest stable &fversion!");
                } else if (latestVersion > currentVersion) {
                    SkJson.console("&cskJson is not up to date!");
                    SkJson.console("&8 > &7Current version: &cv" + currVersionString);
                    SkJson.console("&8 > &7Available version: &av" + lVersionString);
                    SkJson.console("&8 > &7Download available at link: &bhttps://github.com/cooffeeRequired/skJson/releases/latest");
                } else {
                    SkJson.console("You're running on non-public version, so checking is not necessary &bv" + currVersionString + "&r!");
                }
            }
        } else {
            SkJson.console("You're running on beta version, so checking is not necessary &bv" + currentVersion + "&r!");
        }
    }
}
