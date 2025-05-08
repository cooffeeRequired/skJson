package cz.coffeerequired.support;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.Api;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Formatter;

import static cz.coffeerequired.api.Api.Records.mapping;

public class Configuration {

    private static final String REPOSITORY = "skJson";
    private static final String USERNAME = "cooffeeRequired";
    private final JavaPlugin plugin;
    @SuppressWarnings("unused")
    @Getter
    private final PluginConfigHandler handler = new PluginConfigHandler(SkJson.getInstance());

    public Configuration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static YamlConfiguration getPluginConfig() {
        InputStream pluginYmlStream = SkJson.getInstance().getResource("plugin.yml");
        assert pluginYmlStream != null;

        return YamlConfiguration.loadConfiguration(new InputStreamReader(pluginYmlStream));
    }

    public static void applyScheduledUpdate() {
        File updateFile = new File(SkJson.getInstance().getDataFolder(), "updated.yml");
        if (updateFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(updateFile));
                String targetFile = reader.readLine().split(": ")[1];
                String tempFile = reader.readLine().split(": ")[1];
                reader.close();

                targetFile = targetFile.replaceFirst(".paper-remapped\\\\", "");
                // Move the temp file to the target location
                Files.move(Path.of(tempFile), Path.of(targetFile), StandardCopyOption.REPLACE_EXISTING);
                SkJson.info("Update applied successfully.");

                // Delete the update file
                Files.delete(updateFile.toPath());
            } catch (IOException e) {
                SkJson.exception(e, "Failed to apply the scheduled update.");
            }
        }
    }

    public static String getMapping(final String key) {
        if (mapping.containsKey(key)) {
            return mapping.get(key);
        }
        return null;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void checkForUpdate() {

        if (Api.Records.DISABLED_UPDATE) {
            SkJson.info("Update checking is disabled by config.");
            return;
        }


        try {
            URI url = new URI(String.format("https://api.github.com/repos/%s/%s/releases/latest", USERNAME, REPOSITORY));
            SkJson.info("Checking for updates");

            HttpURLConnection conn = (HttpURLConnection) url.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
            }
            conn.disconnect();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            String latestVersion = jsonObject.get("tag_name").getAsString();
            String currentVersion = plugin.getPluginMeta().getVersion();
            if (currentVersion.compareTo(latestVersion) < 0) {
                String downloadUrl = jsonObject.getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();
                scheduleUpdate(downloadUrl);
            } else if (currentVersion.compareTo(latestVersion) > 0) {
                SkJson.info("Running a Development version, no update required &a ✔");
            } else {
                SkJson.info("SkJson is up-to-date &a ✔");
            }
        } catch (Exception e) {
            SkJson.severe("Update failed " + e.getMessage());
        }
    }

    private void scheduleUpdate(final String downloadUrl) {
        try {
            File pluginFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            Path tempPath = Files.createTempFile("plugin-update", ".tmp");

            // Get hash of the old file
            String oldFileHash = getFileHash(pluginFile);

            try (InputStream in = new URI(downloadUrl).toURL().openStream()) {
                Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Get hash of the new file
            String newFileHash = getFileHash(tempPath.toFile());

            // Create the update information file to apply after server restarts
            createUpdateYml(pluginFile.toPath(), tempPath, oldFileHash, newFileHash);
        } catch (Exception e) {
            SkJson.exception(e, e.getMessage());
        }
    }

    private String getFileHash(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = Files.newInputStream(file.toPath())) {
            byte[] dataBytes = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, bytesRead);
            }
        }
        byte[] digestBytes = md.digest();
        try (Formatter formatter = new Formatter()) {
            for (byte b : digestBytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    private void createUpdateYml(Path targetPath, Path tempPath, String oldFileHash, String newFileHash) throws IOException {
        File updateFile = new File(plugin.getDataFolder(), "updated.yml");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(updateFile))) {
            writer.write("target_file: " + targetPath.toString());
            writer.newLine();
            writer.write("temp_file: " + tempPath.toString());
            writer.newLine();
            writer.write("old_file_hash: " + oldFileHash);
            writer.newLine();
            writer.write("new_file_hash: " + newFileHash);
        }
        SkJson.info("Update scheduled, it will be applied after server restart.");
    }
}