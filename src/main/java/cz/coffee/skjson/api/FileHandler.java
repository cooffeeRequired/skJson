package cz.coffee.skjson.api;

import com.google.gson.*;
import cz.coffee.skjson.utils.Logger;
import org.bukkit.Bukkit;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static cz.coffee.skjson.utils.Logger.*;
import static cz.coffee.skjson.utils.Util.fstring;

public class FileHandler {
    private static final File rootDirectory = new File("./");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();


    /**
     * Returns content from the given file
     *
     * @param file given file path
     * @return File
     */
    public static CompletableFuture<JsonElement> get(final String file) {
        return get(new File(file));
    }

    /**
     * Returns content from the given file
     *
     * @param file given file path
     * @return File
     */
    public static CompletableFuture<JsonElement> get(final File file) {
        return CompletableFuture.supplyAsync(() -> {
            if (!file.exists()) {
                Logger.warn("File " + file + " does not exist");
                return JsonNull.INSTANCE;
            }
            try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                var split = file.getName().split("\\.");
                var ext = split[split.length - 1];
                return switch (ext) {
                    case "json" -> JsonParser.parseReader(reader);
                    case "yml", "yaml" -> {
                        var yaml = new Yaml();
                        var map = (LinkedHashMap<?, ?>) yaml.load(reader);
                        yield GSON.toJsonTree(map);
                    }
                    default -> JsonNull.INSTANCE;
                };
            } catch (Exception ex) {
                error(ex);
                return JsonNull.INSTANCE;
            }
        });
    }

    /**
     * @param future completableFuture
     * @param <T>    param of T future
     * @return T
     * @throws ExecutionException   Exception thrown when attempting to retrieve the result of a task that aborted by throwing an exception. This exception can be inspected using the getCause() method
     * @throws InterruptedException Thrown when a thread is waiting, sleeping, or otherwise occupied, and the thread is interrupted, either before or during the activity. Occasionally a method may wish to test whether the current thread has been interrupted, and if so, to immediately throw this exception. The following code can be used to achieve this effect:
     *                              if (Thread.interrupted())  // Clears interrupted status!
     *                              throw new InterruptedException();
     */
    public static <T> T await(CompletableFuture<T> future) throws ExecutionException, InterruptedException {
        return future.get();
    }

    public static CompletableFuture<Boolean> write(String filePath, JsonElement content) {
        if (filePath.startsWith("~")) {
            //noinspection DataFlowIssue
            filePath = Bukkit.getPluginManager().getPlugin("Skript").getDataFolder() + "/scripts" + "/" + filePath.substring(1);
        }
        return createOrWrite(filePath, content, true);
    }

    public static CompletableFuture<Boolean> createOrWrite(String filePath, JsonElement content) {
        if (filePath.startsWith("~")) {
            //noinspection DataFlowIssue
            filePath = Bukkit.getPluginManager().getPlugin("Skript").getDataFolder() + "/scripts" + "/" + filePath.substring(1);
        }
        return createOrWrite(filePath, content, false);
    }

    /**
     * Create new file with given content and gets true or false depending on whether it was successfully.
     *
     * @param filePath the full path for the file what will be created.
     * @param content  the json content what will be inserted into created file.
     * @param writing true or false if file will be created.
     * @return Boolean
     */
    public static CompletableFuture<Boolean> createOrWrite(final String filePath, JsonElement content, boolean writing) {
        assert filePath != null;
        if (content == null) content = new JsonObject();
        final JsonElement json = content;
        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = new File(filePath);
                var parent = file.getParentFile();
                if (parent != null && !file.getParentFile().exists() && !parent.mkdirs()) {
                    simpleError(fstring("Cannot create a directory %s", file.getParentFile().getAbsoluteFile()));
                }
                if (file.exists() && !writing) {
                    warn("Cannot create a file %s cause the file already exists.", filePath);
                    return false;
                }
                Files.writeString(file.toPath(), GSON.toJson(json));
                return true;
            } catch (Exception ex) {
                error(ex);
                return false;
            }
        });
    }

    /**
     * Will search the necessary file and return that when the file exists.
     *
     * @param filename name of the file
     * @return File
     */
    public static CompletableFuture<File> searchFile(final String filename) {
        return searchFile(filename, rootDirectory);
    }

    /**
     * Will search the necessary file and return that when the file exists.
     *
     * @param filename  name of the file
     * @param directory change root directory for that searching to this.
     * @return File
     */
    public static CompletableFuture<File> searchFile(final String filename, final File directory) {
        return CompletableFuture.supplyAsync(() -> {
            Deque<File> fileQue = new ArrayDeque<>();
            File current;
            fileQue.add(directory);
            while ((current = fileQue.pollFirst()) != null) {
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

    /**
     * walk through directory and save all file what meet the condition, so endsWith .json
     *
     * @param directoryPath root path for walking this
     * @return String[]
     */
    @SuppressWarnings("unused")
    public static CompletableFuture<String[]> walkDirectory(final String directoryPath) {
        return CompletableFuture.supplyAsync(() -> {
            File input = new File(directoryPath);
            if (!input.isDirectory() && !input.canRead()) return new String[0];

            var files = input.listFiles(File::isFile);
            if (files == null) return new String[0];

            return Arrays.stream(files)
                    .map(File::getPath)
                    .filter(file -> file.endsWith(".json"))
                    .toArray(String[]::new);
        });
    }

    public static CompletableFuture<File[]> walkDirectoryFiles(final String directoryPath) {
        return CompletableFuture.supplyAsync(() -> {
            File input = new File(directoryPath);
            if (!input.isDirectory() && !input.canRead()) return new File[0];

            var files = input.listFiles(File::isFile);
            if (files == null) return new File[0];

            return Arrays.stream(files)
                    .filter(file -> file.getPath().endsWith(".json"))
                    .toArray(File[]::new);
        });
    }
}
