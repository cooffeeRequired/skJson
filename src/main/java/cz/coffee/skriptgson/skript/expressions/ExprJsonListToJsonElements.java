/**
 * Special thanks to the creator of 'script-json' btk5h thanks to him, we didn't have to use our own Json mapping, so all thanks go to him
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
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.*;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.VariableUtil;
import org.bukkit.event.Event;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings({"unused","NullableProblems","unchecked","ConstantConditions"})
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
                "form",
                "objects");
    }

    private VariableString var;
    private boolean isLocal;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        Expression<?> expr = exprs[0];
        if (expr instanceof Variable) {
            Variable<?> varExpr = (Variable<?>) expr;
            if (varExpr.isList()) {
                var = VariableUtil.getVarName(varExpr);
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
            System.out.println("Test");
            String var = this.var.toString(e).toLowerCase(Locale.ENGLISH);
            return new JsonElement[] {JsonTree(e, var.substring(0, var.length() - 1), false)};
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
            return Variables.getVariable((isLocal ? Variable.LOCAL_VARIABLE_TOKEN : "") + name, e, false);
        }
        return val;
    }

    private static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if ( length != 0) {
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
            val = JsonTree(e, name + Variable.SEPARATOR, false);
        } else if (val == Boolean.TRUE) {
            Object subtree = JsonTree(e,name + Variable.SEPARATOR,true);
            if(subtree != null) {
                val = subtree;
            }
        }
        if (!(val instanceof String || val instanceof Number || val instanceof Boolean || val instanceof Map || val instanceof List || val instanceof JsonPrimitive)) {
            val = new Gson().toJson(val) ;
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
            keys.forEach(key -> obj.getAsJsonArray().add((String) JsonSubTree(e, name + key)));
            return obj;
        } else {
            JsonObject obj = new JsonObject();
            keys.forEach(key -> obj.getAsJsonObject().add(key, JsonParser.parseString(String.valueOf(JsonSubTree(e, name + key)))));
            return obj;
        }
    }
}
