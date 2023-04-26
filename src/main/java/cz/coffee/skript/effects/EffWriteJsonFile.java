package cz.coffee.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import cz.coffee.core.utils.FileUtils;
import cz.coffee.core.utils.JsonFile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static cz.coffee.core.utils.AdapterUtils.parseItem;


@SuppressWarnings("unused")
@Name("Write json file with contents")
@Description("You can write/re-write to jsons")
@Examples({
        "command sk-example:",
        "\ttrigger:",
        "\t\tset {_json} to json from player's world",
        "\t\twrite {_json} to json file \"*.json\"",
})
@Since("2.8.0 performance & clean")
public class EffWriteJsonFile  extends AsyncEffect {

    static {
        Skript.registerEffect(EffWriteJsonFile.class,
                "[re[-]]write %object% to [json file] %-string/jsonfile%"
        );
    }


    private Expression<?> inputEx;
    private Expression<?> ExprFile;

    @Override
    protected void execute(@NotNull Event e) {
        JsonFile file = null;
        Object o = ExprFile.getSingle(e);
        if (o instanceof JsonFile) {
            file = (JsonFile) o;
        } else if (o instanceof String) {
            file = new JsonFile((String) o);
        }
        final Object data =  inputEx.getSingle(e);
        if (file != null) {
            CompletableFuture<Boolean> result =  FileUtils.write(file, parseItem(data, inputEx, e));
        }
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return Classes.getDebugMessage(e);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        ExprFile = LiteralUtils.defendExpression(exprs[1]);
        inputEx = LiteralUtils.defendExpression(exprs[0]);
        if (LiteralUtils.canInitSafely(inputEx)) {
            return LiteralUtils.canInitSafely(ExprFile);
        }
        return false;
    }
}
