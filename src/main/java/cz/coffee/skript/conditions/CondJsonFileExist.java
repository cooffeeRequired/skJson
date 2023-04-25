package cz.coffee.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import static cz.coffee.core.utils.FileUtils.isJsonFile;


@Name("Json file exists")
@Description({"You can check if the json file already exists or not."})
@Examples("""
        command FileExists:
          trigger:
            if json file "plugins/test/main.json" already exists:
                set {_json} to json from string "{'A': [{'B': {}}, false, true, 10, 20, 22.22, 'A']}"
        """
)
@Since("2.8.0 - performance & clean")

public class CondJsonFileExist extends Condition {

    static {
        Skript.registerCondition(CondJsonFileExist.class,
                "json [file] %string% [already] exists",
                "json [file] %string% [already] does(n't| not) exists"
        );
    }

    private Expression<String> exprFile;
    private int line;


    @Override
    public boolean check(@NotNull Event e) {
        final String fileString = exprFile.getSingle(e);
        if (fileString == null) return false;
        return (line == 0) == isJsonFile(fileString);
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json file " + exprFile.toString(e, debug) + " already" + (line == 0 ? "exists" : "not exists");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        exprFile = (Expression<String>) exprs[0];
        line = matchedPattern;
        setNegated(line == 1);
        return true;
    }
}
