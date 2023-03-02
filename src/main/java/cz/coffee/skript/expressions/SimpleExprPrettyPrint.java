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
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.google.gson.*;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.Utils.color;


@Name("Json outputting as pretty printed.")
@Description("You can do colorize and smart output of your current json.")
@Examples({"on load:",
        "\tset {_json} to json from text \"{'player': 'your name', 'number': 10, 'bool': false}\"",
        "\tsend {_json} with pretty print"
})
@Since("2.0.0")

public class SimpleExprPrettyPrint extends SimpleExpression<String> {

    private static final String RESET = "§r";

    static {
        Skript.registerExpression(SimpleExprPrettyPrint.class, String.class, ExpressionType.SIMPLE,
                "%json% with pretty print"
        );
    }

    private Expression<JsonElement> jsonExpression;

    @Override
    protected @Nullable String @NotNull [] get(@NotNull Event e) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().enableComplexMapKeySerialization().create();
        JsonElement el = jsonExpression.getSingle(e);
        if (el instanceof JsonObject) {
            if (el.getAsJsonObject().has("nbt")) {
                String n = el.getAsJsonObject().get("nbt").getAsString();
                el.getAsJsonObject().remove("nbt");
                el.getAsJsonObject().add("nbt", JsonParser.parseString(n));
            }
        }
        String jsonString = gson.toJson(el);
        jsonString = color(jsonString
                .replaceAll("(true)", "§a$0" + RESET)
                .replaceAll("(false)", "§c$0" + RESET)
                .replaceAll("(null)", "§5$0" + RESET)
                .replaceAll("([{}])", "§7$0" + RESET)
                .replaceAll("([\\[\\]])", "§6$0" + RESET)
                .replaceAll("(\")(.*?)(\")", "$1§f$2$3" + RESET)
                .replaceAll("(?<=\\s|^)\\d+", "§3$0" + RESET)
        );
        jsonString = "\n" + jsonString.replaceAll("\\\\\"", "\"");
        return new String[]{jsonString};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return jsonExpression.toString(e, debug) + "with pretty print";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        jsonExpression = (Expression<JsonElement>) exprs[0];
        return true;
    }
}
