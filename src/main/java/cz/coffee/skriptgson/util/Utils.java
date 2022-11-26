/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.util;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@SuppressWarnings("unused")
public class Utils {

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
    public static String SanitizeString(Object stringObject) {
        return String.valueOf(stringObject).replaceAll("[\"'][\\w\\s]+[\"']|\\w+[\"']\\w+", "").replaceAll("\"", "");
    }
    public static String SanitizeJson(Object stringObject) {
        return String.valueOf(stringObject).replaceAll("^[{}$]", "");
    }
    public static String gsonText(Object StringifyElement) {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(StringifyElement);
    }
    public static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

    public static Gson newGson() {
        GsonBuilder g = new GsonBuilder()
                .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitClassAdapt())
                .registerTypeHierarchyAdapter(YggdrasilSerializable.class, new SkriptClassAdapt());
       return g.disableHtmlEscaping().setPrettyPrinting().create();
    }

    public static String getGitVersion() {
        try {
            URL url = new URL("https://api.github.com/repos/cooffeeRequired/skript-gson/releases/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.connect();
            int response = conn.getResponseCode();
            if (response == 200) {
                StringBuilder inLine = new StringBuilder();
                Scanner scanner = null;
                try {
                    scanner = new Scanner(url.openStream());
                    while(scanner.hasNext())
                        inLine.append(scanner.nextLine());
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    assert scanner != null;
                    scanner.close();
                }
                return JsonParser.parseString(inLine.toString()).getAsJsonObject().get("tag_name").toString().replaceAll("\"", "");
            }
            SkriptGson.warning("GitHubRelease response code " + response);
            SkriptGson.warning("We can't check the version, check your connection");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
