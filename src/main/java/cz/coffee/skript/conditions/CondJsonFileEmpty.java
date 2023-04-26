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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import cz.coffee.core.utils.FileUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;


@SuppressWarnings("ALL")
@Name("Json file is empty")
@Description("You can check if the json file empty")
@Examples("""
        Command jsonFileIsEmpty:
            trigger:
                if json file "plugins/raw/test.json" is empty:
                    send true
                else:
                    send false
        """
)
@Since("2.8.0 - performance & clean")

public class CondJsonFileEmpty extends Condition {

    static {
        Skript.registerCondition(CondJsonFileEmpty.class,
                "json file %string% is empty",
                "json file %string% is(n't| not) empty"
        );
    }

    private int line;
    private Expression<String> filePath;

    @Override
    public boolean check(@NotNull Event e) {
        boolean result = false;
        final String fileString = filePath.getSingle(e);
        assert fileString != null;
        final File file = new File(fileString);
        if (file.length() > 1) {
            final JsonElement JSON = FileUtils.get(file);
            if (JSON == null) return true;
            if (JSON instanceof JsonNull) result = true;
            if (JSON instanceof JsonObject object) result = object.isEmpty();
            if (JSON instanceof JsonArray array) result = array.isEmpty();
        }
        return (line == 0) == result;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean debug) {
        return "json file " + filePath.toString(e, debug) + " " + (line == 0 ? "is" : "does not") + " empty";
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?> @NotNull [] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        line = matchedPattern;
        filePath = (Expression<String>) exprs[0];
        setNegated(line == 1);
        return true;
    }
}
