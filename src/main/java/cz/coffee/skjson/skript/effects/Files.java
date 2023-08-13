package cz.coffee.skjson.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.skjson.api.Config;
import cz.coffee.skjson.api.FileWrapper;
import cz.coffee.skjson.json.ParsedJson;
import cz.coffee.skjson.json.ParsedJsonException;
import cz.coffee.skjson.parser.ParserUtil;
import cz.coffee.skjson.utils.Util;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

import static cz.coffee.skjson.api.Config.LOGGING_LEVEL;

/**
 * The type Files.
 */
public abstract class Files {

    /**
     * The type New.
     */
    @Name("New json file with-out content")
    @Since("2.6.2")
    @Examples({
            "on script load:",
            "\tnew json file \"plugins\\SkJson\\test.json\"",
            "",
            "\tset {_json} to json from website \"https://raw.githubusercontent.com/mozilla/node-convict/master/lerna.json\"",
            "\tnew json file \"plugins\\SkJson\\test.json\" with content {_json}",
    })
    public static class New extends Effect {

        static {
            Skript.registerEffect(New.class, "new json file %string% [(:with) content %-object%]");
        }

        private boolean withContent;
        private Expression<String> filePathInput;
        private Expression<?> unparsedValueInput;

        @Override
        protected void execute(@NotNull Event e) {
            String path = filePathInput.getSingle(e);
           if (path == null) return;
           JsonElement content;
           if (withContent) {
               Object o = unparsedValueInput.getSingle(e);
               if (o == null) content = new JsonObject();
               else content = ParserUtil.parse(o);
           } else {
               content = new JsonObject();
           }
            FileWrapper.newFile(path, content);
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return String.format("new json file %s %s", filePathInput.toString(e, debug), withContent ? "with content" + unparsedValueInput.toString(e, debug) : "");
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            withContent = parseResult.hasTag("with");
            filePathInput = (Expression<String>) exprs[0];
            unparsedValueInput = LiteralUtils.defendExpression(exprs[1]);
            if (withContent) return LiteralUtils.canInitSafely(unparsedValueInput);
            return true;
        }
    }

    /**
     * The type Edit.
     */
    @Name("Change json file contents")
    @Since("2.8.5")
    @Description({
            "Its allow to change directly keys/values in the given json file"
    })
    @Examples({
            "on script load:",
            "\tnew json file \"plugins\\SkJson\\jsons\\test.json\"",
            "",
            "command edit:",
            "\ttrigger:",
            "edit value \"command\" of json file \"plugins/SkJson/jsons/test.json\" to player's tool"
    })
    public static class Edit extends Effect {
        static {
            Skript.registerEffect(Edit.class, "edit (0:value|1:key) %string% of json file %string% to %object%");
        }

        private Expression<?> unparsedInput;
        private Expression<String> pathInput, fileInput;
        private boolean isValue;

        @Override
        protected void execute(@NotNull Event e) {
            String path = fileInput.getSingle(e);
            if (path == null) return;
            Object unparsedValue = unparsedInput.getSingle(e);
            FileWrapper.from(new File(path)).whenComplete((cFile, cThrow) -> {
                ParsedJson parsedJson = null;
                if (cFile == null) return;
                JsonElement json = cFile.get();
                String key = pathInput.getSingle(e);
                LinkedList<String> keys = Util.extractKeysToList(key, Config.PATH_VARIABLE_DELIMITER);
                if (keys == null) return;
                try {
                    parsedJson = new ParsedJson(json);
                } catch (ParsedJsonException exception) {
                    if (LOGGING_LEVEL >= 1) Util.log(exception.getLocalizedMessage());
                }

                JsonElement changeValue = ParserUtil.parse(unparsedValue);
                if (parsedJson == null) return;
                if (isValue) {
                    parsedJson.changeValue(keys, changeValue);
                } else {
                    if (unparsedValue instanceof String st) {
                        parsedJson.changeKey(keys, st);
                    } else {
                        if (LOGGING_LEVEL >= 1) Util.log("You can change key only by a string not a object-value.");
                    }
                }
                JsonElement jsonElement = parsedJson.getJson();
                FileWrapper.write(path, jsonElement);
            });
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return String.format("edit %s of %s to %s", (isValue ? "value" : "key"), fileInput.toString(e, debug), unparsedInput.toString(e, debug));
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            System.out.println("here?");
            pathInput = (Expression<String>) exprs[0];
            fileInput = (Expression<String>) exprs[1];
            unparsedInput = LiteralUtils.defendExpression(exprs[2]);
            isValue = parseResult.mark == 0;
            return LiteralUtils.canInitSafely(unparsedInput);
        }
    }

