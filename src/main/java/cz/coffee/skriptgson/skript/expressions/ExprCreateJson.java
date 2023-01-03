package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.btk5h.reqn.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import cz.coffee.skriptgson.adapters.Adapters;
import cz.coffee.skriptgson.filemanager.DefaultConfigFolder;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;

import static cz.coffee.skriptgson.SkriptGson.gsonAdapter;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.ERROR;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.ErrorLevel.WARNING;
import static cz.coffee.skriptgson.utils.GsonErrorLogger.*;
import static cz.coffee.skriptgson.utils.GsonUtils.GsonVariables.parseVariable;
import static cz.coffee.skriptgson.utils.GsonUtils.canCreate;


@Name("New JSON from bunch sources (Text/File/Request)")
@Description({})
@Examples({"command example [<string>]:",
        "\ttrigger:",
        "\t\tsend new json from arg-1",
        "\t\tsend new json from player's location",
        "\t\tsend new json from player's tool",
        "\t\tsend new json from text \"{'title': 'skript-gson', 'number': 2.0}\"",
        "",
        "on load:",
        "\tset {_n} to 9.12",
        "\tsend new json from text \"{'number': ${_n}\" with variables"
})
@Since("2.0.0")


public class ExprCreateJson extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprCreateJson.class, Object.class, ExpressionType.COMBINED,
                "[a] [new] json from (text|string) %object% [(:with variables)]",
                "[a] [new] json from %itemstack%",
                "[a] [new] json from %object%",
                "[a] [new] json from file [(:relative)] [path] %string%",
                "[a] [new] json from request %httpresponse%"
        );
    }

    private Expression<?> toParse;
    private Expression<ItemType> itemTypeExpression;
    private int pattern;
    private boolean hasVariables, last;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        if (pattern == 0) {
            if (hasVariables) {
                Object stringVariablesExpression = this.toParse.getSingle(e);
                if (stringVariablesExpression == null) return new JsonElement[0];
                JsonElement json = null;
                try {
                    json = parseVariable(stringVariablesExpression.toString(), e);
                } catch (JsonSyntaxException exception) {
                    sendErrorMessage(JSON_SYNTAX, WARNING);
                }
                return new JsonElement[]{json};
            } else {
                Object stringExpression = this.toParse.getSingle(e);
                if (stringExpression == null) return new JsonElement[0];
                JsonElement json = null;
                try {
                    json = JsonParser.parseString(stringExpression.toString());
                } catch (JsonSyntaxException exception) {
                    sendErrorMessage(JSON_SYNTAX, WARNING);
                }
                return new JsonElement[]{json};
            }
        }

        if (pattern == 1) {
            Object itemTypeExpression = this.itemTypeExpression.getSingle(e);
            JsonElement json = gsonAdapter.toJsonTree(itemTypeExpression);
            return new JsonElement[]{json};
        }

        if (pattern == 2) {
            Object objectExpression = this.toParse.getSingle(e);
            JsonElement json = gsonAdapter.toJsonTree(Adapters.toJson(objectExpression));
            return new JsonElement[]{json};
        }

        if (pattern == 3) {
            Object pathExpression = this.toParse.getSingle(e);
            JsonElement json = null;
            if (pathExpression == null) return new JsonElement[0];
            try (var protectedReader = new JsonReader(new FileReader(pathExpression.toString()))) {
                json = JsonParser.parseReader(protectedReader);
            } catch (JsonSyntaxException | IOException exception) {
                if (exception instanceof JsonSyntaxException) {
                    if (!canCreate(pathExpression)) {
                        sendErrorMessage(PARENT_DIRECTORY_NOT_EXIST + pathExpression, WARNING);
                    } else {
                        sendErrorMessage(FILE_NOT_EXIST + pathExpression, WARNING);
                    }
                } else {
                    sendErrorMessage(JSON_SYNTAX_FILE, WARNING);
                }
            }
            return new JsonElement[]{json};
        }

        if (pattern == 4) {
            Object httpResponse = this.toParse.getSingle(e);
            if (httpResponse == null) return new JsonElement[0];
            try {
                JsonElement element = JsonParser.parseString(((HttpResponse) httpResponse).getBody());
                return new JsonElement[]{element};
            } catch (JsonSyntaxException exception) {
                sendErrorMessage("response is not json", ERROR);
            }
        }

        return new JsonElement[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }


    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        if (pattern == 0)
            return "new json from string '" + toParse.toString(e, debug) + "'" + (hasVariables ? "with variables" : "");
        else if (pattern == 3)
            return "new json from file '" + toParse.toString(e, debug) + "'";
        else if (pattern == 2)
            return "new json from '" + toParse.toString(e, debug) + "'";
        else if (pattern == 1)
            return "new json from item '" + itemTypeExpression.toString(e, debug) + "'";
        else
            return "new json from request '" + toParse.toString(e, debug) + "'";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        hasVariables = parseResult.hasTag("with variables");
        if (pattern == 0 || pattern == 2 || pattern == 3) {
            toParse = LiteralUtils.defendExpression(exprs[0]);
            return LiteralUtils.canInitSafely(toParse);
        } else if (pattern == 1) {
            itemTypeExpression = (Expression<ItemType>) exprs[0];
            return true;
        } else if (pattern == 4) {
            Object output = DefaultConfigFolder.readConfigRecords("results-handler");
            if (output.toString().equals("true")) {
                last = parseResult.hasTag("last request");
                toParse = LiteralUtils.defendExpression(exprs[0]);
                return LiteralUtils.canInitSafely(toParse);
            } else {
                sendErrorMessage("You can't handle result, because your config for results-handler is false", WARNING);
                sendErrorMessage("Try change the value 'results-handler' to &aTrue&r and restart server", WARNING);
                return false;
            }
        }
        return false;
    }
}
