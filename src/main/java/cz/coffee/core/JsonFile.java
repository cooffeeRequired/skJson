package cz.coffee.core;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

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
 * Created: pátek (17.03.2023)
 */

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
