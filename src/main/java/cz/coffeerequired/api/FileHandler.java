package cz.coffeerequired.api;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import cz.coffeerequired.SkJson;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public abstract class FileHandler {

    private static final File root = new File("./");

    public static CompletableFuture<JsonElement> get(File file) {
        return CompletableFuture.supplyAsync(() -> {
            if (!file.exists()) {
                return JsonNull.INSTANCE;
            }

            try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                var split = file.getName().split("\\.");
                var extension = split[split.length - 1];
                if (extension.equalsIgnoreCase("json")) {
                    try {
                        return JsonParser.parseReader(reader);
                    } catch (JsonParseException ignored) {
                        return JsonNull.INSTANCE;
                    }
                }
                return JsonNull.INSTANCE;
            } catch (IOException e) {
                SkJson.exception(e, e.getMessage());
                return JsonNull.INSTANCE;
            }
        });
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean exists(String filePath) {
        if (filePath.startsWith("~")) {
            filePath = Bukkit.getPluginManager().getPlugin("Skript").getDataFolder().getPath() + "/scripts/" + filePath.substring(1);
        }
        File file = new File(filePath);
        return file.exists();
    }

    @SuppressWarnings("DataFlowIssue")
    public static CompletableFuture<Boolean> write(String filePath, JsonElement content, String[] configuration) {

        boolean replace = false;
        int tabs = 4;
        String encoding = "UTF-8";

        if (configuration != null) {
            for (String config : configuration) {
                String[] parts = config.replaceAll("[\\[\\]]", "").trim().split("=", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                try {
                    switch (key) {
                        case "replace":
                            replace = Boolean.parseBoolean(value);
                            break;
                        case "tabs":
                            tabs = Integer.parseInt(value);
                            break;
                        case "encoding":
                            if (!value.isBlank()) {
                                encoding = value;
                            }
                            break;
                        default:
                            SkJson.warning("Unknown configuration option: " + key);
                    }
                } catch (NumberFormatException e) {
                    SkJson.warning("Invalid value for " + key + ": " + value);
                }
            }
        }

        if (filePath.startsWith("~")) {
            filePath = Bukkit.getPluginManager().getPlugin("Skript").getDataFolder().getPath() + "/scripts/" + filePath.substring(1);
        }
        if (content == null || content.isJsonNull()) content = new JsonObject();
        String finalFilePath = filePath;
        final JsonElement json = content;
        String finalEncoding = encoding;

        boolean finalReplace = replace;
        int finalTabs = tabs;
        return CompletableFuture.supplyAsync(() -> {
            final File file = new File(finalFilePath);
            var parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdir()) {
                var e = new IOException("Failed to create parent directory of " + file);
                SkJson.exception(e, e.getMessage());
            }

            if (file.exists() && !finalReplace) {
                SkJson.warning(String.format("Cannot create a file %s, file already exists", finalFilePath));
                return false;
            }

            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charset.forName(finalEncoding));
                 JsonWriter jsonWriter = new JsonWriter(writer)) {

                jsonWriter.setIndent(" ".repeat(finalTabs)); // Set indentation dynamically
                Gson gson = new Gson();
                gson.toJson(json, jsonWriter);

                return true;
            } catch (IOException e) {
                SkJson.exception(e, e.getMessage());
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

    public static CompletableFuture<File[]> walkAsFiles(final String directoryPath) {
        return CompletableFuture.supplyAsync(() -> {
            File input = new File(directoryPath);
            if (!input.isDirectory() && !input.canRead()) return new File[0];
            return Arrays.stream(Objects.requireNonNull(input.listFiles(File::isFile))).filter(f -> f.getPath().endsWith(".json")).toArray(File[]::new);
        });
    }
}
