package cz.coffee.core.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;


public class JsonFile extends File {
    public JsonFile(@NotNull String pathname) {
        super(pathname);
        if (!pathname.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("File must end with '.json'");
        }
    }

    public JsonFile() {
        super("...");
    }

    public JsonFile(String parent, @NotNull String child) {
        super(parent, child);
        if (!child.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("File must end with '.json'");
        }
    }

    public JsonFile(File parent, @NotNull String child) {
        super(parent, child);
        if (!child.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("File must end with '.json'");
        }
    }

    public JsonFile(@NotNull URI uri) throws MalformedURLException {
        super(uri);
        if (!uri.toURL().toString().toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("File must end with '.json'");
        }
    }
}
