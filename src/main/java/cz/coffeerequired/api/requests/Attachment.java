package cz.coffeerequired.api.requests;

import com.google.gson.JsonObject;
import cz.coffeerequired.api.FileHandler;

import java.io.File;
import java.text.DecimalFormat;

public class Attachment {

    private File file;
    private String fileName;
    private String fileSize;
    private String sizeFormat;
    private String extension;
    private String path;
    public Attachment(final String file) {
        regenerate(new File(file));
    }

    private JsonObject getFileLength(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        final JsonObject object = new JsonObject();

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        object.addProperty("size", df.format(size / sizeKb));
        if (size < sizeMb) {
            object.addProperty("_", "Kb");
        } else if (size < sizeGb) {
            object.addProperty("_", "Mb");
        } else if (size < sizeTerra) {
            object.addProperty("_", "Gb");
        }
        return object;
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
        return "Attachment{ filename: " + this.fileName + ", size: " + this.fileSize + this.sizeFormat + ", path: " + path;
    }

    public void regenerate(File f) {
        String file = f.getPath();
        if (file.startsWith("*")) {
            this.file = FileHandler.search(file.substring(2), new File("/plugins/Skript/scripts")).join();
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
