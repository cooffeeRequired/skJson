package cz.coffee.skriptgson.skript.effect;


import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static cz.coffee.skriptgson.util.Utils.color;
import static cz.coffee.skriptgson.util.Utils.newGson;

@Since("1.2.2")
@Name("Write/Append JSON File")
@Description("Inserting / overwriting data to json file., It can be inserted as a nested object and also with the specified key")
@Examples({"on load:",
        "\tset {-file} to new json file \"plugins\\test\\test.json\"",
        "\tset {-item} to iron sword named \"&cTest\"",
        "\twrite data {-item} to json file {-file}",
        "\tappend data {-item} with key \"Item\" to json file {-file}",
        "\tset {-file} to new json file \"plugins\\test\\test.json\" with data (new json from string \"{'main':{'second':{'another':[]}}}\"",
        "\tappend data {-item} as nested object \"main:second:another\" with key \"Item\" to json file {-file}"
})



public class EffWriteJson  extends Effect {


    private boolean write, append, nested, key;
    private Expression<Object> rawData;
    private Expression<String> rawNestedData;
    private Expression<String> rawKey;
    private Expression<Object> rawFile;


    static {
        Skript.registerEffect(EffWriteJson.class,
                "append data %object% [as new (:nested) object %-string%] [with (:key) %-string%] to [json] file %object%",
                "write data %object% to [json] file %object%"
        );
    }


    private void outputWriter(JsonElement json, File file) {
        try {
            String jsonString = newGson().toJson(json);
            FileOutputStream fos = new FileOutputStream(file);
            try (var writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
                writer.jsonValue(jsonString);
            }
            } catch (IOException| JsonSyntaxException ex) {
                Skript.error("Bad file format " + file);
            }
    }
    private JsonElement inputReader(File file) {
        JsonElement element;
        try (var reader = new JsonReader(new FileReader(file))) {
            element = JsonParser.parseReader(reader);
            return element;
        } catch (JsonSyntaxException | IOException ex) {
            if(ex instanceof JsonSyntaxException) {
                Skript.error(color("&cThe json syntax isn't correct, error message "+ex.getMessage()));
            }
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        write = matchedPattern == 1;
        append = matchedPattern == 0;

        if(write) {
            rawData = LiteralUtils.defendExpression(exprs[0]);
            rawFile = (Expression<Object>) exprs[1];
        }
        if(append) {
            rawData = LiteralUtils.defendExpression(exprs[0]);
            rawNestedData = (Expression<String>) exprs[1];
            rawKey = (Expression<String>) exprs[2];
            rawFile = (Expression<Object>) exprs[3];
            key = parseResult.hasTag("key");
            nested = parseResult.hasTag("nested");
        }
        return LiteralUtils.canInitSafely(rawData);
    }

    @Override
    protected void execute(@NotNull Event e) {
        String Key = null, Nested = null;
        Object data = rawData.getSingle(e);
        Object __ = rawFile.getSingle(e);
        if(__ == null) return;
        File file = __ instanceof File ? (File)__ : new File(__.toString());
        GsonUtils utils = new GsonUtils();

        if(key) {
            Key = rawKey.getSingle(e);
        }
        if(nested) {
            Nested = rawNestedData.getSingle(e);
        }

        JsonElement element = null;
        if(data != null) {
            if (data instanceof JsonElement) {
                element = (JsonElement) data;
            } else if(data instanceof YggdrasilSerializable) {
                element = newGson().toJsonTree(data);
            } else {
                element = JsonParser.parseString(data.toString());
            }
        }

        if(element == null) return;
        JsonElement fileJson;
        fileJson = inputReader(file);
        if(fileJson == null) return;

        if(write) {
            outputWriter(element, file);
        }

        if(append) {
            if(nested) {
                if(fileJson.isJsonArray()) {
                    fileJson = utils.append(fileJson.getAsJsonArray(), Key, Nested, element);
                } else if(fileJson.isJsonObject()) {
                    fileJson = utils.append(fileJson.getAsJsonObject(), Key, Nested, element);
                }
            } else {
                if(fileJson.isJsonArray()) {
                    fileJson.getAsJsonArray().add(element);
                } else if(fileJson.isJsonObject()) {
                    fileJson.getAsJsonObject().add(
                            !key ? String.valueOf(fileJson.getAsJsonObject().entrySet().size() + 1) : (Key != null ? Key : "0"), element
                    );
                }
            }
            if(fileJson == null) return;
            outputWriter(fileJson, file);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return (write ? "write" : "append") + " data " + rawData.toString(e, debug) + " to json file " + rawFile.toString(e, debug);
    }
}
