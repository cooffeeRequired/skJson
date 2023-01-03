package cz.coffee.skriptgson.github;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.ERROR;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.WARNING;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;

public class VersionChecker {

    public static int responseCode;

    public static String gitHubVersion = getGitVersion();
    private static JsonElement source;
    public static String downloadLinkToLatest = getDownloadUrl();
    public static String currentVersion = Properties.getCurrentUserVersion;

    private static String getGitVersion() {
        try {
            URL url = new URL("https://api.github.com/repos/cooffeeRequired/skript-gson/releases/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.connect();

            responseCode = conn.getResponseCode();

            if (conn.getResponseCode() == 200) {
                StringBuilder inLine = new StringBuilder();
                Scanner scanner = null;
                try {
                    scanner = new Scanner(url.openStream());
                    while (scanner.hasNext())
                        inLine.append(scanner.nextLine());
                } catch (IOException ex) {
                    sendErrorMessage("Github Release response code " + conn.getResponseCode(), ERROR);
                    sendErrorMessage("We can't check the version, check your connection", WARNING);
                } finally {
                    assert scanner != null;
                    scanner.close();
                }

                source = JsonParser.parseString(inLine.toString()).getAsJsonObject().getAsJsonArray("assets");

                return JsonParser.parseString(inLine.toString()).getAsJsonObject().get("tag_name").toString().replaceAll("\"", "");
            } else if (conn.getResponseCode() == 403) {
                sendErrorMessage("Lots of restart in a little while, GitHub API Unavailable, Code: " + conn.getResponseCode(), WARNING);
            }
        } catch (IOException ex) {
            sendErrorMessage("Check updated &cFailed", WARNING);
            sendErrorMessage("We can't check the version, check your connection", WARNING);
        }
        return null;
    }

    private static String getDownloadUrl() {
        String userVersion = Bukkit.getVersion();
        String userversion0 = userVersion.split("MC:")[1];
        userversion0 = userversion0.replaceAll(" ", "").replaceAll("[)]", "").replaceAll("[.]", "");

        int version = Integer.parseInt(userversion0);
        int finalVersion = Integer.parseInt("1165");
        JsonElement source;
        if (responseCode == 200) {
            if (version > finalVersion) {
                source = VersionChecker.source.getAsJsonArray().get(1).getAsJsonObject().get("browser_download_url");
            } else {
                source = VersionChecker.source.getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url");
            }
            return source.toString();
        }
        return null;
    }
}
