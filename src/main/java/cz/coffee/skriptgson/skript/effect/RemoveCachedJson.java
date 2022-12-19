package cz.coffee.skriptgson.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import cz.coffee.skriptgson.SkriptGson;
import cz.coffee.skriptgson.utils.GsonErrorLogger;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.skriptgson.SkriptGson.JSON_HASHMAP;

@Name("Remove cached json")
@Description({"You can basically remove the generic Json from the cached Json Map."})
@Examples({"on load:",
        "\tremove json \"json5\"",
        "\tsend json \"json5\""
})
@Since("2.0.0")

public class RemoveCachedJson extends Effect {

    static {
        Skript.registerEffect(RemoveCachedJson.class, "remove [cached] json[(-| )id] %string%");
    }

    private Expression<String> stringIdExpression;

    @Override
    protected void execute(@NotNull Event e) {
        GsonErrorLogger err = new GsonErrorLogger();
        String stringIdExpression = this.stringIdExpression.getSingle(e);
        if (JSON_HASHMAP.containsKey(stringIdExpression)) {
            JSON_HASHMAP.remove(stringIdExpression);
        } else {
            SkriptGson.warning(err.ID_GENERIC_NOT_FOUND);
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "remove json" + stringIdExpression.toString(e, debug);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        stringIdExpression = (Expression<String>) exprs[0];
        return true;
    }
}
