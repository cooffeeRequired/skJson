package cz.coffeerequired.skript.json;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonObject;
import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.FileHandler;
import cz.coffeerequired.api.json.GsonParser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Create JSON file with/out content")
@Since("2.6.2, 4.1 - API UPDATE")
@Description("Allows us to create a JSON file that can either be initialized with some content or remain empty.")
@Examples("""
    create json file "content.json"
    create json file "content.json" with configuration[replace=true, encoding=UTF-8]
    create json file "content.json" and write to it <any>
    create json file "content.json" and write to it <any> with configuration[replace=true, encoding=UTF-8]
""")
public class EffNewFile extends AsyncEffect {

    private boolean hasContent;
    private Expression<String> fileExpression;
    private Expression<Object> contentExpression;
    private String[] configuration;


    @Override
    protected void execute(Event event) {
        String filePath = fileExpression.getSingle(event);
        if (filePath == null) return;

        if (hasContent) {
            var content = contentExpression.getSingle(event);
            if (content == null) return;
            var parsedContent = GsonParser.toJson(content);
            if (parsedContent == null) parsedContent = new JsonObject();
            FileHandler.write(filePath, parsedContent, configuration).join();
            return;
        }

        FileHandler.write(filePath, new JsonObject(), configuration).join();
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return String.format("create json file %s %s", fileExpression.toString(event, b), hasContent ? "and write to it"  + contentExpression.toString(event, b) : "");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        hasContent = i == 1;

        if (!parseResult.regexes.isEmpty()) {
            configuration = parseResult.regexes.getFirst().group().split(",");
        }

        fileExpression = (Expression<String>) expressions[0];
        if (hasContent) {
            contentExpression = LiteralUtils.defendExpression(expressions[1]);
            return LiteralUtils.canInitSafely(contentExpression);
        }
        return fileExpression != null;
    }
}
