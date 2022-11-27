package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EffTestSkriptGsonSyntaxes extends Effect {

    static {
        Skript.registerEffect(EffTestSkriptGsonSyntaxes.class, "test skript-gson [syntaxes] %string% %-objects%");
    }

    private Expression<String> code;
    private Expression<Objects> data;
    private VariableString var;

    @Override
    protected void execute(@NotNull Event e) {

        String code2 = code.getSingle(e);
        Object[] data2 = data.getAll(e);

        assert code2 != null;
        code2 = code2.toLowerCase();


        JsonElement json = JsonParser.parseString("{'A': {'B': {}, 'G': 1}}");
        JsonElement parse = JsonParser.parseString("{'Y': 'parsed'}");
        boolean successfully = false;


        if (Objects.equals(code2, "append")) {
            // without key & nested
            JsonElement mJson = json.getAsJsonObject().deepCopy();
            mJson.getAsJsonObject().add("-#-", parse);

            if (mJson.getAsJsonObject().has("-#-")) {
                SkriptGson.info("test &e\"append without nested\" &aSuccessfully");
                successfully = true;
            } else
                SkriptGson.info("test &e\"append without nested\" &cFailed");

            // with nested
            JsonElement sJson = GsonUtils.append(mJson, parse, "Nested", "A:B");
            assert sJson != null;
            if (sJson.getAsJsonObject().get("A").getAsJsonObject().get("B").getAsJsonObject().has("Nested"))
                SkriptGson.info("test &e\"append as nested\" &aSuccessfully");
            else
                SkriptGson.info("test &e\"append as nested\" &cFailed");
        }

        if (Objects.equals(code2, "Check Json".toLowerCase())) {
            if (GsonUtils.check(json, "B", GsonUtils.Type.KEY))
                SkriptGson.info("test &e\"check Type.KEY\" &aSuccessfully");
            else
                SkriptGson.info("test &e\"check Type.KEY\" &cFailed");

            if (GsonUtils.check(json, "1", GsonUtils.Type.VALUE))
                SkriptGson.info("test &e\"check non-json Type.VALUE\" &aSuccessfully");
            else
                SkriptGson.info("test &e\"check non-json Type.VALUE\" &cFailed");

            if (GsonUtils.check(json, "{'B': {}, 'G': 1}", GsonUtils.Type.VALUE))
                SkriptGson.info("test &e\"check json Type.VALUE\" &aSuccessfully");
            else
                SkriptGson.info("test &e\"check json Type.VALUE\" &cFailed");
        }

        if (Objects.equals(code2, "change Json".toLowerCase())) {
            JsonElement jsonX = JsonParser.parseString("{'A': {'B': {}, 'G': 1}}");
            JsonElement parseX = JsonParser.parseString("{'Y': 'parsed'}");
            JsonElement mJson = GsonUtils.change(jsonX, "A", "Change", GsonUtils.Type.KEY);
            JsonElement sJson = GsonUtils.change(jsonX, "B", parseX, GsonUtils.Type.VALUE);

            if (mJson.getAsJsonObject().has("Change"))
                SkriptGson.info("test &e\"change Json Type.KEY\" &aSuccessfully");
            else
                SkriptGson.info("test &e\"change Json Type.KEY\" &cFailed");

            if (sJson.getAsJsonObject().get("Change").getAsJsonObject().get("B").equals(parseX))
                SkriptGson.info("test &e\"change Json Type.VALUE\" &aSuccessfully");
            else
                SkriptGson.info("test &e\"change Json Type.VALUE\" &cFailed");
        }

        if (Objects.equals(code2, "listtojson")) {
            if (var == null) {
                SkriptGson.info("test &e\"listToJson\" &cFailed");
                Skript.debug("Do you forgot the name of variable?");
            } else {
                String clearVarName = null;
                clearVarName = var.toString(e).substring(0, var.toString(e).length() - 3);
                GsonUtils.GsonMapping.jsonToList(e, clearVarName, json, true);
                clearVarName = var.toString().substring(0, var.toString().length() - 1).replaceFirst("\"", "").replaceAll("[*]", "");
                JsonElement element = GsonUtils.GsonMapping.listToJson(e, clearVarName, true);
                JsonElement mJson = GsonUtils.GsonMapping.listToJson(e, var.toString(e).substring(0, var.toString(e).length() - 1), true);
                if (element.equals(mJson))
                    SkriptGson.info("test &e\"listToJson\" &aSuccessfully");
                else
                    SkriptGson.info("test &e\"listToJson\" &cFailed");
            }
        }


        if (Objects.equals(code2, "jsontolist")) {
            if (var == null) {
                SkriptGson.info("test &e\"jsonToList\" &cFailed");
                Skript.debug("Do you forgot the name of variable?");
            } else {
                String clearVarName = null;
                clearVarName = var.toString(e).substring(0, var.toString(e).length() - 3);
                GsonUtils.GsonMapping.jsonToList(e, clearVarName, json, true);
                Object varT = Variables.getVariable(clearVarName, e, true);
                assert varT != null;
                if (varT.toString().equals(json.toString()))
                    SkriptGson.info("test &e\"jsonToList\" &aSuccessfully");
                else
                    SkriptGson.info("test &e\"jsonToList\" &cFailed");
            }
        }


        if (Objects.equals(code2, "listToJson")) {
            if (var != null)
                System.out.println(GsonUtils.GsonMapping.listToJson(e, data2[0].toString().substring(0, data2[0].toString().length() - 1), true));
            else
                Skript.debug("Do you forgot the name of variable?");


        } else if (Objects.equals(code2, "jsonToList")) {
            if (var != null) {
                String clearVarName;
                clearVarName = var.toString(e).substring(0, var.toString(e).length() - 3);
                GsonUtils.GsonMapping.jsonToList(e, clearVarName, json, true);
                clearVarName = var.toString().substring(0, var.toString().length() - 1).replaceFirst("\"", "").replaceAll("[*]", "");

                JsonElement element = GsonUtils.GsonMapping.listToJson(e, clearVarName, true);

                if (element.toString().equalsIgnoreCase(json.toString()))
                    SkriptGson.info("test &e\"jsonToList\" &aSuccessfully");
                else
                    SkriptGson.info("test &e\"jsonToList\" &cFailed");
            } else {
                Skript.debug("Do you forgot the name of variable?");
            }

        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        code = (Expression<String>) exprs[0];
        data = (Expression<Objects>) exprs[1];

        if (data instanceof Variable<?> variable) {
            if (variable.isList()) {
                var = variable.getName();
                return true;
            }
        }

        return true;
    }
}
