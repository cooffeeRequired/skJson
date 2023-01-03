package cz.coffee.skriptgson.github;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.ERROR;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.WARNING;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;

public class VersionHexTagChecker {

    public static String gitVersionTag = getGitDevTag();
    public static String currentVersionTag = Properties.currentUserVersionTag;

    private static String getGitDevTag() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/cooffeeRequired/skript-gson/main/src/main/resources/local.properties");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            if (conn.getResponseCode() == 200) {
                StringBuilder inLine = new StringBuilder();
                Scanner scanner = null;
                try {
                    scanner = new Scanner(url.openStream());
                    while (scanner.hasNext())
                        inLine.append(scanner.nextLine());
                } catch (IOException ex) {
                    sendErrorMessage("GitHub Release response code " + conn.getResponseCode(), ERROR);
                    sendErrorMessage("We can't check the version tag, check your connection", WARNING);
                } finally {
                    assert scanner != null;
                    scanner.close();
                }
                String tag = inLine.toString().replaceAll("version=", "");
                return JsonParser.parseString(tag).getAsJsonObject().get("v").toString();
            }
        } catch (IOException ex) {
            sendErrorMessage("Check updated &cFailed", WARNING);
            sendErrorMessage("We can't check the version, check your connection", WARNING);
        }
        return null;
    }
}
