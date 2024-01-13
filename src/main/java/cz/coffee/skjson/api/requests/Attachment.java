package cz.coffee.skjson.api.requests;

import com.google.gson.JsonObject;
import cz.coffee.skjson.api.FileHandler;

import java.io.File;
import java.text.DecimalFormat;

import static cz.coffee.skjson.utils.Util.fstring;

public class Attachment {

    private JsonObject getFileLength(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        final JsonObject object =  new JsonObject();

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        object.addProperty("size", df.format(size / sizeKb));
        if(size < sizeMb) {
            object.addProperty("_", "Kb");
        }
        else if(size < sizeGb) {
            object.addProperty("_", "Mb");
        }
        else if(size < sizeTerra) {
            object.addProperty("_", "Gb");
        }
        return object;
    }

    private File file;
    private String fileName;
    private String fileSize;
    private String sizeFormat;
    private String extension;
    private String path;

    public Attachment(final String file) {
        regenerate(new File(file));
    }

    public File file() {
        return file;
    }

    public String extension() {
        return extension;
    }

    public String path() {
        return path;
    }

    @Override
    public String toString() {
        return fstring("Attachment{ filename: %s, size: %s%s, path: %s }", this.fileName, this.fileSize, this.sizeFormat, path);
    }

    public void regenerate(File f) {
        String file = f.getPath();
        if (file.startsWith("*")) {
            this.file = FileHandler.searchFile(file.substring(2)).join();
        } else {
            this.file = new File(file);
        }
        JsonObject stats = getFileLength(this.file.length());
        this.fileName = this.file.getName();
        this.fileSize = stats.get("size").getAsString();
        this.sizeFormat = stats.get("_").getAsString();
        this.extension = this.file.getName().split("\\.")[1];
        this.path = this.file.getPath();
    }
}
