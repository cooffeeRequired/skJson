package cz.coffeerequired.skript.core.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.google.gson.JsonElement;
import cz.coffeerequired.api.json.JsonMerge;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.util.LiteralUtils.canInitSafely;
import static ch.njol.skript.util.LiteralUtils.defendExpression;

@Name("Merge json")
@Description("Merges one JSON object or array into another. Deep merge combines nested objects.")
@Since("5.5")
@Examples("""
        set {_base} to json from "{a: 1, nested: {x: 1}}"
        set {_patch} to json from "{b: 2, nested: {y: 2}}"
        merge {_patch} into {_base} deeply
        """)
public class EffMergeJson extends Effect {

    private Expression<JsonElement> source;
    private Expression<JsonElement> target;
    private boolean deep;

    @Override
    protected void execute(Event event) {
        JsonElement into = target.getSingle(event);
        JsonElement from = source.getSingle(event);
        if (into == null || from == null) {
            return;
        }
        JsonElement merged = JsonMerge.merge(into, from, deep);
        target.change(event, new Object[]{merged}, ch.njol.skript.classes.Changer.ChangeMode.SET);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "merge " + source.toString(event, debug) + " into " + target.toString(event, debug)
                + (deep ? " deeply" : "");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        source = defendExpression(expressions[0]);
        target = defendExpression(expressions[1]);
        deep = matchedPattern != 0;
        return canInitSafely(source, target);
    }
}
