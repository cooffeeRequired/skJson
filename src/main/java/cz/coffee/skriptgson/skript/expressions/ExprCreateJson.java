package cz.coffee.skriptgson.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static cz.coffee.skriptgson.util.Utils.newGson;

@Name("JSON from Text/File/Request")
@Description("Creates a new JSON object from test/file/request")
@Since("1.2.0")
@Examples({"command example:",
        "\tsend new json from string \"{'Hello': 'There'}\"",
        "\tsend new json from player's location",
        "\tsend new json from arg-1"
})

@SuppressWarnings({"unused","NullableProblems"})
public class ExprCreateJson extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprCreateJson.class, Object.class, ExpressionType.COMBINED,
                "[a] new json from [(text|string)] %object%",
                "[a] new json from file [path] %object%",
                "[a] new json from request %object%"
        );
    }

    private Expression<?> toParse;
    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pattern = matchedPattern;
        if(pattern == 2) {
            SkriptGson.warning("&cSorry but at this moment you can't use this syntax.");
            return false;
        }
        toParse = LiteralUtils.defendExpression(exprs[0]);
        return LiteralUtils.canInitSafely(toParse);
    }

    @Override
    protected JsonElement[] get(Event e) {
        Object data = toParse.getSingle(e);
        boolean object = data instanceof ConfigurationSerializable || data instanceof YggdrasilSerializable;
        JsonElement element = null;
        if(data == null)
            return null;

        if(pattern == 0) {
            try {element = (object) ? newGson().toJsonTree(data) : JsonParser.parseString(data.toString());} catch (JsonSyntaxException ignored){}
        } else if(pattern == 1){
            try {element = JsonParser.parseReader(new JsonReader(new FileReader(data.toString())));} catch (FileNotFoundException ignored){}
        }
        return new JsonElement[]{element};
    }

    @Override
    public boolean isSingle() {return true;}

    @Override
    public Class<? extends JsonElement> getReturnType() {return JsonElement.class;}


    @Override
    public String toString(@Nullable Event e, boolean debug) {
        if(pattern == 0) {return  "new json from text";
        } else if (pattern == 1) {return  "new json from file";
        } else {return  "new json from request";
        }
    }
}
