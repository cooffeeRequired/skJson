package cz.coffee.skriptgson.filemanager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonErrorLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageConfigurator extends GsonErrorLogger {

    public static String responseHandler = "response-handler";


    private final File configPath = new File(SkriptGson.getInstance().getDataFolder().toURI());
    private final File configFilePath = new File(SkriptGson.getInstance().getDataFolder() + File.separator + "config.yml");
    private final YamlProcessor yamlProcessor = new YamlProcessor(configFilePath);


    public void create(File file) {

    }

    public void setValue(String key, Object data) {
        try (FileInputStream is = new FileInputStream(configFilePath)) {
            Map<String, Object> map = yamlProcessor.process().load(is);
            if (map.containsKey(key)) {
                map.remove(key);
                map.put(key, data);
                processWrite(new Gson().toJsonTree(map).getAsJsonObject());
            }
        } catch (IOException exception) {
            sendErrorMessage(exception.getMessage(), ErrorLevel.WARNING);
        }
    }

    public Object value(String key) {
        try (FileInputStream is = new FileInputStream(configFilePath)) {
            Map<String, Object> map = yamlProcessor.process().load(is);
            if (map.containsKey(key)) {
                return map.get(key);
            }
        } catch (IOException exception) {
            sendErrorMessage(exception.getMessage(), ErrorLevel.WARNING);
        }
        return null;
    }

    public void create() {
        boolean created = false;
        if (!configPath.exists()) {
            try {
                if (configPath.mkdir()) {
                    if (!(configFilePath.exists())) {
                        created = configFilePath.createNewFile();
                    }
                }
            } catch (IOException ioe) {
                sendErrorMessage(ioe.getMessage(), ErrorLevel.WARNING);
            }
        } else {
            try {
                if (!(configFilePath.exists())) {
                    created = configFilePath.createNewFile();
                }
            } catch (IOException ioException) {
                sendErrorMessage(ioException.getMessage(), ErrorLevel.WARNING);
            }
        }
        if (configFilePath.exists() && configFilePath.length() < 1) {
            writeDefault();
        }
    }

    private void processWrite(JsonObject object) {

        boolean append = configFilePath.length() > 1;

        try {
            Map<String, Object> map;
            if (append) {
                map = yamlProcessor.process().load(new FileInputStream(configFilePath));
                map.putAll(new Gson().fromJson(object, HashMap.class));
            } else {
                map = new HashMap<>(new Gson().fromJson(object, HashMap.class));
            }
            try (PrintWriter pw = new PrintWriter(configFilePath)) {
                yamlProcessor.process().dump(map, pw);
            } catch (IOException ioException) {
                sendErrorMessage(ioException.getMessage(), ErrorLevel.WARNING);
            }
        } catch (IOException ioException) {
            sendErrorMessage(ioException.getMessage(), ErrorLevel.WARNING);
        }
    }

    public void write(String id, Object value) {
        final JsonObject object = new JsonObject();
        object.addProperty(id, new Gson().toJson(value));
        processWrite(object);
    }

    public void write(String id, List<?> values) {
        final JsonObject object = new JsonObject();
        object.add(id, new Gson().toJsonTree(values));
        processWrite(object);
    }


    public void writeDefault() {
        write("version", 1.0);
        write(responseHandler, true);
        write("response-type", List.of("Reqn", "SkriptWebApi", "skript-reflect"));
        write("auto-update", false);
        write("create-examples", true);
    }

    public void writeComments(String ...comments) {
        try {
            if (comments != null) {
                for (String c : comments) {
                    String comment = "\n # " + c;
                    Files.write(configFilePath.toPath(), comment.getBytes(), StandardOpenOption.APPEND);
                }
            }
        } catch (IOException ioException) {
            sendErrorMessage(ioException.getMessage(), ErrorLevel.WARNING);
        }
    }
}
