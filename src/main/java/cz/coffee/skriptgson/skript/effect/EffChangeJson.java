package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffee.skriptgson.util.GsonUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;


@Since("1.2.1")
@Name("Change JSON")
@Description("With this effect you can change JsonObject or JsonArray.")
@Examples({"on script load:",
        "   set {-json} to new json from string \"{'Players': {'3a51d20a-6200-11ed-9b6a-0242ac120002': {'name': 'coffee', 'isAdmin': true, 'prefix': '&4Admin'}}}\"",
        "   set {-file} to new json file \"plugins/gson/yourJson.json\" with data {-json}",
        "   change {_json} values \"isAdmin\", \"prefix\" to false, \"&b&lGod\""
})


public class EffChangeJson extends Effect {

    static {
        Skript.registerEffect(EffChangeJson.class,
                "change %jsonelement% (:key|:value) %object%(=>| to )%object%",
                "change %jsonelement% (:keys|:values) %objects%(=>| to )%objects%"
        );
    }

    private Expression<Objects> changes;
    private Expression<Objects> expressionFrom;
    private Expression<Object> change;
    private Expression<JsonElement> json;
    private int pattern;
    private int tag;
    private boolean isLocal;

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        if (json instanceof Variable<?> k) {
            isLocal = k.isLocal();
        }
        pattern = matchedPattern;
        json = (Expression<JsonElement>) exprs[0];
        expressionFrom = LiteralUtils.defendExpression(exprs[1]);
        if (pattern == 0) {
            tag = parseResult.hasTag("key") ? 1 : 2;
            change = LiteralUtils.defendExpression(exprs[2]);
            return true;
        } else if (pattern == 1) {
            tag = parseResult.hasTag("keys") ? 1 : 2;
            changes = LiteralUtils.defendExpression(exprs[2]);
            return LiteralUtils.canInitSafely(changes);
        }
        return LiteralUtils.canInitSafely(expressionFrom);
    }


    @Override
    protected void execute(@NotNull Event e) {
        String name = json.toString().toLowerCase();
        JsonElement element = json.getSingle(e);
        Object[] to = (pattern == 1) ? changes.getArray(e) : new Object[]{change.getSingle(e)};
        Object[] from = (pattern == 1) ? expressionFrom.getArray(e) : new Object[]{expressionFrom.getSingle(e)};
        if (element == null) return;

        JsonElement changed = null;

        if (pattern == 0) { // Single
            changed = GsonUtils.change(element, from[0].toString(), to[0], tag == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
        } else if (pattern == 1) { // List
            for (int i = 0; from.length > i; i++) {
                changed = GsonUtils.change(element, from[0].toString(), to[i], tag == 1 ? GsonUtils.Type.KEY : GsonUtils.Type.VALUE);
            }
        }
        GsonUtils.setVariable(name.toLowerCase(Locale.ENGLISH), changed, e, isLocal);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "changed " + (pattern == 0 ? (tag == 1 ? "key " : "keys ") : (tag == 2 ? "value " : "values ")) + "and store in " + json.toString(e, debug);
    }
}
