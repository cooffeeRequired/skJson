package cz.coffee.skjson.skript.cache;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skjson.SkJson;
import cz.coffee.skjson.api.Cache.JsonCache;
import cz.coffee.skjson.api.Cache.JsonWatcher;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.FileHandler;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static cz.coffee.skjson.api.ConfigRecords.LOGGING_LEVEL;
import static cz.coffee.skjson.api.FileHandler.await;
import static cz.coffee.skjson.utils.Logger.error;
import static cz.coffee.skjson.utils.Logger.simpleError;

public abstract class JsonCacheInstance {
    @Name("Json storage")
    @Description("You can create virtual json in memory")
    @Examples({
            "on script load:",
            "\tcreate new json storage named \"json-storage\"",
            "\tsend json \"json-storage\""
    })
    @Since("2.9")
    public static class JsonNonFileStorage extends Effect {

        static {
            SkJson.registerEffect(JsonNonFileStorage.class, "[create] new json storage [named] %string%");
        }

        private Expression<String> nameOfStorageExp;

        @Override
        protected void execute(@NotNull Event e) {
            String nameOfStorage = nameOfStorageExp.getSingle(e);
            if (nameOfStorage == null)
                if (LOGGING_LEVEL > 1) simpleError("The name of the storage is not specified.");
            JsonCache<String, JsonElement, File> cache = Config.getCache();
            cache.addValue(nameOfStorage, new JsonObject(), new File("Undefined"));
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return "create new json storage named " + nameOfStorageExp.toString(e, debug);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            nameOfStorageExp = (Expression<String>) exprs[0];
            return true;
        }
    }


    @Name("Link json file with defined cache.")
    @Description("You can works with the cache instead of reopening the file again & again.")
    @Examples({
            "on load:",
            "\tlink json file \"<path to file>\" as \"mine.id\"",
            "\tlink json file \"<path to file>\" as \"mine.id\" and make json watcher listen"
    })
    @Since("2.8.0 - performance & clean")
    public static class LinkFile extends Effect {

        static {
            SkJson.registerEffect(LinkFile.class, "link [json] file %string% as %string% [(:and make) [[json] watcher] listen]");
        }

        private Expression<String> exprFileString, expressionID;
        private boolean asAlive;


        @Override
        protected void execute(@NotNull Event e) {
            String fileString = exprFileString.getSingle(e);
            String id = expressionID.getSingle(e);
            if (id == null || fileString == null) return;
            JsonCache<String, JsonElement, File> cache = Config.getCache();
            File file = new File(fileString);
            FileHandler.get(file).whenComplete((json, cThrow) -> {
                if (json == null) return;
                cache.addValue(id, json, file);
                if (asAlive) if (!JsonWatcher.isRegistered(file)) JsonWatcher.register(id, file);
            });
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return "link json file " + exprFileString.toString(e, debug) + " as " + expressionID.toString(e, debug) + (asAlive ? " and make json watcher listen" : "");
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
            asAlive = parseResult.hasTag("and make");
            exprFileString = (Expression<String>) exprs[0];
            expressionID = (Expression<String>) exprs[1];
            return true;
        }
    }

    @Name("link and load all json files from given folder")
    @Since("2.9 [30.8.2023] - add support for json watcher to all files")
    @Description({"Handle all files from folder"})
    @Examples({
            "load json files from \"plugins/raw/\" and save it in \"raw\"",
            "\tloop values of json \"raw\":",
            "\t\tsend json-value",
            "*Since 2.9 [30.8.2023]*",
            "",
            "load json files from \"plugins/SkJson/jsons\" and let json watcher listen to all with save in \"raw\"",
            "\tloop values of json \"raw\":",
            "\t\tsend json-value",
    })
    public static class AllJsonFromDirectory extends AsyncEffect {

        static {
            SkJson.registerEffect(AllJsonFromDirectory.class,
                    "[:async] load json files from %string% and save it in %string%",
                    "[:async] load json files from %string% and let json watcher listen to all with save it in %string%"
            );
        }

