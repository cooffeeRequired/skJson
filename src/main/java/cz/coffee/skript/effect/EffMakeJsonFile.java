package cz.coffee.skript.effect;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import cz.coffee.SkriptJson;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Since(SkriptJson.docSince)

@SuppressWarnings("unused")
public class EffMakeJsonFile extends Effect {

    static {
        Skript.registerEffect(EffMakeJsonFile.class, "json(-| )[skript] make [new] file %string%");
    }

    @Override
    protected void execute(@NotNull Event e) {
        System.out.println();
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "";
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        return true;
    }
}
