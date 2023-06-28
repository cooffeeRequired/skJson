package cz.coffee.core.utils;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class FileUtils {

    static final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();


    static public boolean isJsonFile(String path) {
        if (!(path.endsWith(".json") || path.endsWith(".jsn"))) return false;
        final File jsonFile = new File(path);
        return jsonFile.length() > 1;
    }


    static public JsonElement get(@NotNull File file) {
        if (!file.exists() || !file.isFile()) return null;
        try (var reader = new BufferedReader(new FileReader(file))) {
            return JsonParser.parseReader(reader);
        } catch (IOException | JsonSyntaxException ignored) {
            return null;
        }
    }


    static public JsonElement getFromYaml(@NotNull File file) {
        if (!file.exists() || !file.isFile()) return null;
        if (file.toString().endsWith(".yaml") || file.toString().endsWith(".yml")) {
            try (var reader = new BufferedReader(new FileReader(file))) {
                Yaml yaml = new Yaml();
                Object yamlMap = yaml.load(reader);
                return new GsonBuilder().create().toJsonTree(yamlMap);
            } catch (IOException | JsonSyntaxException ignored) {
                return null;
            }
        }
        return null;
    }


    static public CompletableFuture<Boolean> write(@NotNull File file, JsonElement element) {
        if (element == null || element instanceof JsonNull) {
            element = new JsonObject();
        }
        String dataToWrite = gson.toJson(element);
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!file.getParentFile().exists() && !file.getParentFile().mkdir()) {
                    throw new IOException("Cannot create directory: " + file.getParentFile().getAbsolutePath());
                }

                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        return false;
                    }
                }


                if (!file.exists()) {
                    return false;
                } else {
                    Files.writeString(file.toPath(), dataToWrite);
                }
                return true;
            } catch (IOException exception) {
                exception.printStackTrace();
                return false;
            }
        });
    }
}
