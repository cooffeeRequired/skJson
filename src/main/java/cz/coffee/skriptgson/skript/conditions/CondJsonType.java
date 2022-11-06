package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.SkriptGson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("is %jsonelement% %json-type%")
@Description("Check what json type is passed %jsonelement%")
@Examples({
        "set {-e} to json from string \"['A': 'B']\"",
        "json {-e} is array: ",
        "\tbroadcast \"true\"",

        "set {-e} to json from string \"{'A': 'B'}\"",
        "json {-e} is object: ",
        "\tbroadcast \"true\"",
})
@Since("1.0")


@SuppressWarnings({"unchecked", "unused","NullableProblems"})
public class CondJsonType extends Condition {

    static {
        Skript.registerCondition(CondJsonType.class,
                "(json|jsonelement) %jsonelement% is [a] (:array|:object|:primitive)",
                "(json|jsonelement) %jsonelement% is(n't| not) [a] (:array|:object|:primitive)"
                );
    }

    private Expression<JsonElement> check;
    private int pattern;
    private List<String> tag;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        check = (Expression<JsonElement>) exprs[0];
        pattern = matchedPattern;
        tag = parseResult.tags;
        setNegated(pattern == 1);
        return true;
    }

    @Override
    public boolean check(Event e) {
        JsonElement checkSingle = check.getSingle(e);
        if (checkSingle == null) {
            return false;
        }
        if (tag.contains("array")) {
            return (pattern == 0) == checkSingle.isJsonArray();
        } else if (tag.contains("object")) {
            return (pattern == 0) == checkSingle.isJsonObject();
        } else if (tag.contains("primitive")){
            return (pattern == 0) == checkSingle.isJsonPrimitive();
        } else {
            SkriptGson.warning("&r&7You can compare &l&e%jsonelement%&7 only with array,object,primitive");
            return false;
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        if (tag.contains("array")) {
            return "json " + check.toString(e,debug) + (isNegated() ? " is array" : "isn't array" );
        } else if (tag.contains("object")) {
            return "json " + check.toString(e,debug) + (isNegated() ? " is object" : "isn't object" );
        } else if (tag.contains("primitive")){
            return "json " + check.toString(e,debug) + (isNegated() ? " is primitive" : "isn't primitive" );
        }else {
            return null;}
    }
}
