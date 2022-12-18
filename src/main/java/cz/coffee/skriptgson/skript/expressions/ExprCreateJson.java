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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.adapters.SimpleAdapter;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;

import static cz.coffee.skriptgson.utils.GsonUtils.GsonVariables.parseVariable;
import static cz.coffee.skriptgson.utils.GsonUtils.canCreate;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

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
                "[a] [new] json from request %object%"
        );
    }

    private Expression<?> toParse;
    private Expression<ItemType> itemTypeExpression;
    private int pattern;
    private boolean hasVariables;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event e) {
        GsonErrorLogger err = new GsonErrorLogger();
        if (pattern == 0) {
            if (hasVariables) {
                Object stringVariablesExpression = this.toParse.getSingle(e);
                if (stringVariablesExpression == null) return new JsonElement[0];
                JsonElement json = null;
                try {
                    json = parseVariable(stringVariablesExpression.toString(), e);
                } catch (JsonSyntaxException exception) {
                    SkriptGson.warning(err.JSON_SYNTAX);
                }
                return new JsonElement[]{json};
            } else {
                Object stringExpression = this.toParse.getSingle(e);
                if (stringExpression == null) return new JsonElement[0];
                JsonElement json = null;
                try {
                    json = JsonParser.parseString(stringExpression.toString());
                } catch (JsonSyntaxException exception) {
                    SkriptGson.warning(err.JSON_SYNTAX);
                }
                return new JsonElement[]{json};
            }
        }


        if (pattern == 1) {
            Object itemTypeExpression = this.itemTypeExpression.getSingle(e);
            JsonElement json = hierarchyAdapter().toJsonTree(itemTypeExpression);
            return new JsonElement[]{json};
        }

        if (pattern == 2) {
            Object objectExpression = this.toParse.getSingle(e);
            JsonElement json = hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(objectExpression));
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
                        SkriptGson.warning(err.PARENT_DIRECTORY_NOT_EXIST + pathExpression);
                    } else {
                        SkriptGson.warning(err.FILE_NOT_EXIST + pathExpression);
                    }
                } else {
                    SkriptGson.warning(err.JSON_SYNTAX_FILE);
                }
            }
            return new JsonElement[]{json};
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
        GsonErrorLogger err = new GsonErrorLogger();
        pattern = matchedPattern;
        hasVariables = parseResult.hasTag("with variables");
        if (pattern == 0 || pattern == 2 || pattern == 3) {
            toParse = LiteralUtils.defendExpression(exprs[0]);
            return LiteralUtils.canInitSafely(toParse);
        } else if (pattern == 1) {
            itemTypeExpression = (Expression<ItemType>) exprs[0];
            return true;
        } else if (pattern == 4) {
            SkriptGson.warning(err.ERROR_METHOD_IS_NOT_ALLOWED);
            return false;
        }
        return false;
    }
}
