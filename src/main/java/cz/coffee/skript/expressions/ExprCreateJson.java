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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import cz.coffee.adapter.DefaultAdapter;
import cz.coffee.core.utils.JsonFilesHandler;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static cz.coffee.core.utils.JsonUtils.parseVariable;

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
})
@Since("2.5.0")

public class ExprCreateJson extends SimpleExpression<JsonElement> {

    static {
        Skript.registerExpression(ExprCreateJson.class, JsonElement.class, ExpressionType.COMBINED,
                "[a] json from (text|string) %string%",
                "[a] json from %object%",
                "[a] json from file [path] %string%",
                "[a] new json from<.*>"
        );
    }

    private final Pattern PATTERN_VAR_LIST = Pattern.compile("\\$\\{[A-Za-z0-9_]+::\\*}");
    private final Pattern PATTERN_VAR = Pattern.compile("\\$\\{[A-Za-z0-9_]+}");
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
                   return new JsonElement[0];
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
            return new JsonElement[]{DefaultAdapter.parse(assignedValue, exprToSerialize, event)};
        } else if (pattern == 2) {
            JsonFilesHandler jfh = new JsonFilesHandler();
            assignedValue = exprToSerialize.getSingle(event);
            assert assignedValue != null;
            return new JsonElement[]{jfh.readFile(assignedValue.toString())};
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
        return "@Deprecated -> [a] new json from ...";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int i, @NotNull Kleenean kleenean, @NotNull ParseResult parseResult) {
        pattern = i;
        if (pattern == 4) {
            Skript.error("This is a deprecated syntax. Don't use syntax like 'new json from ...'");
            return false;
        } else {
            exprToSerialize = LiteralUtils.defendExpression(expressions[0]);
            return LiteralUtils.canInitSafely(exprToSerialize);
        }
    }
}
