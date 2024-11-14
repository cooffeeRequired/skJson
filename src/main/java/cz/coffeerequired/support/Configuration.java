package cz.coffeerequired.support;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.coffeerequired.SkJson;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Formatter;

public class Configuration {

    private static final String REPOSITORY = "skJson";
    private static final String USERNAME = "cooffeeRequired";
    private final JavaPlugin plugin;
    @SuppressWarnings("unused")
    private final PluginConfigHandler configHandler = new PluginConfigHandler(SkJson.getInstance());

    public Configuration(JavaPlugin plugin) {
        this.plugin = plugin;
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
                SkJson.logger().info("Update applied successfully.");

                // Delete the update file
                Files.delete(updateFile.toPath());
            } catch (IOException e) {
                SkJson.logger().exception("Failed to apply the scheduled update.", e);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void checkForUpdate() {
        try {
            URI url = new URI(String.format("https://api.github.com/repos/%s/%s/releases/latest", USERNAME, REPOSITORY));
            SkJson.logger().info("Checking for updates...");

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
                SkJson.logger().info("Running a Development version, no update required.");
            }
        } catch (Exception e) {
            SkJson.logger().severe("Update check URL not found: " + e.getMessage());
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
            SkJson.logger().exception(e.getMessage(), e);
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
        SkJson.logger().info("Update scheduled, it will be applied after server restart.");
    }
}