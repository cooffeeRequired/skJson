package cz.coffee.skjson.api;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.TimingLogHandler;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.OpenCloseable;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.utils.Util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright coffeeRequired nd contributors
 * <p>
 * Created: čtvrtek (13.07.2023)
 */
public class SkriptLoaderFile {

    private static Set<File> toggleFiles(File folder, boolean enable) throws IOException {
        FileFilter filter = enable ? ScriptLoader.getDisabledScriptsFilter() : ScriptLoader.getLoadedScriptsFilter();

        Set<File> changed = new HashSet<>();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                changed.addAll(toggleFiles(file, enable));
            } else {
                if (filter.accept(file)) {
                    String fileName = file.getName();
                    changed.add(FileUtils.move(
                            file,
                            new File(file.getParentFile(), enable ? fileName.substring(ScriptLoader.DISABLED_SCRIPT_PREFIX_LENGTH) : ScriptLoader.DISABLED_SCRIPT_PREFIX + fileName),
                            false
                    ));
                }
            }
        }

        return changed;
    }
    private final File folder;

    public SkriptLoaderFile(File testFolder) throws IOException {
        folder = testFolder;
    }

    public void load() {
        try (var handler = new RetainingLogHandler().start()) {
            try (var skTiming = new TimingLogHandler()) {
                ScriptLoader.loadScripts(folder, OpenCloseable.combine(handler, skTiming))
                        .thenAccept(scriptInfo -> {
                            if (!handler.hasErrors()) {
                                Util.log("Starting delayed SkJson tests");
                            } else {
                                handler.printErrors();
                                handler.stop();
                            }
                        });
            }
        }
    }

    public void unload() {
        var loaded = new HashSet<>(ScriptLoader.getLoadedScripts().stream().filter(script -> script.getConfig().getFileName().contains("skjson.sk")).toList());
        ScriptLoader.unloadScripts(loaded);
    }
}
