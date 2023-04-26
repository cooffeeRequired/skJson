package cz.coffee.core;

import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.core.requests.HttpHandler;

import static cz.coffee.SkJson.getDescriptionFile;
import static cz.coffee.core.utils.AdapterUtils.parseItem;

public class Updater {
    private static final String apiLink = "https://api.github.com/repos/skJsonTeam/" + getDescriptionFile().getName() + "/releases/latest";
    private static final int currentVersion = Integer.parseInt(getDescriptionFile().getVersion().replaceAll("[.]", ""));
    private static final String currVersionString = getDescriptionFile().getVersion();

    private static boolean success = false;

    private static JsonElement getGithubConfig() throws Exception {
        JsonElement element = null;
        HttpHandler handler = HttpHandler.of(Updater.apiLink, "GET");
        handler.asyncSend();
        try {
            success = handler.isSuccessful();
            handler.asyncSend();
        } finally {
            if (handler.getAll().rawBody() != null) element = parseItem(handler.getAll().rawBody(), handler.getAll().rawBody().getClass());
        }
        assert element != null;
        return element;
    }

    static {
        JsonElement apiJson;
        try {
            apiJson = getGithubConfig();
        } catch (Exception e) {
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