        private Expression<String> expressionPathDirectory, expressionCacheDirectory;
        private boolean letWatching;
        private boolean isAsynchronous;

        @Override
        protected void execute(@NotNull Event e) {
            String pathDirectory = expressionPathDirectory.getSingle(e);
            String cacheDirectory = expressionCacheDirectory.getSingle(e);

            if (pathDirectory == null) return;
            if (cacheDirectory == null) cacheDirectory = pathDirectory;

            final File folder = new File(pathDirectory); // directory to walk
            String finalCacheDirectory = cacheDirectory;

            final JsonObject jsonFiles = new JsonObject();
            File[] files = folder.listFiles(f -> f.getName().endsWith(".json"));
            if (files == null || files.length == 0) return;

            if (isAsynchronous) {
                CompletableFuture.runAsync(() -> StreamOf(pathDirectory, folder, finalCacheDirectory, jsonFiles, files));
            } else {
                StreamOf(pathDirectory, folder, finalCacheDirectory, jsonFiles, files);
            }
        }

        private void StreamOf(String pathDirectory, File folder, String finalCacheDirectory, JsonObject jsonFiles, File[] files) {
            Stream.of(files)
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .toList().forEach(potentialFile -> {
                        File potential = new File(pathDirectory + "/" + potentialFile);
                        CompletableFuture<JsonElement> ct = FileHandler.get(potential);
                        JsonElement json = ct.join();
                        if (letWatching) {
                            String parentID = finalCacheDirectory + ";" + potentialFile;
                            if (!JsonWatcher.isRegistered(potential))
                                JsonWatcher.register(potentialFile, potential, parentID);
                        }
                        jsonFiles.add(potentialFile, json);
                    });
            JsonCache<String, JsonElement, File> cache = Config.getCache();
            cache.addValue(finalCacheDirectory, jsonFiles, folder);
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            assert e != null;
            return "load json file from " + expressionPathDirectory.toString(e, debug) + " and save it in " + expressionCacheDirectory.toString(e, debug);

        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull SkriptParser.ParseResult parseResult) {
            expressionPathDirectory = (Expression<String>) exprs[0];
            expressionCacheDirectory = (Expression<String>) exprs[1];
            letWatching = matchedPattern == 1;
            isAsynchronous = parseResult.hasTag("async");
            return true;
        }
    }

    @Name("Json file is cached")
    @Description("Check if the file for given id is cached")
    @Examples({
            "on load:",
            "\tsend true if json \"test\" is linked "
    })
    @Since("2.8.0 - performance & clean")
    public static class CondJsonIsCached extends Condition {
        static {
            SkJson.registerCondition(CondJsonIsCached.class,
                    "json %string% is (load|linked)",
                    "json %string% is(n't| not) (load|linked)"
            );
        }

        private Expression<String> exprId;
        private int line;

        @Override
        public boolean check(@NotNull Event event) {
            final String id = exprId.getSingle(event);
            return (line == 0) == Config.getCache().containsKey(id);
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            assert event != null;
            return "cached json " + exprId.toString(event, b) + (line == 0 ? " is " : " is not") + "loaded";
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
            line = i;
            setNegated(i == 1);
            exprId = (Expression<String>) expressions[0];
            return true;
        }
    }

    @Name("Save cached json to file")
    @Description("It's allow save cached json back to the file")
    @Examples({
            "on unload:",
            "\tsave json \"test\"",
            "\tsave all jsons"
    })
    @Since("2.8.0 - performance & clean")
    public static class SaveCache extends AsyncEffect {
        static {
            SkJson.registerEffect(SaveCache.class,
                    "save json %string%",
                    "save all jsons"
            );
        }

        private int line;
        private Expression<String> externalExprID;

