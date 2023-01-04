package cz.coffee.skriptgson.filemanager;

import com.google.gson.JsonElement;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static cz.coffee.skriptgson.SkriptGson.gsonAdapter;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.sendErrorMessage;

public class DefaultConfigFolder {

    private static Yaml getYaml() {
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        return new Yaml(options);
    }

    public static void create() {
        File dataFolder = new File(SkriptGson.getInstance().getDataFolder().toURI());
        if (dataFolder.exists()) {
            try {
                if (!(new File(dataFolder + File.separator + "config.yml").exists())) {
                    new File(dataFolder + File.separator + "config.yml").createNewFile();
                    if (writeConfigDefault()) {
                        sendErrorMessage("config.yml was created", GsonErrorLogger.ErrorLevel.INFO);
                    }
                }
            } catch (IOException ignored) {
            }
        } else {
            if (dataFolder.mkdir()) {
                try {
                    if (!(new File(dataFolder + File.separator + "config.yml").exists())) {
                        new File(dataFolder + File.separator + "config.yml").createNewFile();
                        if (writeConfigDefault()) {
                            sendErrorMessage("config.yml was created", GsonErrorLogger.ErrorLevel.INFO);
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static boolean writeConfigDefault() {
        final File file = new File(SkriptGson.getInstance().getDataFolder() + File.separator + "config.yml");
        final Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("version", 1.0);
        dataMap.put("results-handler", true);
        dataMap.put("auto-update", false);

        try (PrintWriter pw = new PrintWriter(file)) {
            getYaml().dump(dataMap, pw);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static Object readConfigRecords(String key) {
        final File file = new File(SkriptGson.getInstance().getDataFolder() + File.separator + "config.yml");
        try (FileInputStream is = new FileInputStream(file)) {
            JsonElement output = gsonAdapter.toJsonTree(getYaml().load(is));
            if (output.getAsJsonObject().has(key)) {
                return output.getAsJsonObject().get(key);
            }
        } catch (IOException exception) {
            return null;
        }
        return null;
    }

}
