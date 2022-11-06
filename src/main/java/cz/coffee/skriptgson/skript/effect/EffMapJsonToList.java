/**
* Special thanks to the creator of 'script-json' btk5h thanks to him, we didn't have to use our own Json mapping, so all thanks go to him
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */


package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.*;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.VariableUtil;
import org.bukkit.event.Event;

import java.util.Locale;

@SuppressWarnings({"unused","NullableProblems","unchecked"})

@Since("1.0")
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

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        json = (Expression<Object>) exprs[0];
        Expression<?> expr = exprs[1];
        if (expr instanceof Variable<?> varExpr) {
            if(varExpr.isList()){
                variable = VariableUtil.getVarName((Variable<?>) expr);
                isLocal = varExpr.isLocal();
                return true;
            }
        }
        SkriptGson.warning(expr +  "is not al ist variable");
        return false;
    }

    @Override
    protected void execute(Event e) {
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
            mapE(e, variable.substring(0,variable.length()-3),jsonEl);
        } catch (Exception ex) {ex.printStackTrace(); }
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
                .forEach(key -> map(e,name + Variable.SEPARATOR + key, obj
                        .get(key)
                ));
    }
    private void JsonHandlerArray(Event e, String name, JsonArray obj) {
        for (int i =0;i < obj.size(); i++){
            map(e,name+Variable.SEPARATOR + (i+1),obj.get(i));
        }
    }
    private void map(Event e, String name, JsonElement obj){
        if (obj.isJsonObject()) {
            if ( obj.getAsJsonObject().has("__javaclass__") || obj.getAsJsonObject().has("__skriptclass__")) {
                setVariable(e, name, new Gson().toJson(obj));
            } else {
                setVariable(e, name, true);
                setVariable(e, name, obj.getAsJsonObject());
                JsonHandlerObject(e, name, obj.getAsJsonObject());
            }
        } else if (obj.isJsonArray()) {
            setVariable(e, name, true);
            JsonHandlerArray(e, name, obj.getAsJsonArray());
        } else {
            setVariable(e, name, obj);
        }
    }
    private void setVariable(Event e, String name, Object obj) {
        Variables.setVariable(name.toLowerCase(Locale.ENGLISH), obj, e, isLocal);
    }
    @Override
    public String toString( Event e, boolean debug) {
        return json.toString(e,debug) + " => " + variable.toString(e,debug);
    }
}
