package cz.coffee.skjson.api.Update;

import com.google.gson.JsonElement;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.skript.requests.Requests;
import cz.coffee.skjson.utils.Util;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.PROJECT_DEBUG;

/**
 * The type Update check.
 */
public class UpdateCheck {
    private final JavaPlugin plugin;
    private final Config config;
    private final String API;
    private boolean success = false;

    /**
     * Instantiates a new Update check.
     *
     * @param plugin the plugin
     * @param config the config
     */
    public UpdateCheck(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        API = "https://api.github.com/repos/skJsonTeam/" + plugin.getPluginMeta().getName() + "/releases/latest";
    }

    /**
     * Gets ready.
     *
     * @return the ready
     */
    public boolean getReady() {
        final JsonElement apiResponse = getGithubConfig();
        final PluginMeta meta = plugin.getPluginMeta();
        final String currentVersion = meta.getVersion();
        final int sanitizedCurrentVersion = sanitizeVersion(currentVersion);
        boolean beta = currentVersion.startsWith("-");

        config.setCurrentVersion(sanitizedCurrentVersion);

        if (beta) {
            Util.log("You're running on beta version, so checking is not necessary (&b"+currentVersion+"v.&r)");
        } else {
            if (apiResponse != null) {
                if (!success) Util.log("&eDo you have internet connection? Version check &c&lFailed");
                String onlineVersion = "";
                try {
                   onlineVersion = apiResponse.getAsJsonObject().get("tag_name").getAsString();
                } catch (Exception ignored) {}
                int sanitizeOnlineVersion = sanitizeVersion(onlineVersion);
                if (sanitizedCurrentVersion == sanitizeOnlineVersion) {
                    Util.log("You're running on &alast&f stable version. " + onlineVersion + "v.");
                } else if (sanitizeOnlineVersion > sanitizedCurrentVersion ) {
                    Util.log("&cskJson is not up to date!");
                    Util.log("&8 > &7Current version: &cv" + currentVersion);
                    Util.log("&8 > &7Available version: &av" + onlineVersion);
                    Util.log("&8 > &7Download available at link: &bhttps://github.com/SkJsonTeam/skJson/releases/latest");
                } else {
                    Util.log("You're running on non-public version, so checking is not necessary &bv" + currentVersion + "&r!");
                }
            }
        }
        return true;
    }

    private int sanitizeVersion(String version) {
        try {
            String replaced = version.replaceAll("[.]", "");
            if (replaced.length() == 2) replaced = replaced + 0;
            return Integer.parseInt(replaced);
        } catch (NumberFormatException exception) {
            if (PROJECT_DEBUG) Util.error(exception.getMessage());
        }
        return 0;
    }

    private JsonElement getGithubConfig() {
        CompletableFuture<JsonElement> ft = CompletableFuture.supplyAsync(() -> {
            JsonElement element = null;
            HttpWrapper.Response response = null;
            try (var handler = new HttpWrapper(API, Requests.RequestMethods.GET)) {
                response = handler.addHeader("Accept", "application/json")
                        .request()
                        .process();
                success = response.isSuccessfully();
            } catch (Exception e) {
                if (PROJECT_DEBUG) Util.error(e.getMessage());
                return null;
            } finally {
                if (response != null) {
                    element = response.getBodyContent();
                }
            }
            return element;
        });
        return ft.join();
    }
}