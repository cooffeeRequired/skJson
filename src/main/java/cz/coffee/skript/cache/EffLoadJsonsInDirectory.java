package cz.coffee.skript.cache;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import static cz.coffee.SkJson.JSON_STORAGE;

@Name("link and load all json files from given folder")
@Since("2.8.6")
@Description({"Handle all files from folder"})
@Examples({"        load json files from \"plugins/raw/\" and save it in \"raw\"\n" +
        "        loop entries of json \"raw\":\n" +
        "            send loop-element\n" +
        "        "})

public class EffLoadJsonsInDirectory extends AsyncEffect {

    static {
        Skript.registerEffect(EffLoadJsonsInDirectory.class, "load json files from %string% and save it in %string%");
    }

    private Expression<String> expressionPathDirectory, expressionCacheDirectory;

    @Override
    protected void execute(@NotNull Event event) {
        String pathDirectory = expressionPathDirectory.getSingle(event);
        String cacheDirectory = expressionCacheDirectory.getSingle(event);

        // walk through directory

        final Map<JsonElement, File> map = new WeakHashMap<>();

        if (pathDirectory == null) return;
        if (cacheDirectory == null) cacheDirectory = pathDirectory;
        final File folder = new File(pathDirectory); // directory to walk
        String finalCacheDirectory = cacheDirectory;
        final JsonObject jsonFiles = new JsonObject();
        Stream.of(Objects.requireNonNull(folder.listFiles(f -> f.getName().endsWith(".json"))))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .toList().forEach(potentialJsonFile -> {
                    File potentialFile  = new File(pathDirectory + "/" + potentialJsonFile);
                    jsonFiles.add(potentialJsonFile, FileUtils.get(potentialFile));
                });

        map.put(jsonFiles, folder);
        JSON_STORAGE.put(finalCacheDirectory, map);
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "load json file from " + expressionPathDirectory.toString(event, b) + " and save it in " + expressionCacheDirectory.toString(event, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        expressionPathDirectory = (Expression<String>) expressions[0];
        expressionCacheDirectory = (Expression<String>) expressions[1];
        return true;
    }
}
