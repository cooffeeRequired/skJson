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


package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.VariableReflect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static cz.coffee.skriptgson.util.Utils.color;
import static cz.coffee.skriptgson.util.Utils.newGson;

@Since("1.2.0")
@Name("Map|Copy json to list variables")
@Description("You can copy|map json to variable list, and work with the values:keys pair")
@Examples({
        "on load:",
        "\tset {-e} to json {\"anything\": [1,2,\"false\"]",
        "\tcopy json from {-e} to {_json::*}"
})

public class EffMapJsonToList extends Effect {

    static {
        Skript.registerEffect(EffMapJsonToList.class,
                "(map|copy) json from %string/jsonelement% to %objects%");
    }

    private Expression<Object> json;
    private VariableString variable;
    private boolean isLocal;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        json = (Expression<Object>) exprs[0];
        Expression<?> expr = exprs[1];
        if (expr instanceof ch.njol.skript.lang.Variable<?> varExpr) {
            if(varExpr.isList()){
                variable = VariableReflect.getVarName((ch.njol.skript.lang.Variable<?>) expr);
                isLocal = varExpr.isLocal();
                return true;
            }
        }
        SkriptGson.warning(expr +  "The variable is not a list.");
        return false;
    }

    @Override
    protected void execute(@NotNull Event e) {
        if (json == null) {
            return;
        }
        Object json = this.json.getSingle(e);
        JsonElement jsonEl;
        String variable = this.variable.toString(e).toLowerCase(Locale.ENGLISH);
        if (json instanceof String) {
            assert false;
            jsonEl = JsonParser.parseString((String) json);
        } else {
            jsonEl = (JsonElement) json; }
        try {
            assert jsonEl != null;
            String N = variable.substring(0,variable.length()-3);
            mapE(e, N,jsonEl);
        } catch (Exception ex) {ex.printStackTrace();
            System.out.println("Here we are, EFF MAP");}
    }

    private void mapE(Event e, String name, JsonElement obj){
        if (obj == null)
            return;
        if (obj.isJsonObject()) {
            JsonHandlerObject(e, name, obj.getAsJsonObject());
        } else if (obj.isJsonArray()) {
            JsonHandlerArray(e, name, obj.getAsJsonArray());
        } else {
            setVariable(e, name, obj);
        }
    }

    private void JsonHandlerObject(Event e, String name, JsonObject obj) {
        obj.keySet()
                .forEach(key -> map(e,name + ch.njol.skript.lang.Variable.SEPARATOR + key, obj
                        .get(key)
                ));
    }
    private void JsonHandlerArray(Event e, String name, JsonArray obj) {
        for (int i =0;i < obj.size(); i++){
            map(e,name+ ch.njol.skript.lang.Variable.SEPARATOR + (i+1),obj.get(i));
        }
    }
    private void map(Event e, String name, JsonElement obj){
        if (obj.isJsonObject()) {
            if ( obj.getAsJsonObject().has("__javaclass__") || obj.getAsJsonObject().has("__skriptclass__")) {
                setVariable(e, name, newGson().toJson(obj));
            } else {
                JsonHandlerObject(e, name, obj.getAsJsonObject());
                setVariable(e, name, true);
                setVariable(e, name, obj.getAsJsonObject());
            }
        } else if (obj.isJsonArray()) {
            setVariable(e, name, true);
            JsonHandlerArray(e, name, obj.getAsJsonArray());
        } else {
            Object data = null;
            if(obj.getAsJsonPrimitive().isString()){
                data = color(obj.getAsJsonPrimitive().getAsString());
            } else if ( obj.getAsJsonPrimitive().isNumber()) {
                data = obj.getAsJsonPrimitive().getAsNumber();
            } else if ( obj.getAsJsonPrimitive().isBoolean()) {
                data = obj.getAsJsonPrimitive().getAsBoolean();
            }
            setVariable(e,
                    name,
                    data == null ? obj : data
                    );
        }
    }
    private void setVariable(Event e, String name, Object obj) {
        Variables.setVariable(name.toLowerCase(Locale.ENGLISH), obj, e, isLocal);
    }
    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return json.toString(e,debug) + " => " + variable.toString(e,debug);
    }
}
