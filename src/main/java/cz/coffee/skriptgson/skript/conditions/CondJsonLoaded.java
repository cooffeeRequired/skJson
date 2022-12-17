package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;

@Name("JSON is loaded")
@Description({"Ability to check if json is loaded or not."})
@Examples({"on script load:",
        "\tload json file \"plugins/something.json\" as \"1\"",
        "\tset {_json} to json from text \"{'hello':false}\"",
        "\tif {_json} is loaded:",
        "\t\tsend \"json %{_json}% is loaded!\""
})
@Since("1.4.0")

public class CondJsonLoaded extends Condition {

    static {
        Skript.registerCondition(CondJsonLoaded.class,
                "[cached] json %jsonelement% is loaded",
                "[cached] json %jsonelement% is(n't| not) loaded"
        );
    }

    private Expression<JsonElement> rawJson;
    private int pattern;

    @Override
    public boolean check(@NotNull Event e) {
        JsonElement json = rawJson.getSingle(e);
        if (json == null) return false;
        return (pattern == 0) == JSON_HASHMAP.containsValue(json);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json " + rawJson.toString(e, debug) + " is loaded";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        pattern = matchedPattern;
        rawJson = (Expression<JsonElement>) exprs[0];
        setNegated(pattern == 1);
        return true;
    }
}