    /**
     * The type Write.
     */
    @Name("Write content to json file")
    @Since("2.8.5")
    @Description({
            "Write new data directly to json file (File will be rewritten!"
    })
    @Examples({
            "on script load:",
            "\tnew json file \"plugins\\SkJson\\jsons\\test.json\"",
            "",
            "command write:",
            "\ttrigger:",
            "write json from website \"api-website\" to json file \"plugins/SkJson/jsons/test.json\""
    })
    public static class Write extends AsyncEffect {
        static {
            Skript.registerEffect(Write.class, "write %object% to json file %string%");
        }

        private Expression<?> unparsedInput;
        private Expression<String> inputFile;

        @Override
        protected void execute(@NotNull Event e) {
            String file = inputFile.getSingle(e);
            Object unparsed = unparsedInput.getSingle(e);
            JsonElement parsedJson = ParserUtil.parse(unparsed);
            CompletableFuture.runAsync(() -> FileWrapper.write(file, parsedJson));
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return String.format("write %s to json file %s", unparsedInput.toString(e, debug), inputFile.toString(e, debug));
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            inputFile = (Expression<String>) exprs[1];
            unparsedInput = LiteralUtils.defendExpression(exprs[0]);
            getParser().setHasDelayBefore(Kleenean.TRUE);
            return LiteralUtils.canInitSafely(unparsedInput);
        }
    }

    @Name("Json file exists")
    @Description({"You can check if the json file already exists or not."})
    @Examples({
            "command FileExists:",
            "\ttrigger",
            "\t\tset {_json} to json from string \"{'A': [{'B': {}}, false, true, 10, 20, 22.22, 'A']}\" if json file \"plugins/test/main.json\" already exists"
    })
    @Since("2.8.0 - performance & clean")
    public static class CondFileExists extends Condition {
        static {
            Skript.registerCondition(CondFileExists.class,
                    "json [file] %string% exists",
                    "json [file] %string% does(n't| not) exist"
            );
        }

        private Expression<String> exprFile;
        private int line;

        public boolean isJsonFile(String path) {
            if (!path.endsWith(".json")) return false;
            final File jsonFile = new File(path);
            return jsonFile.length() > 0;
        }

        @Override
        public boolean check(@NotNull Event e) {
            final String fileString = exprFile.getSingle(e);
            if (fileString == null) return false;
            return (line == 0) == isJsonFile(fileString);
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return "json file " + exprFile.toString(e, debug) + (line == 0 ? "exists" : "not exist");
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            exprFile = (Expression<String>) exprs[0];
            line = matchedPattern;
            setNegated(line == 1);
            return true;
        }
    }

    @Name("Json is empty")
    @Description("You can check if the json empty")
    @Examples({
            "Command jsonIsEmpty",
            "\ttrigger",
            "\t\tset {_json} to json from text \"{}\"",
            "\t\tsend true if json element {_json} is empty"
    })
    @Since("2.8.0 - performance & clean")
    public static class CondJsonEmpty extends Condition {
        static {
            Skript.registerCondition(CondJsonEmpty.class,
                    "json(-| )element %json% is empty",
                    "json(-| )element %json% is(n't| not) empty"
            );
        }

        private int line;
        private Expression<JsonElement> jsonElementExpression;

        @Override
        public boolean check(@NotNull Event e) {
            final JsonElement JSON = jsonElementExpression.getSingle(e);
            boolean result = false;
            if (JSON == null) return true;
            if (JSON instanceof JsonNull) result = true;
            if (JSON instanceof JsonObject) result = JSON.getAsJsonObject().isEmpty();
            if (JSON instanceof JsonArray) result = JSON.getAsJsonArray().isEmpty();
            return (line == 0) == result;
        }

        @Override
        public @NotNull String toString(@Nullable Event e, boolean debug) {
            return "json" + jsonElementExpression.toString(e, debug) + " " + (line == 0 ? "is" : "does not") + " empty";
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
            line = matchedPattern;
            jsonElementExpression = (Expression<JsonElement>) exprs[0];
            setNegated(line == 1);
            return true;
        }
    }
}
