package cz.coffee.skriptgson.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.util.JsonMap;
import org.bukkit.event.Event;

import java.util.Objects;


@Since("1.0")
@Name("Json Has Key/Value's")
@Description({"Used to get information if the key or value is in Json"})
@Examples({"on script load:",
        "\tset {-data} to json from string \"{'Hello': {'Hi': 'There'}}\"",
        "\tif {-data} has keys \"Hello\", \"Hi\":",
        "\tsend true"
})


@SuppressWarnings({"unchecked","unused","NullableProblems"})
public class CondJsonHas extends Condition {

    static {
        Skript.registerCondition(CondJsonHas.class,
                "%jsonelement% has (1¦(:key|:value) %-string/integer/boolean%|2¦(:keys|:values) %-objects%)",
                "%jsonelement% has(n't| not) (1¦(:key|:value)%-string/integer/boolean%|2¦(:keys|:values) %-objects%)"
        );
    }

    private Expression<JsonElement> otch;
    private Expression<?> check;
    private Expression<?> checkAll;
    private int pattern;
    private int mark;
    private int type;


    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pattern = matchedPattern;
        mark = parseResult.mark;
        type = parseResult.tags.contains("key") ? 1 : 2;
        otch = (Expression<JsonElement>) exprs[0];
        if ( exprs[0] == null)
            return false;
        setNegated(pattern == 1);
        if (mark == 2) {
            checkAll = LiteralUtils.defendExpression(exprs[2]);
            type = parseResult.tags.contains("keys") ? 1 : 2;
            return LiteralUtils.canInitSafely(exprs[2]);
        } else {
            check = LiteralUtils.defendExpression(exprs[1]);
            return LiteralUtils.canInitSafely(exprs[1]);
        }
    }

    @Override
    public boolean check(Event e) {
        JsonElement jsonelement = otch.getSingle(e);
        String object = null;
        Object[] objects = new Objects[0];
        if (mark == 2) {
            objects = checkAll.getAll(e);
        } else {
            object = String.valueOf(check.getSingle(e));
        }
        assert jsonelement != null;
        if (mark == 1) {
            if (jsonelement.isJsonObject()) {
                return (pattern == 0) == JsonMap.checkObject(jsonelement.getAsJsonObject(), type, object);
            } else if (jsonelement.isJsonArray()) {
                return (pattern == 0) == JsonMap.checkArray(jsonelement.getAsJsonArray(), type, object);
            }
        } else {
            if (jsonelement.isJsonObject()) {
                boolean b = false;
                for (Object key : objects) {
                    b = JsonMap.checkObject(jsonelement.getAsJsonObject(), type, key.toString());
                    if (!b)
                        return false;
                }
                return (pattern == 0) == b;
            } else if (jsonelement.isJsonArray()) {
                boolean b = false;
                for (Object key : objects) {
                    b = JsonMap.checkArray(jsonelement.getAsJsonArray(), type, key.toString());
                    if (!b)
                        return false;
                }
                return (pattern == 0) == b;
            }
        }
        return false;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return null;
    }
}
