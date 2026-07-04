package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonObject;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.json.Parser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Create JSON file with/out content")
@Since("2.6.2, 4.1 - API UPDATE")
@Description({
        "Creates or overwrites a JSON file on disk.",
        "Without content, writes an empty `{}`.",
        "Optional configuration: `[replace=true, encoding=UTF-8]`."
})
@Examples("""
            create json file "skjson/temp.json"
            create json file "skjson/temp.json" with configuration[replace=true, encoding=UTF-8]
            create json file "skjson/temp.json" and write to it {_data} with configuration[replace=true, encoding=UTF-8]
            write {_data} to json file "skjson/temp.json"
        """)
public class EffNewFile extends AsyncEffect {

    private static final int[] CONTENT_PATTERNS = {1, 3, 5, 7};
    private static final int REVERSED_CONTENT_PATTERN = 7;

    private boolean hasContent;
    private boolean reversedArgs;
    private Expression<String> fileExpression;
    private Expression<Object> contentExpression;
    private String[] configuration;

    @Override
    protected void execute(Event event) {
        String filePath = fileExpression.getSingle(event);
        if (filePath == null) {
            return;
        }

        if (hasContent) {
            var content = contentExpression.getSingle(event);
            if (content == null) {
                return;
            }
            var parsedContent = Parser.toJson(content);
            if (parsedContent == null) {
                parsedContent = new JsonObject();
            }
            FileHandler.write(filePath, parsedContent, configuration).join();
            return;
        }

        FileHandler.write(filePath, new JsonObject(), configuration).join();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "create json file %s %s".formatted(
                fileExpression.toString(event, debug),
                hasContent ? "and write to it " + contentExpression.toString(event, debug) : ""
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        hasContent = containsPattern(CONTENT_PATTERNS, matchedPattern);
        reversedArgs = matchedPattern == REVERSED_CONTENT_PATTERN;

        if (!parseResult.regexes.isEmpty()) {
            configuration = parseResult.regexes.getFirst().group().split(",");
        }

        if (reversedArgs) {
            contentExpression = LiteralUtils.defendExpression(expressions[0]);
            fileExpression = (Expression<String>) expressions[1];
            return LiteralUtils.canInitSafely(contentExpression, fileExpression);
        }

        fileExpression = (Expression<String>) expressions[0];
        if (hasContent) {
            contentExpression = LiteralUtils.defendExpression(expressions[1]);
            return LiteralUtils.canInitSafely(contentExpression, fileExpression);
        }
        return fileExpression != null;
    }

    private static boolean containsPattern(int[] patterns, int value) {
        for (int pattern : patterns) {
            if (pattern == value) {
                return true;
            }
        }
        return false;
    }
}
