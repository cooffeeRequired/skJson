package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.adapters.SimpleAdapter;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.GsonUtils.GsonFileHandler.saveToFile;
import static cz.coffee.skriptgson.utils.GsonUtils.change;
import static cz.coffee.skriptgson.utils.GsonUtils.getVariable;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("Change value of JsonElements.")
@Description("You can handle a JsonElement or cached Jsons. and change their data as you want.")
@Examples({"on load:",
        "\tset {-json} to new json from text \"{'test': {'some': false}\"",
        "\tchange {-json} value \"test:some\" to item (iron sword named \"The &acolored &fSword\") and save it to json file \"plugins/sk-gson/test.json\"",
        "\tbroadcast {-json}",
        "",
        "\tchange cached json-id \"json5\" value \"test:some\" to item (iron sword named \"The &acolored &fSword\")",
        "\tbroadcast cached json-id \"json5\"",
})
@Since("2.0.0")


public class EffChangeJsonElement extends Effect {
    static {
        Skript.registerEffect(EffChangeJsonElement.class,
                "change (1:%-jsonelement%|2:[cached] json[(-| )id] %-string%) [value] %string% to %-object% [and (:save) it to [json file] %-string%]",
                "change (1:%-jsonelement%|2:[cached] json[(-| )id] %-string%) [value] %string% to item %-itemstack% [and (:save) it to [json file] %-string%]"
        );
    }

    private Expression<Object> fromGeneric;
    private Expression<Object> dataTochange;
    private Expression<ItemType> itemTypeExpression;
    private boolean save, isCached, isJson, isItem, isLocal;
    private VariableString variableString;
    private Expression<String> filePath;
    private Expression<String> fromExpression;

    @Override
    protected void execute(@NotNull Event e) {
        String filePath = null;
        JsonElement json = null;
        GsonErrorLogger err = new GsonErrorLogger();
        if (save) {
            filePath = this.filePath.getSingle(e);
        }
        Object dataToChange;
        String from = fromExpression.getSingle(e);
        if (from == null) return;

        if (!isItem) {
            dataToChange = this.dataTochange.getSingle(e);
        } else {
            dataToChange = this.itemTypeExpression.getSingle(e);
        }
        if (dataToChange == null) return;

        if (isJson) {
            String variableName = variableString.getDefaultVariableName().replaceAll("_", "");
            Object isJsonVar = fromGeneric.getSingle(e);
            if (!(isJsonVar instanceof JsonElement)) {
                SkriptGson.warning(err.ONLY_JSONVAR_IS_ALLOWED);
                return;
            }
            JsonElement fromVar = hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(getVariable(e, variableName, isLocal)));
            json = change(fromVar, from, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(dataToChange)));

        } else if (isCached) {
            String cachedID = String.valueOf(fromGeneric.getSingle(e));
            if (JSON_HASHMAP.containsKey(cachedID)) {
                JsonElement fromCache = hierarchyAdapter().toJsonTree(JSON_HASHMAP.get(cachedID));
                JSON_HASHMAP.remove(cachedID);
                json = change(fromCache, from, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(dataToChange)));
                JSON_HASHMAP.put(cachedID, json);
            } else {
                SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
            }
        }
        if (save) {
            System.out.println(json);
            saveToFile(json, filePath);
        }
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        GsonErrorLogger err = new GsonErrorLogger();
        filePath = (Expression<String>) exprs[4];
        save = parseResult.hasTag("save");

        isItem = matchedPattern == 1;
        isCached = parseResult.mark == 2;
        isJson = parseResult.mark == 1;

        fromExpression = (Expression<String>) exprs[2];

        if (isJson) {
            fromGeneric = (Expression<Object>) exprs[0];
            if (fromGeneric instanceof Variable<Object> variable) {
                if (!variable.isSingle()) {
                    SkriptGson.severe(err.VAR_NEED_TO_BE_SINGLE);
                    return false;
                } else {
                    isLocal = variable.isLocal();
                    variableString = variable.getName();
                }
            } else {
                SkriptGson.severe(err.ONLY_JSONVAR_IS_ALLOWED);
                return false;
            }
        } else {
            fromGeneric = (Expression<Object>) exprs[1];
        }

        if (!isItem) {
            dataTochange = LiteralUtils.defendExpression(exprs[3]);
            return LiteralUtils.canInitSafely(dataTochange);
        } else {
            itemTypeExpression = LiteralUtils.defendExpression(exprs[3]);
            return LiteralUtils.canInitSafely(itemTypeExpression);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "change " + (isJson ? fromGeneric.toString() + " " : null) + ((isCached) ? "cached json " + fromGeneric.toString() : null) + "value " + fromExpression.toString() + "to " + (isItem ? "item " + itemTypeExpression.toString(e, debug) : dataTochange.toString(e, debug)) + " " + (save ? "and save it to json file " + filePath.toString() : null);
    }
}
