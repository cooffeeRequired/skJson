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
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


@Since("1.2.0")
@Name("Json Has Key/Value's")
@Description({"Used to get information if the key or value is in Json"})
@Examples({"on script load:",
        "\tset {-data} to json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tif {-data} has keys \"Hello\", \"Hi\":",
        "\tsend true"
})
public class CondJsonHas extends Condition {

//    static {
//        Skript.registerCondition(CondJsonHas.class,
//                "%jsonelement% has (1¦(:key|:value) %-string/integer/boolean%|2¦(:keys|:values) %-objects%)",
//                "%jsonelement% has(n't| not) (1¦(:key|:value)%-string/integer/boolean%|2¦(:keys|:values) %-objects%)"
//        );
//    }

    static {
        Skript.registerCondition(CondJsonHas.class,
                "%jsonelement% have (1¦(:key|:value) %-object%|2¦(:keys|:values) %-objects%)",
                "%jsonelement% have(n't| not) (1¦(:key|:value) %-object%|2¦(:keys|:values) %-objects%)"

        );
    }

    private Expression<JsonElement> otch;
    private Expression<?> check;
    private int pattern;
    private int mark;
    private int type;


    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, ParseResult parseResult) {
        pattern = matchedPattern;
        mark = parseResult.mark;
        type = parseResult.tags.contains("key") ? 1 : 2;
        otch = (Expression<JsonElement>) exprs[0];
        if ( exprs[0] == null)
            return false;
        setNegated(pattern == 1);
        if (mark == 2) {
            check = LiteralUtils.defendExpression(exprs[2]);
            type = parseResult.tags.contains("keys") ? 1 : 2;
        } else {
            check = LiteralUtils.defendExpression(exprs[1]);
        }
        return LiteralUtils.canInitSafely(check);
    }

    @Override
    public boolean check(@NotNull Event e) {
        GsonUtils utils = new GsonUtils();
        JsonElement jsonelement = otch.getSingle(e);
        String object = null;
        Object[] objects = new Objects[0];
        if (mark == 2) {
            objects = check.getAll(e);
        } else {
            object = String.valueOf(check.getSingle(e));
        }
        if(jsonelement == null)
            return false;

        if (mark == 1) {
            if (jsonelement.isJsonObject()) {
                return (pattern == 0) == (type == 1 ? utils.getKey(object).check(jsonelement.getAsJsonObject()) : utils.getValue(object).check(jsonelement.getAsJsonObject()));
            } else if (jsonelement.isJsonArray()) {
                return (pattern == 0) == (type == 1 ? utils.getKey(object).check(jsonelement.getAsJsonArray()) : utils.getValue(object).check(jsonelement.getAsJsonArray()));
            }
        } else {
            boolean b = false;
            for (Object key : objects) {
                if(jsonelement.isJsonObject()) {
                    b = (pattern == 0) == (type == 1 ? utils.getKey(key).check(jsonelement.getAsJsonObject()) : utils.getValue(key).check(jsonelement.getAsJsonObject()));
                    if (!b)
                        return false;
                } else if(jsonelement.isJsonArray()){
                    b = (pattern == 0) == (type == 1 ? utils.getKey(key).check(jsonelement.getAsJsonArray()) : utils.getValue(key).check(jsonelement.getAsJsonArray()));
                    if (!b)
                        return false;
                }
            }
            return b;
        }
        return false;
    }

    @Override
    public @NotNull String toString(Event e, boolean debug) {
        return "json " + check.toString(e,debug) + (isNegated() ? " is exist" : "isn't exist");
    }
}
