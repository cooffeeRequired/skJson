package cz.coffee.skriptgson.github;

import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.INFO;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;

public class Properties {

    public static String currentUserVersionTag = localTag(readProperties());
    public static String getCurrentUserVersion = SkriptGson.getInstance().getDescription().getVersion();


    private static String readProperties() {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (InputStream is = SkriptGson.getInstance().getClass().getResourceAsStream("/local.properties")) {
            if (is == null) return "0";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line);
                }
            } catch (IOException exception) {
                sendErrorMessage("", INFO);
            }
        } catch (IOException exception) {
            sendErrorMessage("", INFO);
        }
        return resultStringBuilder.toString();
    }

    private static String localTag(String tag) {
        return JsonParser.parseString(tag.replaceAll("version=", "")).getAsJsonObject().get("v").toString();
    }
}
