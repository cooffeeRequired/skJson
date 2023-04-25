package cz.coffee.core.utils;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        System.out.println(file);


        if (!file.exists() || !file.isFile()) return null;
        if (file.toString().endsWith(".yaml") || file.toString().endsWith(".yml")) {
            try (var reader = new BufferedReader(new FileReader(file))) {
                Yaml yaml = new Yaml();
                Object yamlMap = yaml.load(reader);
                return new GsonBuilder().create().toJsonTree(yamlMap);
            } catch (IOException | JsonSyntaxException ignored) {
                return null;
            }
        } else {
            System.out.println("HERE");
        }
        return null;
    }

    static public void write(@NotNull File file, JsonElement element, boolean async) {
        if (element == null || element instanceof JsonNull) {
            element = new JsonObject();
        }
        String dataToWrite = gson.toJson(element);
        try {
            if (!file.getParentFile().exists() && !file.getParentFile().mkdir()) {
                if (!file.createNewFile()) {
                    CompletableFuture.completedFuture(false);
                    return;
                }
            }
            if (async) {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        //noinspection ReadWriteStringCanBeUsed
                        Files.write(file.toPath(), dataToWrite.getBytes(StandardCharsets.UTF_8));
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                });
            } else {
                //noinspection ReadWriteStringCanBeUsed
                Files.write(file.toPath(), dataToWrite.getBytes(StandardCharsets.UTF_8));
                CompletableFuture.completedFuture(true);
                return;
            }
            return;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        CompletableFuture.completedFuture(false);
    }
}
