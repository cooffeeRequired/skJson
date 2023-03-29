package cz.coffee.core.utils;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <<a href="http://www.gnu.org/licenses/">...</a>>.
 * <p>
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: Saturday (3/4/2023)
 */
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
