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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.adapters.SimpleAdapter;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.FILE_JSON_HASHMAP;
import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;
import static cz.coffee.skriptgson.utils.GsonUtils.GsonFileHandler.saveToFile;
import static cz.coffee.skriptgson.utils.GsonUtils.*;
import static cz.coffee.skriptgson.utils.Utils.hierarchyAdapter;

@Name("Append jsonelement/cached Json/Json file")
@Description({"You can append the jsonelement or the cached json or the json file"})
@Examples(value = {"command sk-example:",
        "\ttrigger:",
        "\t\tappend player's location with key \"location\" to cached json-id \"your\"",
        "\t\tsend cached json-id \"your\" with pretty print",
        "",
        "\t\tset {_json} to new json from player's world",
        "\t\tappend player's location with key \"location\" as new nested object \"player:data:0\" to {_json}",
        "\t\tsend {_json} with pretty print",
        "",
        "\t\tset {_fileJson} to new json from file path \"sk-gson\\test.json\"",
        "\t\tappend player's location to file \"sk-gson\\test.json\"",
        "\t\tset {_fileJson} to new json from file path \"sk-gson\\test.json\"",
        "\t\tsend {_fileJson} with pretty print",
})
@Since("2.0.0")

public class EffAppendJsonElement extends Effect {
    static {
        Skript.registerEffect(EffAppendJsonElement.class,
                "append [data] %object% [(:with key) %-string%] [(:as nested) [object] %-string%] to (1:%-jsonelement%|2:file [path] %-string%|3:[cached] json[(-| )id] %-string%)",
                "append item %itemstack% [(:with key) %-string%] [(:as nested) [object] %-string%] to (1:%-jsonelement%|2:file [path] %-string%|3:[cached] json[(-| )id] %-string%)"
        );
    }

    private boolean isJson, isCached, isFile, isItem, hasTagNested, hasTagKey;
    private boolean isLocal;
    private int pattern;
    private VariableString variableString;
    private Expression<Object> dataExpression;

    private Expression<String> keyExpression, nestedExpression;

    private Expression<Object> fromGenericExpression;
    private Expression<ItemType> fromItemType;

    @Override
    protected void execute(@NotNull Event e) {
        String Key = null, Nested = null;
        if (hasTagKey) {
            Key = keyExpression.getSingle(e);
        }
        if (hasTagNested) {
            Nested = nestedExpression.getSingle(e);
        }

        GsonErrorLogger err = new GsonErrorLogger();
        Object fromGeneric;

        if (!isItem) {
            fromGeneric = this.fromGenericExpression.getSingle(e);
        } else {
            fromGeneric = this.fromItemType.getSingle(e);
        }
        if (fromGeneric == null) return;

        if (isJson) {
            String variableName = variableString.getDefaultVariableName().replaceAll("_", "");
            Object isJsonVar = dataExpression.getSingle(e);
            if (!(isJsonVar instanceof JsonElement)) {
                SkriptGson.warning(err.ONLY_JSONVAR_IS_ALLOWED);
                return;
            }
            JsonElement json;
            JsonElement fromVar = hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(getVariable(e, variableName, isLocal)));
            if (Nested == null) {
                if (fromVar instanceof JsonObject object) {
                    object.add(Key == null ? String.valueOf(object.entrySet().size()) : Key, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)));
                } else if (fromVar instanceof JsonArray array) {
                    array.add(hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)));
                }
                json = fromVar;
            } else {
                json = append(fromVar, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)), Key, Nested);
            }
            setVariable(variableName, json, e, isLocal);

        } else if (isFile) {
            Object objectFilePath = dataExpression.getSingle(e);
            if (objectFilePath == null) return;
            String filepathString = objectFilePath.toString();
            JsonElement fromFile = GsonUtils.GsonFileHandler.fromFile(filepathString);
            JsonElement json;
            if (Nested == null) {
                if (fromFile instanceof JsonObject object) {
                    object.add(Key == null ? String.valueOf(object.entrySet().size()) : Key, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)));
                } else if (fromFile instanceof JsonArray array) {
                    array.add(hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)));
                }
                json = fromFile;
            } else {
                json = append(fromFile, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)), Key, Nested);
            }

            saveToFile(json, filepathString);

        } else if (isCached) {
            Object objectFilePath = dataExpression.getSingle(e);
            if (objectFilePath == null) return;

            if (FILE_JSON_HASHMAP.containsKey(objectFilePath.toString())) {
                if (JSON_HASHMAP.containsKey(objectFilePath.toString())) {
                    JsonElement fromCache = hierarchyAdapter().toJsonTree(JSON_HASHMAP.get(objectFilePath.toString()));
                    JsonElement json;
                    if (Nested == null) {
                        if (fromCache instanceof JsonObject object) {
                            object.add(Key == null ? String.valueOf(object.entrySet().size()) : Key, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)));
                        } else if (fromCache instanceof JsonArray array) {
                            array.add(hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)));
                        }
                        json = fromCache;
                    } else {
                        json = append(fromCache, hierarchyAdapter().toJsonTree(SimpleAdapter.toJson(fromGeneric)), Key, Nested);
                    }
                    JSON_HASHMAP.remove(objectFilePath.toString());
                    JSON_HASHMAP.put(objectFilePath.toString(), json);
                } else {
                    SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
                }
            } else {
                SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
            }
        }
    }


    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        hasTagKey = parseResult.hasTag("with key");
        hasTagNested = parseResult.hasTag("as nested");


        if (hasTagNested) {
            nestedExpression = (Expression<String>) exprs[2];
        }
        if (hasTagKey) {
            keyExpression = (Expression<String>) exprs[1];
        }

        isItem = (matchedPattern == 1);
        if (isItem) {
            fromItemType = LiteralUtils.defendExpression(exprs[0]);
        } else {
            fromGenericExpression = LiteralUtils.defendExpression(exprs[0]);
        }

        GsonErrorLogger err = new GsonErrorLogger();
        // parser marks
        isJson = (parseResult.mark == 1);
        isFile = (parseResult.mark == 2);
        isCached = (parseResult.mark == 3);

        if (isJson) {
            dataExpression = (Expression<Object>) exprs[3];
            if (dataExpression instanceof Variable<?> variable) {
                if (variable.isSingle()) {
                    isLocal = variable.isLocal();
                    variableString = variable.getName();
                } else {
                    SkriptGson.warning(err.VAR_NEED_TO_BE_SINGLE);
                    return false;
                }
            } else {
                SkriptGson.warning(err.ONLY_JSONVAR_IS_ALLOWED);
                return false;
            }
        } else if (isFile) {
            dataExpression = (Expression<Object>) exprs[4];
        } else if (isCached) {
            dataExpression = (Expression<Object>) exprs[5];
        }
        if (isItem) {
            return LiteralUtils.canInitSafely(fromItemType);
        } else {
            return LiteralUtils.canInitSafely(fromGenericExpression);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "write " + (pattern == 0 ? "data " + fromGenericExpression.toString() : "item " + fromItemType.toString()) + "to " + (isJson ? dataExpression.toString() : null) + (isFile ? "file path " + dataExpression.toString() : null) + (isCached ? "cached json " + dataExpression.toString() : null);
    }
}
