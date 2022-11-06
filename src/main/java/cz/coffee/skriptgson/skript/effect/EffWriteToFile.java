package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static cz.coffee.skriptgson.util.PluginUtils.SanitizeString;


@Name("Write/Append data to json file")
@Since("1.0")
@Description({
        "Writing or Append to file with a property object and key"
})
@Examples({
        "json file:",
        "json",
        "\t{\"players\": {\"coffeeRequired\":{\"name\": \"coffee\",\"surname\": \"coffeeRequired\"}}}",
        "```\non load:",
        "\tset {_file} to loaded json file \"gson/test1.json\"\n",
        "\tappend new data (json false) to json file {_file} as nested object 'players;coffeeRequired' with key \"isAdmin\"",
        "\nJsonOutput:\n\t{\"players\": {\"coffeeRequired\":{\"name\": \"coffee\",\"surname\": \"coffeeRequired\", \"isAdmin\": false}}}"
})

@SuppressWarnings({"unchecked","unused","NullableProblems"})
public class EffWriteToFile extends Effect {

    static {
        Skript.registerEffect(EffWriteToFile.class,
                "write [new] data %jsonelement% to [json] file %object%",
                "append(ing|) [new] data %jsonelement% to [json] file %object% [(:as) [nested] object %-jsonelement% [(:with) [property] key %-string/integer%]]"
        );
    }

    private int pattern;
    private boolean as;
    private boolean with;
    private Expression<JsonElement> raw_data;
    private Expression<File> raw_jsonFile;
    private Expression<JsonElement> raw_objects;
    private Expression<?> raw_keys;
    private void outputWriter(JsonElement json, File file) {
        try {
            String p = new GsonBuilder()
                    .disableHtmlEscaping().setPrettyPrinting()
                    .create().toJson(json);
            FileOutputStream o = new FileOutputStream(file);
            JsonWriter w = new JsonWriter(new OutputStreamWriter(o, StandardCharsets.UTF_8));
            w.jsonValue(p);w.flush();w.close();
        } catch (IOException | JsonSyntaxException ex) {
            SkriptGson.severe("&cBad file format " + file + "");
        }
    }

    // ! todo.

    private JsonElement inputReader(File file) {
        JsonElement j;
        try {
            JsonReader r = new JsonReader(new FileReader(file));
            j = JsonParser.parseReader(r);
            r.close();
        } catch (IOException|JsonSyntaxException ex) {
            return null;
        }
        return j;
    }

    @Override
    protected void execute(Event e) {
        Object nKey = null;
        JsonElement k;
        File file;
        JsonElement json;
        if ( with) {
            nKey = raw_keys.getSingle(e);
        }

        try {
            json = raw_data.getSingle(e);
            file = raw_jsonFile.getSingle(e);
        } catch (SkriptAPIException ex) {
            SkriptGson.warning("&cDid you mean &e'%object%'&c instead of &f'%object");
            return;
        }
        String[] nObjects = new String[]{};


        if ( file == null || json == null)
            return;

        if ( pattern == 1) {
            if ( as){
                k = raw_objects.getSingle(e);
                assert k != null;
                nObjects = k.toString().contains(";") ? k.toString().split(";") : new String[]{k.toString()};
                if (nObjects[0] == null)
                    return;
            }

            JsonElement loaded_data = inputReader(file);
            if (loaded_data == null) {
                return;
            }
            JsonElement je;

            if (nObjects.length == 1 || nObjects.length == 0) {
                System.out.println("IN");
                if (loaded_data.isJsonObject()) {
                    loaded_data.getAsJsonObject().add(as ? nObjects[0].replaceAll("\"", "") : String.valueOf(loaded_data.getAsJsonObject().size()), json);
                } else if (loaded_data.isJsonArray()) {
                    loaded_data.getAsJsonArray().add(json);
                } else {
                    SkriptGson.warning("&cBad file format " + file + ",you can use the append method only for &e'Object' &r&land &e'Array'");
                    return;
                }
                outputWriter(loaded_data, file);
            } else {
                if (loaded_data.isJsonObject()) {
                    je = loaded_data.getAsJsonObject().get(SanitizeString(nObjects[0]));
                } else if (loaded_data.isJsonArray()) {
                    je = loaded_data.getAsJsonArray().get(Integer.parseInt(SanitizeString(nObjects[0])));
                } else {
                    SkriptGson.warning("&cBad file format " + file + ",you can use the append method only for &e'Object' &r&land &e'Array'");
                    return;
                }
                if (je == null)
                    return;
                nObjects = Arrays.copyOfRange(nObjects, 1, nObjects.length);
                for (String key : nObjects) {
                    key = SanitizeString(key);
                    if (je.isJsonObject()) {
                        je = je.getAsJsonObject().get(key);
                    } else if (je.isJsonArray()) {
                        je = je.getAsJsonArray().get(Integer.parseInt(key));
                    }
                }

                if (je.isJsonArray()) {
                    je.getAsJsonArray().add(json);
                } else if (je.isJsonObject()) {
                    je.getAsJsonObject().add(!with ? String.valueOf(je.getAsJsonObject().size()) : String.valueOf(nKey), JsonParser.parseString(json.toString()));
                } else {
                    return;
                }
                outputWriter(loaded_data, file);
            }
        } else {
            outputWriter(json, file);
        }
    }
    @Override
    public String toString( Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pattern = matchedPattern;
        as = parseResult.hasTag("as");
        with = parseResult.hasTag("with");
        raw_data = (Expression<JsonElement>) exprs[0];
        raw_jsonFile = (Expression<File>) exprs[1];
        if ( pattern == 1) {
            raw_objects = (Expression<JsonElement>) exprs[2];
            raw_keys = exprs[3];
        }

        return true;
    }
}
