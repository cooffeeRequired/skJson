package cz.coffeerequired.api;

import com.google.gson.*;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.json.GsonParser;
import org.bukkit.Bukkit;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;


public abstract class FileHandler {

    private static final File root = new File("./");

    public static CompletableFuture<JsonElement> get(File file) {
        return CompletableFuture.supplyAsync(() -> {
            if (!file.exists()) {
                SkJson.logger().info("&cFile " + file + " doesn't exist");
                return JsonNull.INSTANCE;
            }

            try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                var split = file.getName().split("\\.");
                var extension = split[split.length - 1];
                return switch (extension) {
                    case "json" -> {
                        try {
                            yield JsonParser.parseReader(reader);
                        } catch (JsonParseException ignored) {
                            //? TODO handle that as a line loop and make a new json from it
                            yield JsonNull.INSTANCE;
                        }
                    }
                    case "yml", "yaml" -> {
                        var yaml = new Yaml();
                        @SuppressWarnings("unchecked") var map = (LinkedHashMap<String, ?>) yaml.load(reader);
                        yield GsonParser.getGson().toJsonTree(map);
                    }
                    default -> JsonNull.INSTANCE;
                };
            } catch (IOException e) {
                SkJson.logger().exception(e.getMessage(), e);
                return JsonNull.INSTANCE;
            }
        });
    }

    @SuppressWarnings("DataFlowIssue")
    public static CompletableFuture<Boolean> write(String filePath, JsonElement content, boolean replace) {
        if (filePath.startsWith("~")) {
            filePath = Bukkit.getPluginManager().getPlugin("Skript").getDataFolder().getPath() + "/scripts/" + filePath.substring(1);
        }
        if (!(content == null && content.isJsonNull())) content = new JsonObject();
        String finalFilePath = filePath;
        final JsonElement json = content;

        return CompletableFuture.supplyAsync(() -> {
            final File file = new File(finalFilePath);
            var parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdir()) {
                var e = new IOException("Failed to create parent directory of " + file);
                SkJson.logger().exception(e.getMessage(), e);
            }

            if (file.exists() && !replace) {
                SkJson.logger().warning(String.format("Cannot create a file %s, file already exists", finalFilePath));
                return false;
            }

            try {
                Files.writeString(file.toPath(), GsonParser.getGson().toJson(json));
                return true;
            } catch (IOException e) {
                SkJson.logger().exception(e.getMessage(), e);
                return false;
            }
        });
    }

    public static CompletableFuture<File> search(final String filename, File rootDirectory) {
        return CompletableFuture.supplyAsync(() -> {
            Deque<File> fileQue = new ArrayDeque<>();
            File current;
            fileQue.add(rootDirectory == null ? root : rootDirectory);
            while ((current = fileQue.poll()) != null) {
                File[] files = current.listFiles();
                if (files == null) continue;
                for (final File file : files) {
                    if (file.isDirectory()) fileQue.offerLast(file);
                    else if (file.getName().equals(filename)) return file;
                }
            }
            return null;
        });
    }

    public static CompletableFuture<String[]> walk(final String directoryPath) {
        return CompletableFuture.supplyAsync(() -> {
            File input = new File(directoryPath);
            if (!input.isDirectory() && !input.canRead()) return new String[0];

            var files = input.listFiles(File::isFile);
            if (files == null) return new String[0];

            return Arrays.stream(files)
                    .map(File::getPath)
                    .filter(f -> f.endsWith(".json"))
                    .toArray(String[]::new);
        });
    }
}
