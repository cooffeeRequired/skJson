/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.utils.ErrorHandler.*;
import static cz.coffee.utils.ErrorHandler.Level.WARNING;

@SuppressWarnings("unused")
public class JsonFilesHandler {

    boolean canBeForced = false;

    public JsonFilesHandler(boolean canBeForced) {
        this.canBeForced = canBeForced;
    }

    public JsonFilesHandler() {}

    public static boolean canBeCreated(@NotNull File file) {
        return !file.getParentFile().exists();
    }

    /**
     * Create new json file with data, otherwise without as empty object
     *
     * @param inputString {@link String}
     * @param inputData   {@link JsonElement}
     * @param forcing     {@link Boolean}
     */
    public void newFile(@NotNull String inputString, Object inputData, boolean forcing, boolean async) {
        File file = new File(inputString);
        if (file.exists()) {
            if (file.length() < 0x01) sendMessage(JSON_FILE_EXISTS, WARNING);
            return;
        }
        if (forcing) {
            try {
                Path fileParents = Paths.get(inputString);
                if (!Files.exists(fileParents.getParent())) {
                    try {
                        Files.createDirectories(fileParents.getParent());
                    } catch (IOException ioException) {
                        sendMessage(PARENT_DIRECTORY_EXISTS, WARNING);
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (inputData == null || inputData instanceof JsonNull) {
            inputData = new JsonObject();
        }

        if (async) {
            asyncWriting(inputData, inputString);
        } else {
            try {
                Files.writeString(file.toPath(), new Gson().toJson(inputData));
            } catch (IOException exception) {
                if (canBeCreated(file)) sendMessage(PARENT_DIRECTORY_NOT_EXIST, WARNING);
                sendMessage(exception.getMessage(), WARNING);
            }
        }
    }

    public JsonElement readFile(@NotNull String inputString) {
        JsonElement element = null;
        File file = new File(inputString);
        try (var ptr = new JsonReader(new FileReader(file))) {
            element = JsonParser.parseReader(ptr);
        } catch (IOException | JsonSyntaxException exception) {
            if (exception instanceof IOException) {
                if(!file.exists()) sendMessage(FILE_NOT_EXIST + inputString, WARNING);
            } else {
                sendMessage((exception).getMessage(), WARNING);
            }
        }
        return element;
    }

    public static void asyncWriting(Object dataJson, String strFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().create();
        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Files.writeString(new File(strFile).toPath(),gson.toJson(dataJson));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
        );
    }

    public void writeFile(File file, Object data, boolean async) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().enableComplexMapKeySerialization().create();
        try {
            if (async) {
                asyncWriting(data, file.toString());
            } else {
                Files.writeString(file.toPath(), gson.toJson(data));
            }
        } catch (IOException exception) {
            sendMessage(exception.getMessage(), WARNING);
        }
    }
}
