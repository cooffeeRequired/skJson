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
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


@Name("Write or put data to json file.")
@Since("1.0")
@Description({
        "You can put data (append) to json Object/Array, when you put data to Object you can define the Key, if you don't define the key",
        "key will be iterating from 0, otherwise Key will be set to your input Key. When you put data to Array you don't need a key"
})
@Examples({
        "on load:",
        "\tset {-e} to new json file \"YourAwesome\\test.json\"",
        "\twrite data (json from string 'Alright': false) to {-e}",
        "\tput data (json from string 'Alright': false) to {-e}"
})

@SuppressWarnings({"unchecked","unused","NullableProblems"})

public class EffWriteToFile extends Effect {

    static {
        Skript.registerEffect(EffWriteToFile.class,
                "(:write|:put) data %jsonelement% to json file %object% [(:as key) %-string%]"
        );
    }

    private Expression<JsonElement> jsonelement;
    private Expression<File> object;
    private Expression<String> inKey;
    private String type;
    private boolean k;

    @Override
    protected void execute(Event e) {
        if ( jsonelement == null || object == null )
            return;

        JsonElement json = jsonelement.getSingle(e);
        File file;
        try {
            file = object.getSingle(e);
        } catch (SkriptAPIException ex) {ex.printStackTrace();return;}

        FileOutputStream outStream;
        JsonWriter jsonWriter;
        JsonReader reader;
        String parsedElement = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(json);
        if ( file == null || json == null) {
            return;
        }
        if (Objects.equals(type, "write")) {
            try {
                outStream = new FileOutputStream(file);
                jsonWriter = new JsonWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
                jsonWriter.jsonValue(parsedElement);
                jsonWriter.flush();
                jsonWriter.close();
            } catch (IOException ignore) {}
        } else {
            try {
                reader = new JsonReader(new FileReader(file));
                JsonElement element = JsonParser.parseReader(reader);
                reader.close();
                if ( element.isJsonNull())
                    return;
                if ( element.isJsonObject() ) {
                    if ( !k ) {
                        int size = element.getAsJsonObject().entrySet().size();
                        element.getAsJsonObject().add(String.valueOf(size), json);
                        outStream = new FileOutputStream(file);
                        jsonWriter = new JsonWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
                        jsonWriter.jsonValue(new GsonBuilder()
                                .disableHtmlEscaping().setPrettyPrinting().create().toJson(element)
                        );
                        jsonWriter.flush();
                        jsonWriter.close();
                    } else {
                        /**
                         * TODO input jsonelement by users key input
                         * ? Enchantment -> search the user key input in array -> Try Append
                         */
                        System.out.println(inKey);
                    }
                }
            }catch (Exception ex) {ex.printStackTrace();}
        }





    }

    @Override
    public String toString(Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        jsonelement = (Expression<JsonElement>) exprs[0];
        SkriptGson.info("IN");
        if (parseResult.tags.contains("write")) {
            type = "write";
        } else if (parseResult.tags.contains("put")) {
            type = "put";
        }
        object = (Expression<File>) exprs[1];
        if (parseResult.tags.contains("as key")) {
            k = true;
            inKey = (Expression<String>) exprs[2];
        }
        return true;
    }
}
