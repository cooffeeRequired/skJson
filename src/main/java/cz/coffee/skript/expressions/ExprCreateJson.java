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
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.skript.expressions;

import ch.njol.skript.Skript;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cz.coffee.adapter.DefaultAdapters;
import cz.coffee.utils.json.JsonFilesHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.regex.Pattern;

import static cz.coffee.utils.ErrorHandler.Level.ERROR;
import static cz.coffee.utils.ErrorHandler.Level.WARNING;
import static cz.coffee.utils.ErrorHandler.sendMessage;
import static cz.coffee.utils.config.Config._REQUEST_HANDLER;
import static cz.coffee.utils.json.JsonUtils.parseVariable;

@Name("New json from bunch sources (Text/File/Request)")
@Description({
        "You are able a create new json from bunch of sources",
        "String/text any Skript object, and from Web/Url request"
})
@Examples({"command example [<string>]:",
        "\ttrigger:",
        "\t\tsend json from arg-1",
        "\t\tsend json from player's location",
        "\t\tsend json from player's tool",
        "\t\tsend json from text \"{'title': 'skJson', 'number': 2.0}\"",
        "",
        "on load:",
        "\tset {_n} to 9.12",
        "\tsend json from text \"{'number': ${_n}\"",
        "",
        "on load:",
        "\tsend json from request last response"
})
@Since("2.5.0")

public class ExprCreateJson extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprCreateJson.class, JsonElement.class, ExpressionType.COMBINED,
                "[a] json from (text|string) %string%",
                "[a] json from %object%",
                "[a] json from file [path] %string%",
                "[a] json from request %object%",
                "[a] new json from<.*>"
        );
    }

    private final Pattern PATTERN_VAR_LIST = Pattern.compile("\\$\\{[A-Za-z0-9_]+::\\*}");
    private final Pattern PATTERN_VAR = Pattern.compile("\\$\\{[\\-A-z_].?}");
    private Expression<?> exprToSerialize;
    private int pattern;

    @Override
    protected @Nullable JsonElement @NotNull [] get(@NotNull Event event) {
        Object assignedValue;
        if (pattern == 0) {
            assignedValue = exprToSerialize.getSingle(event);
            if (assignedValue == null) return new JsonElement[0];
            boolean hasVariables = PATTERN_VAR_LIST.matcher(assignedValue.toString()).find() || PATTERN_VAR.matcher(assignedValue.toString()).find();

            if (hasVariables) {
                try {
                    return new JsonElement[]{parseVariable(assignedValue.toString(), event)};
                } catch (JsonSyntaxException syntax) {
                    sendMessage(syntax.getCause(), WARNING);
                }
            } else {
                try {
                    return new JsonElement[]{JsonParser.parseString(assignedValue.toString())};
                } catch (JsonSyntaxException exception) {
                    return new JsonElement[0];
                }
            }
        } else if (pattern == 1) {
            assignedValue = exprToSerialize.getSingle(event);
            if (assignedValue instanceof String) {
                return new JsonElement[0];
            }
            return new JsonElement[]{DefaultAdapters.parse(assignedValue, exprToSerialize, event)};
        } else if (pattern == 2) {
            JsonFilesHandler jfh = new JsonFilesHandler();
            assignedValue = exprToSerialize.getSingle(event);
            assert assignedValue != null;
            return new JsonElement[]{jfh.readFile(assignedValue.toString())};

        } else if (pattern == 3) {
            assignedValue = exprToSerialize.getSingle(event);
            assert assignedValue != null;
            if (assignedValue instanceof HttpResponse) {
                return new JsonElement[]{JsonParser.parseString(((HttpResponse) assignedValue).getBody())};
            } else if (assignedValue instanceof HttpsURLConnection) {
                try {
                    return new JsonElement[]{new Gson().toJsonTree(((HttpsURLConnection) assignedValue).getContent())};
                } catch (IOException ignored) {
                    return new JsonElement[]{};
                }
            } else if (assignedValue instanceof String) {
                try {
                    return new JsonElement[]{JsonParser.parseString(assignedValue.toString())};
                } catch (JsonSyntaxException syntaxException) {
                    return new JsonElement[]{};
                }
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
    public @NotNull String toString(@Nullable Event event, boolean b) {
        String main = "json from ";
        if (pattern == 0) return main + "string/text " + exprToSerialize.toString(event, b);
        if (pattern == 1 || pattern == 2) return main + exprToSerialize.toString(event, b);
        if (pattern == 3) return main + "file path" + exprToSerialize.toString(event, b);
        if (pattern == 4) return main + "request" + exprToSerialize.toString(event, b);
        return "@Deprecated -> [a] new json from ...";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
        pattern = i;
        if (pattern == 3) {
            if (_REQUEST_HANDLER) {
                exprToSerialize = LiteralUtils.defendExpression(expressions[0]);
                return LiteralUtils.canInitSafely(exprToSerialize);
            }
            return false;
        } else if (pattern == 4) {
            sendMessage("This is a deprecated syntax, &f&lTry that expression without &c&7'&f... &cnew&f json from text ...", ERROR);
            return false;
        } else {
            exprToSerialize = LiteralUtils.defendExpression(expressions[0]);
            return LiteralUtils.canInitSafely(exprToSerialize);
        }
    }
}
