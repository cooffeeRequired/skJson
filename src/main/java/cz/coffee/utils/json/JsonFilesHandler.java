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
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.utils.ErrorHandler.FILE_NOT_EXIST;
import static cz.coffee.utils.ErrorHandler.Level.WARNING;
import static cz.coffee.utils.ErrorHandler.sendMessage;
import static cz.coffee.utils.SimpleUtil.printPrettyStackTrace;

@SuppressWarnings("unused")
public class JsonFilesHandler {

    boolean canBeForced = false;

    public JsonFilesHandler(boolean canBeForced) {
        this.canBeForced = canBeForced;
    }

    public JsonFilesHandler() {
    }

    public static boolean canBeCreated(@NotNull File file) {
        return !file.getParentFile().exists();
    }

    public static void asyncWriting(Object dataJson, String strFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().create();
        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Files.writeString(new File(strFile).toPath(), gson.toJson(dataJson));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
        );
    }
    public JsonElement readFile(@NotNull String inputString) {
        JsonElement element = null;
        File file = new File(inputString);
        try (var ptr = new JsonReader(new FileReader(file))) {
            element = JsonParser.parseReader(ptr);
        } catch (IOException | JsonSyntaxException exception) {
            if (exception instanceof IOException) {
                if (!file.exists()) sendMessage(FILE_NOT_EXIST + " ... " + inputString, WARNING);
            } else {
                sendMessage((exception).getMessage(), WARNING);
            }
        }
        return element;
    }

    public void writeFile(File file, Object data, boolean async) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().enableComplexMapKeySerialization().create();
        boolean isExist = file.exists(), isCreated = false;

        try {
            if (!file.getParentFile().exists()) {
                if (file.getParentFile().mkdirs()) isCreated = file.createNewFile();
            } else {
                isCreated = file.createNewFile();
            }
            if (!(isCreated || isExist)) return;
            if (data == null || data instanceof JsonNull) data = new JsonObject();
            if (async) {
                asyncWriting(data, file.toString());
            } else {
                Files.writeString(file.toPath(), gson.toJson(data));
            }
        } catch (IOException exception) {
            printPrettyStackTrace(exception, 5);
        }
    }
}
