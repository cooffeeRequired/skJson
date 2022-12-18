/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson.utils;

import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.adapters.BukkitClassAdapt;
import cz.coffee.skriptgson.adapters.SkriptClassAdapt;
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

    public static boolean isNumeric(String str) {
        return str != null && str.matches("[0-9.]+");
    }

    public static boolean isIncrementing(Object[] indexes) {
        int step = 1;
        int count = 1;
        for (Object o : indexes) {
            if (o instanceof String iStr) {
                count = Integer.parseInt(iStr);
            } else if (o instanceof Number number) {
                count = number.intValue();
            }
            if (step != count) return false;
            step++;
        }
        return true;
    }

    public static Gson hierarchyAdapter() {
        GsonBuilder g = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitClassAdapt())
                .registerTypeHierarchyAdapter(YggdrasilSerializable.YggdrasilExtendedSerializable.class, new SkriptClassAdapt())
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
                    while (scanner.hasNext())
                        inLine.append(scanner.nextLine());
                } catch (IOException ex) {
                    SkriptGson.warning("GitHubRelease response code " + response);
                    SkriptGson.warning("We can't check the version, check your connection");
                } finally {
                    assert scanner != null;
                    scanner.close();
                }
                return JsonParser.parseString(inLine.toString()).getAsJsonObject().get("tag_name").toString().replaceAll("\"", "");
            } else if (response == 403) {
                SkriptGson.warning("Tots of restart in a little while, GitHub API Unavailable, Code: " + response);
                return "403";
            }
        } catch (IOException ex) {
            SkriptGson.warning("Check updated &cFailed");
            SkriptGson.warning("We can't check the version, check your connection");
        }
        return null;
    }

}
