/**
 * Special thanks to the creator of 'script-json' btk5h thanks to him, we didn't have to use our own Json mapping, so all thanks go to him
 * https://github.com/btk5h/skript-json/blob/master/LICENSE
 * MIT License
 * Copyright (c) 2017 Bryan Terce
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */

package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.*;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.Variable;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static cz.coffee.skriptgson.util.Utils.newGson;

@SuppressWarnings({"unused","NullableProblems","unchecked"})
@Since("1.0")
@Name("Converts list")
@Description("Converting list variable to JsonElement")
@Examples({
        "\n", "on load:",
        "\tset {-w} to form of {_json::*}",
        "\tbroadcast {-w} pretty printed"
})
public class ExprJsonListToJsonElements extends SimpleExpression<JsonElement> {

    static {
        PropertyExpression.register(ExprJsonListToJsonElements.class, JsonElement.class,
                "(form|structure)",
                "objects");
    }

    private VariableString var;
    private boolean isLocal;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        Expression<?> expr = exprs[0];
        if (expr instanceof ch.njol.skript.lang.Variable<?> varExpr) {
            if (varExpr.isList()) {
                var = Variable.getVarName(varExpr);
                isLocal = varExpr.isLocal();
                return true;
            }
        }
        SkriptGson.warning(expr +  "is not al ist variable");
        return false;
    }

    @Override
    protected JsonElement[] get(Event e) {
        try {
            String var = this.var.toString(e).toLowerCase(Locale.ENGLISH);
            return new JsonElement[] {JsonParser.parseString(String.valueOf(JsonTree(e, var.substring(0, var.length() - 1), false)))};
        } catch (Exception ex) {ex.printStackTrace();}
        return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends JsonElement> getReturnType() {
        return JsonElement.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return null;
    }

    private Object getVariable(Event e, String name) {
        final Object val = Variables.getVariable(name, e, isLocal);
        if (val == null) {
            return Variables.getVariable((isLocal ? ch.njol.skript.lang.Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return val;
    }

    private static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if ( length == 0) {
            return  false;
        }
        int i = 0;
        if(str.charAt(0) == '-'){
            if(length == 1) {
                return false;
            }
            i = 1;
        }
        while (i < length) {
            char c =  str.charAt(i);
            if(c < '0' || c > '9') {
                return false;
            }
            i++;
        }
        return true;
    }

    private Object JsonSubTree(Event e, String name) {
        Object val = getVariable(e, name);
        if (val == null) {
            val = JsonTree(e, name + ch.njol.skript.lang.Variable.SEPARATOR, false);
        } else if (val == Boolean.TRUE) {
            Object subtree = JsonTree(e,name + ch.njol.skript.lang.Variable.SEPARATOR,true);
            if(subtree != null) {
                val = subtree;
            }
        }
        if (!(val instanceof String || val instanceof Number || val instanceof Boolean || val instanceof Map || val instanceof List || val instanceof JsonPrimitive)) {
            val = newGson().toJson(val);
        }
        return val;
    }
    private JsonElement JsonTree(Event e, String name, boolean nullable) {
        Map<String, Object> var = (Map<String, Object>) getVariable(e, name + "*");
        if (var==null) {
            return nullable ? null : new JsonObject();
        }

        Stream<String> keys = var.keySet().stream().filter(Objects::nonNull);
        if(var.keySet().stream().filter(Objects::nonNull).allMatch(ExprJsonListToJsonElements::isInteger)) {
            JsonArray obj = new JsonArray();
            keys.forEach(key -> obj.getAsJsonArray().add(
                    JsonParser.parseString(JsonSubTree(e, name+key).toString())
                    )
            );
            return obj;
        } else {
            JsonObject obj = new JsonObject();
            keys.forEach(
                    key -> obj.getAsJsonObject().add(
                            key, JsonParser.parseString(JsonSubTree(e, name+key).toString())
                    )
            );
            return obj;
        }
    }
}