        @Override
        protected void execute(@NotNull Event e) {
            CompletableFuture.runAsync(() -> {
                JsonCache<String, JsonElement, File> cache = Config.getCache();
                if (line == 0) {
                    String id = externalExprID.getSingle(e);
                    if (cache.containsKey(id)) {
                        ConcurrentHashMap<JsonElement, File> jsonMap = cache.get(id);
                        jsonMap.forEach((json, file) -> {
                            if (file.getName().equals("Undefined")) {
                                simpleError("You cannot save virtual storage of json.");
                                return;
                            }
                            try {
                                await(FileHandler.createOrWrite(file.toString(), json));
                            } catch (ExecutionException | InterruptedException ex) {
                                error(ex, null, getParser().getNode());
                            }
                        });
                    }
                } else {
                    cache.forEach((key, map) -> map.forEach((json, file) -> {
                        if (!file.getName().equals("Undefined")) {
                            try {
                                await(FileHandler.createOrWrite(file.toString(), json));
                            } catch (ExecutionException | InterruptedException ex) {
                                error(ex, null, getParser().getNode());
                            }
                        }
                    }));
                }
            });
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            if (line == 0) {
                assert e != null;
                return "save cached json " + externalExprID.toString(e, debug);
            } else return "save all cached jsons";
        }

        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            line = matchedPattern;
            if (line == 0) {
                externalExprID = (Expression<String>) exprs[0];
            }
            return true;
        }
    }

    @Name("Unlink or unload json file from cache")
    @Description("You can unload the json file.")
    @Examples({
            "on load:",
            "\tunlink json \"mine.id\""
    })
    @Since("2.8.0 - performance & clean")
    public static class UnlinkFile extends Effect {
        static {
            SkJson.registerEffect(UnlinkFile.class, "unlink json %string%");
        }

        private Expression<String> exprID;

        @Override
        protected void execute(@NotNull Event e) {
            String id = exprID.getSingle(e);
            if (id == null) return;
            cz.coffee.skjson.api.Cache.JsonCache<String, JsonElement, File> cache = Config.getCache();
            if (cache.containsKey(id)) {
                AtomicReference<File> finalFile = new AtomicReference<>();
                cache.get(id).forEach((json, file) -> finalFile.set(file));
                if (JsonWatcher.isRegistered(finalFile.get())) JsonWatcher.unregister(finalFile.get());
                cache.remove(id);
            }

        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            assert event != null;
            return "unlink json " + exprID.toString(event, b);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
            exprID = (Expression<String>) expressions[0];
            return true;
        }
    }

    @Name("Get cached json")
    @Description({"You can get json from cache storage by key defined by you"})
    @Examples({"on script load:",
            "\tset {_json} to json \"your\"",
            "\tsend {_json} with pretty print"
    })
    @Since("2.8.0 - performance & clean")
    public static class GetCachedJson extends SimpleExpression<JsonElement> {

        static {
            SkJson.registerExpression(GetCachedJson.class, JsonElement.class, ExpressionType.SIMPLE,
                    "json %string%",
                    "all cached jsons"
            );
        }

        private Expression<String> storedKeyExpr;
        private int line;


        @Override
        protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
            String storedKey = storedKeyExpr.getSingle(e);
            cz.coffee.skjson.api.Cache.JsonCache<String, JsonElement, File> cache = Config.getCache();
            if (line == 0) {
                if (storedKey != null) {
                    if (cache.containsKey(storedKey)) {
                        AtomicReference<JsonElement> element = new AtomicReference<>();
                        cache.get(storedKey).forEach((json, file) -> element.set(json));
                        return new JsonElement[]{element.get()};
                    }
                }
            } else if (line == 1) {
                ArrayList<JsonElement> finalElements = new ArrayList<>();
                cache.forEach((key, map) -> map.forEach((json, file) -> finalElements.add(json)));
                return finalElements.toArray(JsonElement[]::new);
            }

            return new JsonElement[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public @NotNull Class<JsonElement> getReturnType() {
            return JsonElement.class;
        }

        @Override
        public @NotNull String toString(@Nullable Event event, boolean b) {
            if (line == 0) {
                assert event != null;
                return "get cached json " + storedKeyExpr.toString(event, b);
            } else {
                return "all cached jsons";
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            line = matchedPattern;
            storedKeyExpr = (Expression<String>) exprs[0];
            return true;
        }
    }

}
