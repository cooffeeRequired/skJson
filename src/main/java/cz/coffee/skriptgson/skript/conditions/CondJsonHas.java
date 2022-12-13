package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.coffee.skriptgson.utils.GsonUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;


@Since("1.2.0")
@Name("JSON Has Key/Value's")
@Description({"Used to get information if the key or value exists in the JSON"})
@Examples({"on script load:",
        "\tset {_j} to new json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tif {_j} has keys \"Hello\", \"Hi\":",
        "\t\tsend true"
})
public class CondJsonHas extends Condition {

    static {
        Skript.registerCondition(CondJsonHas.class,
                "%jsonelement% has (1¦(:key|:value) %-object%|2¦(:keys|:values) %-objects%)",
                "%jsonelement% does(n't| not) have (1¦(:key|:value) %-object%|2¦(:keys|:values) %-objects%)"
        );
    }

    private Expression<JsonElement> exprJsonElement;
    private Expression<?> exprSearch;
    private int pattern;
    private int mark;
    private int type;


    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        mark = parseResult.mark;
        type = parseResult.tags.contains("key") ? 1 : 2;
        exprJsonElement = (Expression<JsonElement>) exprs[0];
        if (exprs[0] == null)
            return false;
        setNegated(pattern == 1);
        if (mark == 2) {
            exprSearch = LiteralUtils.defendExpression(exprs[2]);
            type = parseResult.tags.contains("keys") ? 1 : 2;
        } else {
            exprSearch = LiteralUtils.defendExpression(exprs[1]);
        }
        return LiteralUtils.canInitSafely(exprSearch);
    }

    @Override
    public boolean check(@NotNull Event e) {
        JsonElement json = exprJsonElement.getSingle(e);
        String search = null;
        Object[] searches = new Objects[0];

        if (mark == 2) {
            searches = exprSearch.getAll(e);
        } else {
            Object nonStringifySearch = exprSearch.getSingle(e);
            if (nonStringifySearch == null) return false;
            search = nonStringifySearch.toString();
        }

        if (json == null) return false;
        boolean match = false;

        if (json instanceof JsonObject object) {
            if (mark == 1) {
                return (pattern == 0) == GsonUtils.check(object, search, type == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
            } else {
                for (Object search0 : searches) {
                    match = (pattern == 0) == GsonUtils.check(object, search0.toString(), type == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
                    if (!match) return false;
                }
                return match;
            }
        } else if (json instanceof JsonArray array) {
            if (mark == 1) {
                return (pattern == 0) == GsonUtils.check(array, search, type == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
            } else {
                for (Object search0 : searches) {
                    match = (pattern == 0) == GsonUtils.check(array, search0.toString(), type == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
                    if (!match) return false;
                }
                return match;
            }
        }
        return false;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "json " + exprSearch.toString(e, debug) + (isNegated() ? " doesn't have" : " has") + (mark == 1 ? (type == 1 ? " key " + exprSearch.getSingle(e) : " value " + exprSearch.getSingle(e)) : (type == 1 ? " keys " + Arrays.toString(exprSearch.getAll(e)) : " values " + Arrays.toString(exprSearch.getAll(e))));
    }
}