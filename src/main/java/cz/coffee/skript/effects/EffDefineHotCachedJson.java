package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.google.gson.JsonObject;
import cz.coffee.core.cache.Cache;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


@Since("2.7.0 - define new cached json")
@Name("Pre-define new hot-cached json")
@Description("You can define new hot-cache for given uuid")
@Examples({"on load:",
        "\tdefine hot cached json \"test1\" for player's uuid",
        "\tsend hot cached json \"test1\" of player's uuid",
        "",
        "\tdefine hot cached json \"test1\" for \"e4f5b3f1-4a6d-4cc0-b4ba-67e8190f51cf\"",
        "\tsend hot cached json \"test1\""
})

public class EffDefineHotCachedJson extends Effect {

    static {
        Skript.registerEffect(EffDefineHotCachedJson.class,
                "define hot(-| )cached json %string% [(:for) %-object%]"
        );
    }

    private Expression<String> exprName;
    private Expression<Object> exprUUID;
    private boolean uuidFor;

    @Override
    protected void execute(@NotNull Event e) {
        UUID uuid = null;
        if (uuidFor) {
            Object o = exprUUID.getSingle(e);
            if (o == null) return;
            uuid = UUID.fromString(o.toString());
        }
        String name = exprName.getSingle(e);
        if (name == null) return;

        Cache.addToHot(name, new JsonObject(), uuid);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return ".";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        uuidFor = parseResult.hasTag("for");
        if (uuidFor) {
            exprUUID = (Expression<Object>) exprs[1];
        }
        exprName = (Expression<String>) exprs[0];
        return true;
    }
}
